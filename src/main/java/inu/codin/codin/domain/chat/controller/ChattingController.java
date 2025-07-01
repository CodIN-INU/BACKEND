package inu.codin.codin.domain.chat.controller;

import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.domain.chat.dto.chatting.request.ChattingRequestDto;
import inu.codin.codin.domain.chat.service.ChattingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chats")
@Tag(name = "Chatting API", description = "채팅 보내기, 채팅 내역 반환")
public class ChattingController {

    private final ChattingService chattingService;

    @Operation(
            summary = "채팅 보내기"
    )
    @MessageMapping("/chats/{chatRoomId}") //client 측에서 앞에 '/pub' 를 붙여서 요청
    @SendTo("/queue/{chatRoomId}")
    public ResponseEntity<SingleResponse<?>> sendMessage(@DestinationVariable("chatRoomId") String chatRoomId,
                                                         @RequestBody @Valid ChattingRequestDto chattingRequestDto,
                                                         @AuthenticationPrincipal Authentication authentication){
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "채팅 송신 완료", chattingService.sendMessage(chatRoomId, chattingRequestDto, authentication)));
    }

    @Operation(
            summary = "채팅으로 사진 보내기"
    )
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SingleResponse<?>> sendImageMessage(List<MultipartFile> chatImages){
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "채팅 사진 업로드 완료", chattingService.sendImageMessage(chatImages)));
    }

    @Operation(
            summary = "채팅 내용 리스트 가져오기",
            description = "Pageable에 해당하는 page, size, sort 내역에 맞게 반환"
    )
    @GetMapping("/list/{chatRoomId}")
    public ResponseEntity<SingleResponse<?>> getAllMessage(@PathVariable("chatRoomId") String chatRoomId, @RequestParam("page") int page){
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "채팅 내용 리스트 반환 완료", chattingService.getAllMessage(chatRoomId, page)));
    }
}
