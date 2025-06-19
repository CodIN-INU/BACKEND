# Chat Service - 실시간 채팅 서비스

## 📋 개요
실시간 채팅, WebSocket 통신, 채팅방 관리를 담당하는 마이크로서비스입니다.

## 🎯 주요 기능
- 실시간 1:1 채팅
- 그룹 채팅방
- 파일 공유
- 이모지 및 스티커
- 메시지 검색
- 채팅방 알림 설정
- 온라인 상태 표시

## 🚀 API 엔드포인트

### 채팅방 관리
- `GET /chat/rooms` - 사용자 채팅방 목록
- `GET /chat/rooms/{roomId}` - 채팅방 상세 정보
- `POST /chat/rooms` - 새 채팅방 생성
- `PUT /chat/rooms/{roomId}` - 채팅방 정보 수정
- `DELETE /chat/rooms/{roomId}` - 채팅방 나가기

### 메시지 관리
- `GET /chat/rooms/{roomId}/messages` - 메시지 히스토리 조회
- `POST /chat/rooms/{roomId}/messages` - 메시지 전송 (REST API)
- `PUT /chat/messages/{messageId}` - 메시지 수정
- `DELETE /chat/messages/{messageId}` - 메시지 삭제

### 참가자 관리
- `GET /chat/rooms/{roomId}/participants` - 채팅방 참가자 목록
- `POST /chat/rooms/{roomId}/participants` - 사용자 초대
- `DELETE /chat/rooms/{roomId}/participants/{userId}` - 사용자 내보내기
- `PUT /chat/rooms/{roomId}/participants/{userId}/role` - 참가자 권한 변경

### WebSocket 엔드포인트
- `ws://localhost:8085/chat` - WebSocket 연결
- `/topic/rooms/{roomId}` - 채팅방 메시지 구독
- `/topic/users/{userId}` - 개인 알림 구독
- `/app/chat.send` - 메시지 전송
- `/app/chat.typing` - 타이핑 상태 전송

## 🔧 기술 스택
- Spring Boot 3.3.5
- Spring WebSocket & STOMP
- MongoDB
- Redis (메시지 큐, 온라인 상태)
- AWS S3 (파일 공유)

## ⚙️ 환경 설정

```yaml
server:
  port: 8085

spring:
  application:
    name: chat-service
  data:
    mongodb:
      uri: mongodb://localhost:27017/codin_chat
  data:
    redis:
      host: localhost
      port: 6379

websocket:
  allowed-origins: 
    - http://localhost:3000
    - https://codin.inu.ac.kr
  heartbeat:
    interval: 30000
    timeout: 60000

file:
  upload:
    max-size: 50MB
    allowed-types: jpg,jpeg,png,gif,pdf,doc,docx,mp4,mov

chat:
  message:
    max-length: 1000
    retention-days: 365
  room:
    max-participants: 100
```

## 📊 데이터 모델

### ChatRoom Collection
```json
{
  "_id": "ObjectId",
  "name": "컴공과 3학년 스터디",
  "description": "알고리즘 스터디 채팅방입니다.",
  "type": "GROUP", // PRIVATE, GROUP
  "ownerId": "user123",
  "participants": [
    {
      "userId": "user123",
      "role": "OWNER", // OWNER, ADMIN, MEMBER
      "joinedAt": "2024-12-01T10:00:00Z",
      "nickname": "홍길동",
      "isActive": true
    },
    {
      "userId": "user456",
      "role": "MEMBER",
      "joinedAt": "2024-12-01T10:30:00Z",
      "nickname": "김철수",
      "isActive": true
    }
  ],
  "settings": {
    "isPublic": false,
    "allowInvite": true,
    "maxParticipants": 50,
    "messageRetentionDays": 365
  },
  "avatarUrl": "https://s3.../room-avatar.jpg",
  "lastMessageAt": "2024-12-01T15:30:00Z",
  "lastMessage": {
    "id": "msg789",
    "content": "안녕하세요!",
    "senderId": "user456",
    "senderName": "김철수",
    "timestamp": "2024-12-01T15:30:00Z"
  },
  "unreadCount": 0,
  "isArchived": false,
  "createdAt": "2024-12-01T10:00:00Z",
  "updatedAt": "2024-12-01T15:30:00Z"
}
```

