package inu.codin.codin.domain.post.domain.comment.controller;

import inu.codin.codin.common.response.ListResponse;
import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.domain.post.domain.comment.dto.CommentCreateRequestDTO;
import inu.codin.codin.domain.post.domain.comment.dto.CommentResponseDTO;
import inu.codin.codin.domain.post.domain.comment.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @Operation(summary = "댓글 추가")
    @PostMapping("/{postId}")
    public ResponseEntity<SingleResponse<?>> addComment(@PathVariable String postId,
                                             @RequestBody @Valid CommentCreateRequestDTO requestDTO) {
        commentService.addComment(postId, requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SingleResponse<>(201, "댓글이 추가되었습니다.", null));
    }

    @Operation(summary = "해당 게시물의 댓글 및 대댓글 조회")
    @GetMapping("/post/{postId}")
    public ResponseEntity<ListResponse<CommentResponseDTO>> getCommentsByPostId(@PathVariable String postId) {
        List<CommentResponseDTO> response = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok()
                .body(new ListResponse<>(200, "해당 게시물의 댓글 및 대댓글 조회 성공", response));

    }

    @Operation(summary = "댓글 삭제")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<SingleResponse<?>> deleteComment(@PathVariable String commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "댓글이 삭제되었습니다.", null));
    }
}