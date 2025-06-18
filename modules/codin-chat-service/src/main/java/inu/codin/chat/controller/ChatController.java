package inu.codin.chat.controller;

import inu.codin.common.response.SingleResponse;
import inu.codin.common.response.ListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    @GetMapping("/rooms")
    public ListResponse<ChatRoomDto> getChatRooms(@RequestHeader("Authorization") String token) {
        // TODO: 채팅방 목록 조회 로직 구현
        return ListResponse.success(List.of());
    }

    @PostMapping("/rooms")
    public SingleResponse<String> createChatRoom(@RequestHeader("Authorization") String token,
                                               @RequestBody CreateChatRoomRequest request) {
        // TODO: 채팅방 생성 로직 구현
        return SingleResponse.success("채팅방 생성 성공");
    }

    @GetMapping("/rooms/{roomId}/messages")
    public ListResponse<ChatMessageDto> getChatMessages(@PathVariable String roomId,
                                                       @RequestHeader("Authorization") String token,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "50") int size) {
        // TODO: 채팅 메시지 조회 로직 구현
        return ListResponse.success(List.of());
    }

    @PostMapping("/rooms/{roomId}/join")
    public SingleResponse<String> joinChatRoom(@PathVariable String roomId,
                                             @RequestHeader("Authorization") String token) {
        // TODO: 채팅방 참여 로직 구현
        return SingleResponse.success("채팅방 참여 성공");
    }

    @PostMapping("/rooms/{roomId}/leave")
    public SingleResponse<String> leaveChatRoom(@PathVariable String roomId,
                                              @RequestHeader("Authorization") String token) {
        // TODO: 채팅방 나가기 로직 구현
        return SingleResponse.success("채팅방 나가기 성공");
    }

    // DTOs
    public static class ChatRoomDto {
        private String id;
        private String name;
        private String type; // DIRECT, GROUP
        private int memberCount;
        private String lastMessage;
        private LocalDateTime lastMessageTime;
        // getters, setters
    }

    public static class ChatMessageDto {
        private String id;
        private String senderId;
        private String senderName;
        private String message;
        private String type; // TEXT, IMAGE, FILE
        private LocalDateTime createdAt;
        // getters, setters
    }

    public static class CreateChatRoomRequest {
        private String name;
        private String type;
        private List<String> memberIds;
        // getters, setters
    }
}

@Controller
class WebSocketChatController {

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(ChatMessage chatMessage) {
        // TODO: WebSocket 메시지 처리 로직 구현
        return chatMessage;
    }

    public static class ChatMessage {
        private String roomId;
        private String senderId;
        private String senderName;
        private String content;
        private String type;
        // getters, setters
    }
}