### Message Collection
```json
{
  "_id": "ObjectId",
  "roomId": "room123",
  "senderId": "user456",
  "senderName": "김철수",
  "senderAvatar": "https://s3.../avatar.jpg",
  "type": "TEXT", // TEXT, IMAGE, FILE, SYSTEM
  "content": "안녕하세요! 스터디 시간은 언제인가요?",
  "attachments": [
    {
      "id": "file789",
      "name": "study-schedule.pdf",
      "url": "https://s3.../file789.pdf",
      "size": 1024000,
      "mimeType": "application/pdf"
    }
  ],
  "mentions": ["user123", "user789"],
  "replyTo": {
    "messageId": "msg456",
    "content": "스터디 일정을 확인해주세요",
    "senderId": "user123",
    "senderName": "홍길동"
  },
  "reactions": [
    {
      "emoji": "👍",
      "users": ["user123", "user789"],
      "count": 2
    }
  ],
  "isEdited": false,
  "editedAt": null,
  "isDeleted": false,
  "deletedAt": null,
  "readBy": [
    {
      "userId": "user123",
      "readAt": "2024-12-01T15:31:00Z"
    }
  ],
  "timestamp": "2024-12-01T15:30:00Z"
}
```

### UserStatus Collection (Redis)
```json
{
  "userId": "user123",
  "status": "ONLINE", // ONLINE, OFFLINE, AWAY, BUSY
  "lastSeen": "2024-12-01T15:30:00Z",
  "currentRoomId": "room123",
  "deviceInfo": {
    "type": "WEB",
    "userAgent": "Mozilla/5.0...",
    "ipAddress": "192.168.1.100"
  }
}
```

## 🔌 WebSocket 통신

### WebSocket 구성
```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/chat")
            .setAllowedOriginPatterns("*")
            .withSockJS();
    }
}
```

### 메시지 컨트롤러
```java
@Controller
public class ChatController {
    
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessage message, SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        
        // 메시지 저장
        ChatMessage savedMessage = chatService.saveMessage(message);
        
        // 채팅방 참가자들에게 실시간 전송
        messagingTemplate.convertAndSend("/topic/rooms/" + message.getRoomId(), savedMessage);
        
        // 알림 서비스에 이벤트 전송
        notificationService.sendChatNotification(savedMessage);
    }
    
    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload TypingMessage typingMessage) {
        messagingTemplate.convertAndSend(
            "/topic/rooms/" + typingMessage.getRoomId() + "/typing", 
            typingMessage
        );
    }
}
```

### 클라이언트 연결 관리
```java
@Component
public class WebSocketEventListener {
    
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        String userId = getUserIdFromSession(event);
        userStatusService.setUserOnline(userId);
        
        // 친구들에게 온라인 상태 알림
        notifyFriendsOnlineStatus(userId, UserStatus.ONLINE);
    }
    
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String userId = getUserIdFromSession(event);
        userStatusService.setUserOffline(userId);
        
        // 친구들에게 오프라인 상태 알림
        notifyFriendsOnlineStatus(userId, UserStatus.OFFLINE);
    }
}
```

## 📁 파일 공유

### 파일 업로드
```java
@PostMapping("/chat/rooms/{roomId}/upload")
public ResponseEntity<FileUploadResponse> uploadFile(
    @PathVariable String roomId,
    @RequestParam("file") MultipartFile file,
    @RequestParam(required = false) String caption) {
    
    // 파일 크기 및 형식 검증
    validateFile(file);
    
    // S3에 파일 업로드
    String fileUrl = s3Service.uploadChatFile(roomId, file);
    
    // 파일 메시지 생성 및 전송
    ChatMessage fileMessage = ChatMessage.builder()
        .roomId(roomId)
        .type(MessageType.FILE)
        .content(caption)
        .attachments(Arrays.asList(FileAttachment.builder()
            .name(file.getOriginalFilename())
            .url(fileUrl)
            .size(file.getSize())
            .mimeType(file.getContentType())
            .build()))
        .build();
        
    chatService.sendMessage(fileMessage);
    
    return ResponseEntity.ok(FileUploadResponse.of(fileUrl));
}
```

