package inu.codin.codin.domain.chat.dto.chatting.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class ChattingAndUserIdResponseDto {

    private final List<ChattingResponseDto> chatting;

    private final String currentUserId;

    @Builder
    public ChattingAndUserIdResponseDto(List<ChattingResponseDto> chatting, String currentUserId) {
        this.chatting = chatting;
        this.currentUserId = currentUserId;
    }
}
