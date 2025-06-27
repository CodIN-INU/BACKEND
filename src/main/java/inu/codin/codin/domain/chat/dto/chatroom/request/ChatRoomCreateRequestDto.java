package inu.codin.codin.domain.chat.dto.chatroom.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import org.bson.types.ObjectId;

@Getter
@Builder
public class ChatRoomCreateRequestDto {

    @NotBlank
    @Schema(description = "채팅방 제목", example = "채팅해요")
    private String roomName;

    @NotBlank
    @Schema(description = "채팅 수신자", example = "1111111")
    private String receiverId;

    @NotBlank
    @Schema(description = "채팅이 시작된 게시글, 댓글, 댓글의 id", example = "65asdf")
    private String referenceId;

    public ObjectId getReceiverId(){
        return new ObjectId(this.receiverId);
    }

    public ObjectId getReferenceId(){
        return new ObjectId(this.referenceId);
    }
}
