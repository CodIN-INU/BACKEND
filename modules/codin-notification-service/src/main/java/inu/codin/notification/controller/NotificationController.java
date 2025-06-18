package inu.codin.notification.controller;

import inu.codin.common.response.SingleResponse;
import inu.codin.common.response.ListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    @GetMapping
    public ListResponse<NotificationDto> getNotifications(@RequestHeader("Authorization") String token,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "20") int size) {
        // TODO: 알림 목록 조회 로직 구현
        return ListResponse.success(List.of());
    }

    @PutMapping("/{notificationId}/read")
    public SingleResponse<String> markAsRead(@PathVariable String notificationId,
                                           @RequestHeader("Authorization") String token) {
        // TODO: 알림 읽음 처리 로직 구현
        return SingleResponse.success("알림 읽음 처리 완료");
    }

    @PutMapping("/read-all")
    public SingleResponse<String> markAllAsRead(@RequestHeader("Authorization") String token) {
        // TODO: 모든 알림 읽음 처리 로직 구현
        return SingleResponse.success("모든 알림 읽음 처리 완료");
    }

    @PostMapping("/send")
    public SingleResponse<String> sendNotification(@RequestBody SendNotificationRequest request) {
        // TODO: 알림 전송 로직 구현 (내부 API)
        return SingleResponse.success("알림 전송 성공");
    }

    @PostMapping("/settings")
    public SingleResponse<String> updateNotificationSettings(@RequestHeader("Authorization") String token,
                                                           @RequestBody NotificationSettingsRequest request) {
        // TODO: 알림 설정 업데이트 로직 구현
        return SingleResponse.success("알림 설정 업데이트 성공");
    }

    // DTOs
    public static class NotificationDto {
        private String id;
        private String type;
        private String title;
        private String message;
        private boolean isRead;
        private LocalDateTime createdAt;
        // getters, setters
    }

    public static class SendNotificationRequest {
        private String userId;
        private String type;
        private String title;
        private String message;
        private boolean sendPush;
        private boolean sendEmail;
        // getters, setters
    }

    public static class NotificationSettingsRequest {
        private boolean enablePush;
        private boolean enableEmail;
        private boolean enableLike;
        private boolean enableComment;
        private boolean enableFollow;
        // getters, setters
    }
}
