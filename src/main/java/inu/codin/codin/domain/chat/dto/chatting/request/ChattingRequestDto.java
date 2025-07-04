package inu.codin.codin.domain.chat.dto.chatting.request;

import inu.codin.codin.domain.chat.domain.chatting.ContentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class ChattingRequestDto {

    @NotBlank
    @Schema(description = "채팅 내용", example = "안녕하세요")
    private String content;

    @NotNull
    @Schema(description = "채팅 타입", example = "TEXT")
    private ContentType contentType;
}
