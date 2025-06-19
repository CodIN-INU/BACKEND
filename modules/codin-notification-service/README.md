# Notification Service - 알림 서비스

## 📋 개요
푸시 알림, 이메일 알림, 시스템 알림을 담당하는 마이크로서비스입니다.

## 🎯 주요 기능
- Firebase Cloud Messaging (FCM) 푸시 알림
- 이메일 알림 발송
- 인앱 알림 관리
- 알림 설정 관리
- 알림 히스토리 및 통계
- 알림 템플릿 관리

## 🚀 API 엔드포인트

### 푸시 알림
- `POST /notifications/push/send` - 푸시 알림 발송
- `POST /notifications/push/topic` - 토픽 기반 알림 발송
- `POST /notifications/push/multicast` - 다중 사용자 알림 발송

### 이메일 알림
- `POST /notifications/email/send` - 이메일 발송
- `POST /notifications/email/template` - 템플릿 기반 이메일 발송
- `GET /notifications/email/templates` - 이메일 템플릿 목록

### 알림 관리
- `GET /notifications/users/{userId}` - 사용자 알림 목록
- `PUT /notifications/{notificationId}/read` - 알림 읽음 처리
- `DELETE /notifications/{notificationId}` - 알림 삭제
- `PUT /notifications/users/{userId}/read-all` - 모든 알림 읽음 처리

### 알림 설정
- `GET /notifications/users/{userId}/settings` - 알림 설정 조회
- `PUT /notifications/users/{userId}/settings` - 알림 설정 변경
- `POST /notifications/users/{userId}/tokens` - FCM 토큰 등록

## 🔧 기술 스택
- Spring Boot 3.3.5
- Firebase Admin SDK
- Spring Mail
- MongoDB
- Redis (알림 큐)
- Thymeleaf (이메일 템플릿)

## ⚙️ 환경 설정

```yaml
server:
  port: 8084

spring:
  application:
    name: notification-service
  data:
    mongodb:
      uri: mongodb://localhost:27017/codin_notification
  data:
    redis:
      host: localhost
      port: 6379
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${EMAIL_USERNAME}
    password: ${EMAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true

firebase:
  config-path: classpath:firebase-service-account.json
  project-id: ${FIREBASE_PROJECT_ID}

notification:
  batch:
    size: 500
    delay: 1000
  retry:
    max-attempts: 3
    delay: 5000
```

## 📊 데이터 모델

### Notification Collection
```json
{
  "_id": "ObjectId",
  "userId": "user123",
  "type": "POST_LIKED",
  "title": "새로운 좋아요",
  "message": "홍길동님이 회원님의 게시글을 좋아합니다.",
  "data": {
    "postId": "post456",
    "likedByUserId": "user789",
    "likedByUserName": "홍길동"
  },
  "imageUrl": "https://s3.../notification-image.png",
  "isRead": false,
  "channel": "PUSH", // PUSH, EMAIL, IN_APP
  "priority": "NORMAL", // HIGH, NORMAL, LOW
  "scheduledAt": "2024-12-01T10:30:00Z",
  "sentAt": "2024-12-01T10:30:05Z",
  "createdAt": "2024-12-01T10:29:50Z"
}
```

### FCM Token Collection
```json
{
  "_id": "ObjectId",
  "userId": "user123",
  "token": "fcm_device_token_here",
  "deviceType": "ANDROID", // ANDROID, IOS, WEB
  "deviceId": "device_unique_id",
  "appVersion": "1.2.0",
  "isActive": true,
  "lastUsedAt": "2024-12-01T10:30:00Z",
  "createdAt": "2024-12-01T09:00:00Z"
}
```

