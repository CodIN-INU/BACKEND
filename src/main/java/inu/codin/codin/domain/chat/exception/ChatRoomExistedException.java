package inu.codin.codin.domain.chat.exception;

import lombok.Getter;
import org.bson.types.ObjectId;

@Getter
public class ChatRoomExistedException extends ChatRoomException{

    private final ObjectId chatRoomId;

    public ChatRoomExistedException(ObjectId chatRoomId){
        super(ChatRoomErrorCode.CHATROOM_EXISTED);
        this.chatRoomId = chatRoomId;
    }
}
