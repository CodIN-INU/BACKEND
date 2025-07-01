package inu.codin.codin.domain.chat.stomp;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.domain.chat.domain.chatroom.ChatRoom;
import inu.codin.codin.domain.chat.domain.chatting.Chatting;
import inu.codin.codin.domain.chat.domain.chatting.event.ChattingUnreadCountEvent;
import inu.codin.codin.domain.chat.exception.ChatRoomErrorCode;
import inu.codin.codin.domain.chat.exception.ChatRoomException;
import inu.codin.codin.domain.chat.exception.ChattingErrorCode;
import inu.codin.codin.domain.chat.exception.ChattingException;
import inu.codin.codin.domain.chat.repository.ChatRoomRepository;
import inu.codin.codin.domain.chat.repository.ChattingRepository;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
@RequiredArgsConstructor
@Slf4j
public class StompMessageService {

    private final Map<String, String> sessionStore = new ConcurrentHashMap<>();

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final ChattingRepository chattingRepository;

    private final MongoTemplate mongoTemplate;
    private final ApplicationEventPublisher eventPublisher;

    public void connectSession(StompHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        sessionStore.put(sessionId, "");
        log.info("[STOMP CONNECT] session 연결 : {}", sessionId);
    }

    @Transactional
    public void enterToChatRoom(StompHeaderAccessor headerAccessor){
        ChatRoomContext context = resolveChatRoomContext(headerAccessor);
        sessionStore.put(headerAccessor.getSessionId(), context.chatroom().get_id().toString());

        publishingUnreadCount(context);

        context.chatroom.getParticipants().enter(context.user.get_id());
        chatRoomRepository.save(context.chatroom);
        log.info("[STOMP SUBSCRIBE] session : {}, chatRoomId : {} ", headerAccessor.getSessionId(), context.chatroom().get_id().toString());

    }

    private void publishingUnreadCount(ChatRoomContext context) {
        List<Chatting> updateChats = updateUnreadCount(context.chatroom.get_id(), context.user.get_id());
        if (!updateChats.isEmpty())
            eventPublisher.publishEvent(new ChattingUnreadCountEvent(this, updateChats, context.chatroom.get_id().toString()));
    }

    @Transactional
    public void exitToChatRoom(StompHeaderAccessor headerAccessor) {
        ChatRoomContext context = resolveChatRoomContext(headerAccessor);

        context.chatroom().getParticipants().exit(context.user().get_id());
        chatRoomRepository.save(context.chatroom());
        log.info("[STOMP UNSUBSCRIBE] session : {}, chatRoomId : {} ", headerAccessor.getSessionId(), context.chatroom().get_id().toString());
    }

    public void disconnectSession(StompHeaderAccessor headerAccessor){
        sessionStore.remove(headerAccessor.getSessionId());
        log.info("[STOMP DISCONNECT] session : {} ", headerAccessor.getSessionId());
    }

    private ChatRoomContext resolveChatRoomContext(StompHeaderAccessor headerAccessor) {
        UserEntity user = getUserEntityOrThrow(headerAccessor);
        ChatRoom chatroom = getChatRoomOrThrow(headerAccessor);

        return new ChatRoomContext(chatroom, user);
    }

    private UserEntity getUserEntityOrThrow(StompHeaderAccessor headerAccessor) {
        String email = Optional.ofNullable(headerAccessor.getUser())
                .map(Principal::getName)
                .orElseThrow(() -> new ChattingException(ChattingErrorCode.CHATTING_USER_NOT_FOUND, headerAccessor.getSessionId()));

        return userRepository.findByEmailAndStatusAll(email)
                .orElseThrow(()-> new NotFoundException("유저를 찾을 수 없습니다."));
    }

    private ChatRoom getChatRoomOrThrow(StompHeaderAccessor headerAccessor) {
        String chatroomId = headerAccessor.getFirstNativeHeader("chatRoomId");
        if (chatroomId == null || !ObjectId.isValid(chatroomId))
            throw new ChattingException(ChattingErrorCode.CHATTING_ID_NOT_FOUND, headerAccessor.getSessionId());

        return getChatRoom(new ObjectId(chatroomId));
    }

    private ChatRoom getChatRoom(ObjectId chatroomId) {
        return chatRoomRepository.findBy_idAndDeletedAtIsNull(chatroomId)
                .orElseThrow(() -> new ChatRoomException(ChatRoomErrorCode.CHATROOM_NOT_FOUND));
    }

    private record ChatRoomContext(ChatRoom chatroom, UserEntity user) {
    }

    private List<Chatting> updateUnreadCount(ObjectId chatRoomId, ObjectId userId){
        ChatRoom chatRoom = getChatRoom(chatRoomId);
        int unreadCount = chatRoom.getParticipants().getUnreadCount(userId);

        List<Chatting> unreadChats = List.of();
        if (unreadCount > 0) { //unreadCount가 있을 경우,
            //유저가 읽지 않은 채팅만 가져와서 채팅의 unreadCount를 줄인다.
            unreadChats = chattingRepository.findAllByChatRoomIdOrderByCreatedAtDesc(chatRoomId, PageRequest.of(0, unreadCount));
            bulkUpdateUnreadCount(unreadChats);
        }
        return unreadChats;
    }

    private void bulkUpdateUnreadCount(List<Chatting> chats) {
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Chatting.class);

        chats.forEach(chat -> {
            Query query = new Query(where("_id").is(chat.get_id()));
            Update update = new Update().inc("unreadCount", -1);
            bulkOps.updateOne(query, update);

            chat.minusUnread();
        });

        bulkOps.execute();
    }

}
