package inu.codin.codin.domain.chat.event;

import inu.codin.codin.domain.chat.domain.chatroom.ParticipantInfo;
import inu.codin.codin.domain.chat.domain.chatroom.event.ChatRoomNotificationEvent;
import inu.codin.codin.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatRoomEventListener {

    private final NotificationService notificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChatRoomNotification(ChatRoomNotificationEvent event){
        Optional.ofNullable(event.getParticipants().getInfo().get(event.getReceiverId()))
                .filter(ParticipantInfo::isNotificationsEnabled)
                .ifPresent(participant ->
                        notificationService.sendNotificationMessageByChat(participant.getUserId(), event.getChatRoomId())
                );
    }
}