package inu.codin.codin.domain.chat.dto.chatroom.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class ChatRoomCreateResponseDto {

    @Schema(description = "생성된 채팅방 _id", example = "111111")
    private final String chatRoomId;

    public ChatRoomCreateResponseDto(String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }
}
