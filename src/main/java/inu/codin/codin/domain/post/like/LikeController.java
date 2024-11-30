package inu.codin.codin.domain.post.like;

import inu.codin.codin.common.response.SingleResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @Operation(summary = "게시물, 댓글, 대댓글 좋아요 추가"+
            "entityType = post,comment,reply"
            +"entityId = postId, commentId, replyId")
    @PostMapping("/{entityType}/{entityId}/{userId}")
    public ResponseEntity<SingleResponse<?>> likeEntity(
            @PathVariable String entityType,
            @PathVariable String entityId,
            @PathVariable String userId) {
        likeService.addLike(entityType, entityId, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SingleResponse<>(201, "좋아요가 추가되었습니다.", null));
    }

    @Operation(summary = "게시물, 댓글, 대댓글 좋아요 삭제 " +
            "entityType = post,comment,reply"
    +"entityId = postId, commentId, replyId")
    @DeleteMapping("/{entityType}/{entityId}/{userId}")
    public ResponseEntity<SingleResponse<?>> unlikeEntity(
            @PathVariable String entityType,
            @PathVariable String entityId,
            @PathVariable String userId) {
        likeService.removeLike(entityType, entityId, userId);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "좋아요가 취소되었습니다.", null));
    }
}