package inu.codin.codin.domain.post.dto.request;

import inu.codin.codin.domain.post.entity.PostCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class PostCreateRequestDTO {

//    @Schema(description = "유저 ID", example = "111111")
//    @NotBlank
//    private String userId;

    @Schema(description = "게시물 제목", example = "Example")
    @NotBlank
    private String title;

    @Schema(description = "게시물 내용", example = "example content")
    @NotBlank
    private String content;

    //이미지 별도 Multipart (RequestPart 사용)

    @Schema(description = "게시물 익명 여부 default = 0 (익명)", example = "true")
    @NotNull
    private boolean isAnonymous;

    @Schema(description = "게시물 종류", example = "구해요_스터디")
    @NotNull
    private PostCategory postCategory;
    //STATUS 필드 - DEFAULT :: ACTIVE

}