### Notification Settings Collection
```json
{
  "_id": "ObjectId",
  "userId": "user123",
  "pushEnabled": true,
  "emailEnabled": true,
  "settings": {
    "POST_LIKED": {
      "push": true,
      "email": false,
      "inApp": true
    },
    "COMMENT_ADDED": {
      "push": true,
      "email": true,
      "inApp": true
    },
    "CHAT_MESSAGE": {
      "push": true,
      "email": false,
      "inApp": true
    }
  },
  "quietHours": {
    "enabled": true,
    "startTime": "22:00",
    "endTime": "08:00",
    "timezone": "Asia/Seoul"
  },
  "updatedAt": "2024-12-01T10:30:00Z"
}
```

### Email Template Collection
```json
{
  "_id": "ObjectId",
  "name": "POST_LIKED",
  "subject": "새로운 좋아요 알림",
  "htmlTemplate": "<html>...</html>",
  "textTemplate": "{{userName}}님이 회원님의 게시글을 좋아합니다.",
  "variables": ["userName", "postTitle", "postUrl"],
  "isActive": true,
  "createdAt": "2024-12-01T00:00:00Z"
}
```

## 🔔 알림 유형

### 시스템 알림 유형
```java
public enum NotificationType {
    // 게시글 관련
    POST_LIKED("게시글 좋아요"),
    POST_COMMENTED("게시글 댓글"),
    POST_BOOKMARKED("게시글 북마크"),
    
    // 댓글 관련
    COMMENT_LIKED("댓글 좋아요"),
    COMMENT_REPLIED("댓글 답글"),
    
    // 채팅 관련
    CHAT_MESSAGE("채팅 메시지"),
    CHAT_ROOM_INVITED("채팅방 초대"),
    
    // 시스템 관련
    SYSTEM_NOTICE("시스템 공지"),
    MAINTENANCE("점검 안내"),
    
    // 사용자 관련
    PROFILE_VISIT("프로필 방문"),
    FOLLOW("팔로우"),
    
    // 이메일 인증
    EMAIL_VERIFICATION("이메일 인증"),
    PASSWORD_RESET("비밀번호 재설정");
}
```

## 📱 FCM 푸시 알림

### 단일 사용자 알림
```java
@Service
public class FCMService {
    
    public void sendNotification(String userId, NotificationRequest request) {
        List<String> tokens = fcmTokenService.getActiveTokens(userId);
        
        Message message = Message.builder()
            .setNotification(Notification.builder()
                .setTitle(request.getTitle())
                .setBody(request.getMessage())
                .setImage(request.getImageUrl())
                .build())
            .setData(request.getData())
            .setAndroidConfig(AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH)
                .build())
            .setApnsConfig(ApnsConfig.builder()
                .setAps(Aps.builder()
                    .setSound("default")
                    .build())
                .build())
            .build();
            
        for (String token : tokens) {
            try {
                FirebaseMessaging.getInstance().send(message.toBuilder().setToken(token).build());
            } catch (Exception e) {
                handleTokenError(token, e);
            }
        }
    }
}
```

### 토픽 기반 알림
```java
public void sendTopicNotification(String topic, NotificationRequest request) {
    Message message = Message.builder()
        .setTopic(topic)
        .setNotification(Notification.builder()
            .setTitle(request.getTitle())
            .setBody(request.getMessage())
            .build())
        .build();
        
    FirebaseMessaging.getInstance().send(message);
}
```

## 📧 이메일 알림

### 템플릿 기반 이메일 발송
```java
@Service
public class EmailService {
    
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    
    public void sendTemplateEmail(String email, String templateName, Map<String, Object> variables) {
        EmailTemplate template = emailTemplateService.getTemplate(templateName);
        
        Context context = new Context();
        context.setVariables(variables);
        
        String htmlContent = templateEngine.process(template.getHtmlTemplate(), context);
        
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setTo(email);
        helper.setSubject(template.getSubject());
        helper.setText(htmlContent, true);
        
        mailSender.send(message);
    }
}
```

