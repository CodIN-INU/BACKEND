package inu.codin.codin.common.stomp;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.domain.chat.chatroom.entity.ChatRoom;
import inu.codin.codin.domain.chat.chatroom.repository.ChatRoomRepository;
import inu.codin.codin.domain.chat.chatting.dto.event.UpdateUnreadCountEvent;
import inu.codin.codin.domain.chat.chatting.entity.Chatting;
import inu.codin.codin.domain.chat.chatting.repository.ChattingRepository;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class StompMessageService {

    private final Map<String, String> sessionStore = new ConcurrentHashMap<>();

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final ChattingRepository chattingRepository;
    private final ApplicationEventPublisher eventPublisher;

    public void connectSession(StompHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        sessionStore.put(sessionId, "");
        log.info("[STOMP CONNECT] session 연결 : {}", sessionId);
    }

    public void enterToChatRoom(StompHeaderAccessor headerAccessor){
        Result result = getResult(headerAccessor);
        sessionStore.put(headerAccessor.getSessionId(), result.chatroom().get_id().toString());
        log.info("[STOMP SUBSCRIBE] session : {}, chatRoomId : {} ", headerAccessor.getSessionId(), result.chatroom().get_id().toString());

        List<Chatting> chattings = updateUnreadCount(result.chatroom.get_id(), result.user.get_id());
        result.chatroom.getParticipants().enter(result.user.get_id());
        chatRoomRepository.save(result.chatroom);
        if (!chattings.isEmpty())
            eventPublisher.publishEvent(new UpdateUnreadCountEvent(this, chattings, result.chatroom.get_id().toString()));
    }

    public void exitToChatRoom(StompHeaderAccessor headerAccessor) {
        Result result = getResult(headerAccessor);
        result.chatroom().getParticipants().exit(result.user().get_id());
        chatRoomRepository.save(result.chatroom());
        sessionStore.remove(headerAccessor.getSessionId());
        log.info("[STOMP UNSUBSCRIBE] session : {}, chatRoomId : {} ", headerAccessor.getSessionId(), result.chatroom().get_id().toString());
    }

    public void disconnectSession(StompHeaderAccessor headerAccessor){
        sessionStore.remove(headerAccessor.getSessionId());
        log.info("[STOMP DISCONNECT] session : {} ", headerAccessor.getSessionId());

    }

    private Result getResult(StompHeaderAccessor headerAccessor) {
        String email;
        if (headerAccessor.getUser() != null) {
            email = headerAccessor.getUser().getName();
        } else {
            throw new UsernameNotFoundException("헤더에서 유저를 찾을 수 없습니다.");
        }
        log.info(headerAccessor.toString());
        String chatroomId = headerAccessor.getFirstNativeHeader("chatRoomId");
        if (chatroomId == null || !ObjectId.isValid(chatroomId)) {
            throw new IllegalArgumentException("세션에서 가져올 수 없거나, 올바른 chatRoomId가 아닙니다: " + chatroomId);
        }
        ChatRoom chatroom = chatRoomRepository.findById(new ObjectId(chatroomId))
                .orElseThrow(() -> new NotFoundException("채팅방을 찾을 수 없습니다."));
        UserEntity user = userRepository.findByEmailAndStatusAll(email)
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다."));
        Result result = new Result(chatroom, user);
        return result;
    }



    private record Result(ChatRoom chatroom, UserEntity user) {
    }

    private List<Chatting> updateUnreadCount(ObjectId chatRoomId, ObjectId userId){
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(()-> new NotFoundException("채팅방을 찾을 수 없습니다."));
        List<Chatting> chattings = chattingRepository.findAllByChatRoomIdOrderByCreatedAtDesc(chatRoomId)
                .stream()
                .limit(chatRoom.getParticipants().getInfo().get(userId).getUnreadMessage())
                .map(chatting -> {
                    chatting.minusUnread();
                    return chattingRepository.save(chatting);
                }).toList();
        return chattings;

    }
}