### 이미지 썸네일 생성
```java
@Service
public class ImageProcessingService {
    
    public String generateThumbnail(String originalImageUrl) {
        try {
            BufferedImage originalImage = ImageIO.read(new URL(originalImageUrl));
            BufferedImage thumbnail = Scalr.resize(originalImage, 200, 200);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(thumbnail, "jpg", baos);
            
            return s3Service.uploadThumbnail(baos.toByteArray());
        } catch (Exception e) {
            log.error("썸네일 생성 실패", e);
            return originalImageUrl;
        }
    }
}
```

## 🔍 메시지 검색

### 메시지 검색 API
```java
@GetMapping("/chat/search")
public ResponseEntity<PageResponse<MessageSearchResult>> searchMessages(
    @RequestParam String keyword,
    @RequestParam(required = false) String roomId,
    @RequestParam(required = false) String senderId,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
    @PageableDefault Pageable pageable) {
    
    MessageSearchQuery query = MessageSearchQuery.builder()
        .keyword(keyword)
        .roomId(roomId)
        .senderId(senderId)
        .fromDate(from)
        .toDate(to)
        .build();
        
    return ResponseEntity.ok(chatService.searchMessages(query, pageable));
}
```

### MongoDB 텍스트 검색
```java
@Repository
public class MessageRepository {
    
    public Page<Message> searchMessages(MessageSearchQuery query, Pageable pageable) {
        Criteria criteria = new Criteria();
        
        if (StringUtils.hasText(query.getKeyword())) {
            criteria.orOperator(
                Criteria.where("content").regex(query.getKeyword(), "i"),
                Criteria.where("attachments.name").regex(query.getKeyword(), "i")
            );
        }
        
        if (StringUtils.hasText(query.getRoomId())) {
            criteria.and("roomId").is(query.getRoomId());
        }
        
        Query mongoQuery = new Query(criteria).with(pageable);
        
        return PageableExecutionUtils.getPage(
            mongoTemplate.find(mongoQuery, Message.class),
            pageable,
            () -> mongoTemplate.count(Query.of(mongoQuery).limit(-1).skip(-1), Message.class)
        );
    }
}
```

## 🔄 서비스 간 통신

### User Service 연동
```java
@FeignClient(name = "user-service")
public interface UserServiceClient {
    
    @GetMapping("/users/{userId}/profile")
    UserProfile getUserProfile(@PathVariable String userId);
    
    @GetMapping("/users/batch")
    List<UserProfile> getUserProfiles(@RequestParam List<String> userIds);
}
```

### Notification Service 연동
```java
@Component
public class ChatNotificationHandler {
    
    @Async
    public void sendMessageNotification(ChatMessage message) {
        List<String> participants = chatRoomService.getParticipants(message.getRoomId());
        
        for (String participantId : participants) {
            if (!participantId.equals(message.getSenderId())) {
                NotificationRequest notification = NotificationRequest.builder()
                    .userId(participantId)
                    .type(NotificationType.CHAT_MESSAGE)
                    .title(message.getSenderName())
                    .message(message.getContent())
                    .data(Map.of(
                        "roomId", message.getRoomId(),
                        "messageId", message.getId()
                    ))
                    .build();
                    
                notificationService.sendNotification(notification);
            }
        }
    }
}
```

## 📊 실시간 상태 관리

### 온라인 상태 관리 (Redis)
```java
@Service
public class UserStatusService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String USER_STATUS_KEY = "user:status:";
    
    public void setUserOnline(String userId) {
        UserStatus status = UserStatus.builder()
            .userId(userId)
            .status(OnlineStatus.ONLINE)
            .lastSeen(Instant.now())
            .build();
            
        redisTemplate.opsForValue().set(
            USER_STATUS_KEY + userId, 
            status, 
            Duration.ofMinutes(5)
        );
    }
    
    public void updateUserActivity(String userId, String roomId) {
        String key = USER_STATUS_KEY + userId;
        UserStatus status = (UserStatus) redisTemplate.opsForValue().get(key);
        
        if (status != null) {
            status.setLastSeen(Instant.now());
            status.setCurrentRoomId(roomId);
            redisTemplate.opsForValue().set(key, status, Duration.ofMinutes(5));
        }
    }
}
```

