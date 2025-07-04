package inu.codin.codin.domain.chat.domain.chatroom;

import inu.codin.codin.common.dto.BaseTimeEntity;
import inu.codin.codin.domain.chat.dto.chatroom.request.ChatRoomCreateRequestDto;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "chatroom")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ChatRoom extends BaseTimeEntity {

    @Id @NotBlank
    private ObjectId _id;

    @NotBlank
    private String roomName;

    @NotBlank
    private ObjectId referenceId; //채팅방이 시작한 곳의 id (게시글, 댓글, 대댓글 _id)

    @NotBlank
    private Participants participants; //참가자들의 userId (1:1 채팅에서는 두 명의 id만 들어감)

    private String lastMessage;

    private LocalDateTime currentMessageDate;


    @Builder
    public ChatRoom(String roomName, ObjectId referenceId, Participants participants, String lastMessage, LocalDateTime currentMessageDate) {
        this.roomName = roomName;
        this.referenceId = referenceId;
        this.participants = participants;
        this.lastMessage = lastMessage;
        this.currentMessageDate = currentMessageDate;
    }

    public static ChatRoom of(ChatRoomCreateRequestDto chatRoomCreateRequestDto, ObjectId senderId){
        Participants participants = getParticipants(chatRoomCreateRequestDto, senderId);
        return ChatRoom.builder()
                .roomName(chatRoomCreateRequestDto.getRoomName())
                .referenceId(chatRoomCreateRequestDto.getReferenceId())
                .participants(participants)
                .currentMessageDate(LocalDateTime.now())
                .build();
    }

    private static Participants getParticipants(ChatRoomCreateRequestDto chatRoomCreateRequestDto, ObjectId senderId) {
        Participants participants = new Participants();
        participants.create(senderId);
        participants.create(chatRoomCreateRequestDto.getReceiverId());
        return participants;
    }

    public boolean isExistedReactivateParticipants(ObjectId userId) {
        List<ParticipantInfo> info = this.participants.remainReceiver(userId);
        return !info.isEmpty();
    }

    public void updateLastMessage(String message){
        this.lastMessage = message;
        this.currentMessageDate = LocalDateTime.now();
    }
}
