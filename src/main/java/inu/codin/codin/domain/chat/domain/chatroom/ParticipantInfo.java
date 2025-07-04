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

    static ParticipantInfo enter(ObjectId userId) {
        return new ParticipantInfo(userId);
    }

    void updateNotification() {
        this.notificationsEnabled = !notificationsEnabled;
    }

    void incrementUnreadCount() {
        this.unreadCount++;
    }

    void connect() {
        this.isConnected = true;
        this.unreadCount = 0;
    }

    void disconnect() {
        this.isConnected = false;
        this.unreadCount = 0;
        setUpdatedAt();
    }

    void leave() {
        this.isLeaved = true;
        this.whenLeaved = LocalDateTime.now();
        disconnect();
    }

    void remain() {
        this.isLeaved = false;
    }

    boolean isNotified(ObjectId userId) {
        return !this.userId.equals(userId) && this.notificationsEnabled && !this.isConnected;
    }

}
