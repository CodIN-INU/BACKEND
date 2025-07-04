package inu.codin.codin.domain.chat.event;

import inu.codin.codin.domain.chat.domain.chatroom.ChatRoom;
import inu.codin.codin.domain.chat.domain.chatroom.Participants;
import inu.codin.codin.domain.chat.domain.chatting.Chatting;
import inu.codin.codin.domain.chat.domain.chatting.event.ChattingArrivedEvent;
import inu.codin.codin.domain.chat.domain.chatting.event.ChattingNotificationEvent;
import inu.codin.codin.domain.chat.domain.chatting.event.ChattingUnreadCountEvent;
import inu.codin.codin.domain.chat.repository.ChatRoomRepository;
import inu.codin.codin.domain.notification.service.NotificationService;
import inu.codin.codin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChattingEventListener {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final SimpMessageSendingOperations template;
    private final NotificationService notificationService;

    /**
        채팅을 발신했을 경우,
        1. 채팅방의 마지막 메세지 업데이트
        2. 상대방이 접속한 상태가 아니라면 상대방의 unread 값 +1
        3. /queue/chatroom/unread 를 통해 상대방의 채팅방 목록 실시간 업데이트
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChattingArrivedEvent(ChattingArrivedEvent event){
        updateLastMessage(event);
        updateUnreadCountAndNotify(event.getChatting(), event.getChatRoom());
        chatRoomRepository.save(event.getChatRoom());
        log.info("[handleChattingArrivedEvent] ChattingArrivedEvent 완료");
    }

    private void updateLastMessage(ChattingArrivedEvent event){
        event.getChatRoom().updateLastMessage(event.getChatting().getContent());
    }

    private void updateUnreadCountAndNotify(Chatting chatting, ChatRoom chatRoom) {
        Participants participants = chatRoom.getParticipants();
        participants.getDisconnectedUsersAndUpdateUnreadCount(chatting.getSenderId())
                .forEach(receiverInfo -> sendUnreadCountChange(receiverInfo.receiverId(), getLastMessageAndUnread(chatting, receiverInfo.unreadMessage())));
    }

    private Map<String, String> getLastMessageAndUnread(Chatting chatting, int unreadMessage) {
        return Map.of(
                "chatRoomId", chatting.getChatRoomId().toString(),
                "lastMessage", chatting.getContent(),
                "unread", String.valueOf(unreadMessage)
        );
    }

    private void sendUnreadCountChange(ObjectId receiverId, Map<String, String> result) {
        userRepository.findByUserId(receiverId)
                .ifPresent(userEntity -> template.convertAndSendToUser(userEntity.getEmail(), "/queue/chatroom/unread", result));
        log.info("[sendUnreadCountChange] user : {}, /queue/chatroom/unread 전송 완료", receiverId);
    }

    /**
     * 채팅을 받는 사람들에게 알림 보내기
     * @param event
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChattingNotificationEvent(ChattingNotificationEvent event){
        ChatRoom chatRoom = event.getChatRoom();
        chatRoom.getParticipants().getUsersToNotify(event.getUserId())
                .forEach(userId -> notificationService.sendNotificationMessageByChat(userId, chatRoom.get_id()));
        log.info("[handleChattingNotificationEvent] ChattingNotificationEvent 완료");
    }

    /**
        유저가 채팅방 입장 시, 읽지 않은 채팅에 대하여 새로운 unread 값 송신
        클라이언트 : chat_id 와 일치하는 채팅값의 unread 값 업데이트
     */
    @EventListener
    public void updateUnreadCountEvent(ChattingUnreadCountEvent chattingUnreadCountEvent){
        List<Map<String, String>> unreadChattingList =
                chattingUnreadCountEvent.getChattingList().stream()
                        .map(chatting -> Map.of(
                                "id", chatting.get_id().toString(),
                                "unread", String.valueOf(chatting.getUnreadCount())
                        )).toList();

        template.convertAndSend("/queue/unread/"+ chattingUnreadCountEvent.getChatRoomId(), unreadChattingList);
        log.info("[updateUnreadCountEvent] ChattingUnreadCountEvent 완료");
    }

}
