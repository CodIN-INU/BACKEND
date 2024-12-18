package inu.codin.codin.domain.post.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import inu.codin.codin.domain.post.entity.PostCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PostDetailResponseDTO {
    @Schema(description = "유저 ID", example = "111111")
    @NotBlank
    private String userId;

    @Schema(description = "게시물 ID", example = "111111")
    @NotBlank
    private String postId;

    @Schema(description = "게시물 종류", example = "구해요")
    @NotBlank
    private PostCategory postCategory;

    @Schema(description = "게시물 제목", example = "Example")
    @NotBlank
    private String title;

    @Schema(description = "게시물 내용", example = "example content")
    @NotBlank
    private String content;

    @Schema(description = "게시물 내 이미지 url , blank 가능", example = "example/1231")
    private List<String> postImageUrl;

    @Schema(description = "게시물 익명 여부 default = 0 (익명)", example = "0")
    @NotNull
    private boolean isAnonymous;

    @Schema(description = "좋아요 count", example = "0")
    private int likeCount;

    @Schema(description = "스크랩 count", example = "0")
    private int scrapCount;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    @Schema(description = "생성 일자", example = "2024-12-02 20:10:18")
    private LocalDateTime createdAt;

    public PostDetailResponseDTO(String userId, String postId, String content, String title, PostCategory postCategory, List<String> postImageUrls , boolean isAnonymous, int likeCount, int scrapCount, LocalDateTime createdAt) {
        this.userId = userId;
        this.postId = postId;
        this.content = content;
        this.title = title;
        this.postCategory = postCategory;
        this.postImageUrl = postImageUrls;
        this.isAnonymous = isAnonymous;
        this.likeCount = likeCount;
        this.scrapCount = scrapCount;
        this.createdAt = createdAt;
    }
}
