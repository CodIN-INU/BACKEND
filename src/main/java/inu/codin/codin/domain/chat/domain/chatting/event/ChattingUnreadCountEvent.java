package inu.codin.codin.domain.chat.domain.chatting.event;

import inu.codin.codin.domain.chat.domain.chatting.Chatting;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
public class ChattingUnreadCountEvent extends ApplicationEvent {

    private final List<Chatting> chattingList;
    private final String chatRoomId;

    public ChattingUnreadCountEvent(Object source, List<Chatting> chattingList, String chatRoomId) {
        super(source);
        this.chattingList = chattingList;
        this.chatRoomId = chatRoomId;
    }
}