### 이메일 템플릿 예시
```html
<!-- auth-email.html -->
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>이메일 인증</title>
</head>
<body>
    <div style="max-width: 600px; margin: 0 auto;">
        <h2>CODIN 이메일 인증</h2>
        <p>안녕하세요, <strong th:text="${userName}"></strong>님!</p>
        <p>CODIN 회원가입을 위해 이메일 인증을 완료해주세요.</p>
        
        <div style="text-align: center; margin: 30px 0;">
            <div style="font-size: 24px; font-weight: bold; color: #3498db;">
                <span th:text="${verificationCode}"></span>
            </div>
        </div>
        
        <p>인증 코드는 10분간 유효합니다.</p>
        <p>감사합니다.</p>
    </div>
</body>
</html>
```

## 🔄 서비스 간 통신

### Content Service 연동
```java
@Component
public class ContentEventHandler {
    
    @EventListener
    public void handlePostLiked(PostLikedEvent event) {
        NotificationRequest notification = NotificationRequest.builder()
            .userId(event.getPostAuthorId())
            .type(NotificationType.POST_LIKED)
            .title("새로운 좋아요")
            .message(event.getLikerName() + "님이 회원님의 게시글을 좋아합니다.")
            .data(Map.of(
                "postId", event.getPostId(),
                "likedByUserId", event.getLikerId()
            ))
            .build();
            
        notificationService.sendNotification(notification);
    }
}
```

### Chat Service 연동
```java
@FeignClient(name = "chat-service")
public interface ChatServiceClient {
    
    @PostMapping("/chat/notifications/message-sent")
    void notifyMessageSent(@RequestBody ChatMessageEvent event);
}
```

## 📊 알림 통계 및 분석

### 알림 전송 통계
```java
@Service
public class NotificationAnalyticsService {
    
    public NotificationStats getNotificationStats(String userId, LocalDate from, LocalDate to) {
        return NotificationStats.builder()
            .totalSent(getTotalSent(userId, from, to))
            .totalRead(getTotalRead(userId, from, to))
            .readRate(calculateReadRate(userId, from, to))
            .byType(getStatsByType(userId, from, to))
            .byChannel(getStatsByChannel(userId, from, to))
            .build();
    }
}
```

### Redis를 활용한 실시간 알림 큐
```java
@Service
public class NotificationQueueService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    public void enqueueNotification(NotificationRequest request) {
        String queueKey = "notification:queue:" + request.getPriority().name().toLowerCase();
        redisTemplate.opsForList().leftPush(queueKey, request);
    }
    
    @Scheduled(fixedDelay = 1000)
    public void processNotificationQueue() {
        // 우선순위별 큐 처리
        processQueue("notification:queue:high");
        processQueue("notification:queue:normal");
        processQueue("notification:queue:low");
    }
}
```

## 🧪 테스트

```bash
# 단위 테스트
./gradlew :modules:codin-notification-service:test

# FCM 통합 테스트
./gradlew :modules:codin-notification-service:fcmTest

# 이메일 발송 테스트
./gradlew :modules:codin-notification-service:emailTest
```

## 📈 성능 최적화

### 배치 처리
- FCM 알림: 500개씩 배치 처리
- 이메일 발송: 100개씩 배치 처리
- 처리 간격: 1초

### 캐싱 전략
- 알림 설정: 1시간 캐싱
- FCM 토큰: 30분 캐싱
- 이메일 템플릿: 24시간 캐싱

### 비동기 처리
```java
@Async("notificationExecutor")
public CompletableFuture<Void> sendNotificationAsync(NotificationRequest request) {
    try {
        sendNotification(request);
        return CompletableFuture.completedFuture(null);
    } catch (Exception e) {
        return CompletableFuture.failedFuture(e);
    }
}
```

## 🔐 보안 및 개인정보 보호

### 개인정보 보호
- 알림 내용 암호화 저장
- 민감한 정보 마스킹 처리
- GDPR 준수 (사용자 데이터 삭제)

### FCM 토큰 보안
- 토큰 만료 시 자동 삭제
- 디바이스별 토큰 관리
- 비활성 토큰 정리

### Rate Limiting
- 사용자당 알림 발송 제한
- IP별 API 호출 제한
- 스팸 알림 방지
