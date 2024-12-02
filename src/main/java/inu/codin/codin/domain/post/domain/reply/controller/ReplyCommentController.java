package inu.codin.codin.domain.post.domain.reply.controller;

import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.domain.post.domain.reply.service.ReplyCommentService;
import inu.codin.codin.domain.post.domain.reply.dto.request.ReplyCreateRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/replies")
public class ReplyCommentController {

    private final ReplyCommentService replyCommentService;

    @Operation(summary = "대댓글 추가")
    @PostMapping("/{commentId}")
    public ResponseEntity<SingleResponse<?>> addReply(@PathVariable String commentId,
                                                      @RequestBody @Valid ReplyCreateRequestDTO requestDTO) {
        replyCommentService.addReply(commentId, requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SingleResponse<>(201, "대댓글이 추가되었습니다.", null));

    }

    @Operation(summary = "대댓글 삭제")
    @DeleteMapping("/{replyId}")
    public ResponseEntity<SingleResponse<?>> deleteReply(@PathVariable String replyId) {
        replyCommentService.deleteReply(replyId);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "대댓글이 삭제되었습니다.", null));
    }
}