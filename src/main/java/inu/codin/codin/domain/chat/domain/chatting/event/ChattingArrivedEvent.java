package inu.codin.codin.domain.chat.domain.chatting.event;

import inu.codin.codin.domain.chat.domain.chatroom.ChatRoom;
import inu.codin.codin.domain.chat.domain.chatting.Chatting;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ChattingArrivedEvent extends ApplicationEvent {

    private final Chatting chatting;
    private final ChatRoom chatRoom;

    public ChattingArrivedEvent(Object source, Chatting chatting, ChatRoom chatRoom) {
        super(source);
        this.chatting = chatting;
        this.chatRoom = chatRoom;
    }
}
