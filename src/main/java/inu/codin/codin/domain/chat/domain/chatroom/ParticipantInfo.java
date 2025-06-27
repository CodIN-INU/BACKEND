package inu.codin.codin.domain.chat.domain.chatroom;

import inu.codin.codin.common.dto.BaseTimeEntity;
import lombok.*;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ParticipantInfo extends BaseTimeEntity {

    private ObjectId userId;
    private boolean isConnected = false;
    private int unreadCount = 0;
    private boolean isLeaved = false;
    private LocalDateTime whenLeaved;
    private boolean notificationsEnabled = true;

    public ParticipantInfo(ObjectId userId) {
        this.userId = userId;
    }

    public static ParticipantInfo enter(ObjectId userId) {
        return new ParticipantInfo(userId);
    }

    public void updateNotification() {
        this.notificationsEnabled = !notificationsEnabled;
    }

    public void incrementUnreadCount() {
        this.unreadCount++;
    }

    public void connect() {
        this.isConnected = true;
        this.unreadCount = 0;
    }

    public void disconnect() {
        this.isConnected = false;
        this.unreadCount = 0;
        setUpdatedAt();
    }

    public void leave() {
        this.isLeaved = true;
        this.whenLeaved = LocalDateTime.now();
        disconnect();
    }

    public void remain() {
        this.isLeaved = false;
    }

    public boolean isNotified(ObjectId userId){
        return !this.userId.equals(userId) && this.notificationsEnabled & !this.isConnected;
    }

}
