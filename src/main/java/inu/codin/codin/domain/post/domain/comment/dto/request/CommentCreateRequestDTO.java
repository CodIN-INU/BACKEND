package inu.codin.codin.domain.post.domain.comment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CommentCreateRequestDTO {

    @Schema(description = "댓글 내용", example = "content")
    @NotBlank
    private String content;

    @Schema(description = "게시물 익명 여부 default = true (익명)", example = "true")
    @NotNull
    private boolean anonymous;
}