### 타이핑 상태 관리
```java
@Service
public class TypingStatusService {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, ScheduledFuture<?>> typingTimers = new ConcurrentHashMap<>();
    
    public void startTyping(String roomId, String userId, String userName) {
        String key = roomId + ":" + userId;
        
        // 이전 타이머 취소
        ScheduledFuture<?> previousTimer = typingTimers.get(key);
        if (previousTimer != null) {
            previousTimer.cancel(false);
        }
        
        // 타이핑 시작 알림
        TypingMessage typingMessage = TypingMessage.builder()
            .roomId(roomId)
            .userId(userId)
            .userName(userName)
            .isTyping(true)
            .build();
            
        messagingTemplate.convertAndSend("/topic/rooms/" + roomId + "/typing", typingMessage);
        
        // 3초 후 자동으로 타이핑 중지
        ScheduledFuture<?> timer = taskScheduler.schedule(
            () -> stopTyping(roomId, userId, userName),
            Instant.now().plusSeconds(3)
        );
        
        typingTimers.put(key, timer);
    }
}
```

## 🧪 테스트

```bash
# 단위 테스트
./gradlew :modules:codin-chat-service:test

# WebSocket 통합 테스트
./gradlew :modules:codin-chat-service:websocketTest

# 부하 테스트 (동시 연결 1000개)
./gradlew :modules:codin-chat-service:loadTest
```

### WebSocket 테스트 예시
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ChatWebSocketTest {
    
    @Test
    public void testMessageSending() throws Exception {
        WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient());
        StompSession session = stompClient.connect(
            "ws://localhost:" + port + "/chat", 
            new StompSessionHandlerAdapter() {}
        ).get();
        
        BlockingQueue<ChatMessage> messageQueue = new ArrayBlockingQueue<>(1);
        
        session.subscribe("/topic/rooms/test-room", new StompFrameHandler() {
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                messageQueue.offer((ChatMessage) payload);
            }
        });
        
        ChatMessage message = ChatMessage.builder()
            .roomId("test-room")
            .content("Test message")
            .build();
            
        session.send("/app/chat.send", message);
        
        ChatMessage receivedMessage = messageQueue.poll(5, TimeUnit.SECONDS);
        assertThat(receivedMessage.getContent()).isEqualTo("Test message");
    }
}
```

## 📈 성능 최적화

### 메시지 페이징
- 최신 메시지부터 20개씩 로드
- 무한 스크롤 지원
- 이미지 지연 로딩

### Redis 캐싱
```java
@Service
public class ChatCacheService {
    
    @Cacheable(value = "chatRooms", key = "#userId")
    public List<ChatRoom> getUserChatRooms(String userId) {
        return chatRoomRepository.findByParticipantsUserId(userId);
    }
    
    @CacheEvict(value = "chatRooms", key = "#userId")
    public void evictUserChatRooms(String userId) {
        // 캐시 무효화
    }
}
```

### WebSocket 연결 최적화
- Heartbeat 간격: 30초
- 연결 타임아웃: 60초
- 최대 동시 연결: 10,000개

## 🔐 보안

### 메시지 암호화
- End-to-End 암호화 (향후 도입)
- 전송 중 암호화 (TLS)
- 저장 시 암호화 (MongoDB)

### 접근 제어
```java
@PreAuthorize("@chatSecurityService.canAccessRoom(#roomId, authentication.name)")
@GetMapping("/chat/rooms/{roomId}/messages")
public ResponseEntity<PageResponse<Message>> getMessages(@PathVariable String roomId) {
    // 메시지 조회 로직
}
```

### Rate Limiting
- 메시지 전송: 사용자당 1분에 60개
- 파일 업로드: 사용자당 1분에 10개
- 채팅방 생성: 사용자당 1일에 10개
