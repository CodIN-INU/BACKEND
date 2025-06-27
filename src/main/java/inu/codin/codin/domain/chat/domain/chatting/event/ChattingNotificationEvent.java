package inu.codin.codin.domain.chat.domain.chatting.event;

import inu.codin.codin.domain.chat.domain.chatroom.ChatRoom;
import lombok.Getter;
import org.bson.types.ObjectId;
import org.springframework.context.ApplicationEvent;
@Getter
public class ChattingNotificationEvent extends ApplicationEvent {

    private final ChatRoom chatRoom;
    private final ObjectId userId;


    public ChattingNotificationEvent(Object source, ObjectId userId, ChatRoom chatRoom) {
        super(source);
        this.userId = userId;
        this.chatRoom = chatRoom;
    }
}
