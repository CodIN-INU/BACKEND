# Content Service - 콘텐츠 관리 서비스

## 📋 개요
게시판, 댓글, 파일 업로드 등 콘텐츠 관련 기능을 담당하는 마이크로서비스입니다.

## 🎯 주요 기능
- 게시글 CRUD
- 댓글 시스템
- 파일 첨부 및 관리
- 좋아요/북마크
- 검색 및 필터링
- 카테고리 관리

## 🚀 API 엔드포인트

### 게시글 관리
- `GET /posts` - 게시글 목록 조회 (페이징, 필터링)
- `GET /posts/{postId}` - 게시글 상세 조회
- `POST /posts` - 게시글 작성
- `PUT /posts/{postId}` - 게시글 수정
- `DELETE /posts/{postId}` - 게시글 삭제

### 댓글 관리
- `GET /posts/{postId}/comments` - 댓글 목록 조회
- `POST /posts/{postId}/comments` - 댓글 작성
- `PUT /comments/{commentId}` - 댓글 수정
- `DELETE /comments/{commentId}` - 댓글 삭제

### 파일 관리
- `POST /posts/{postId}/attachments` - 파일 첨부
- `GET /posts/{postId}/attachments` - 첨부파일 목록
- `DELETE /attachments/{fileId}` - 파일 삭제

### 상호작용
- `POST /posts/{postId}/like` - 좋아요
- `DELETE /posts/{postId}/like` - 좋아요 취소
- `POST /posts/{postId}/bookmark` - 북마크
- `DELETE /posts/{postId}/bookmark` - 북마크 취소

## 🔧 기술 스택
- Spring Boot 3.3.5
- Spring Data MongoDB
- AWS S3 (파일 저장)
- Redis (캐싱, 조회수)
- Elasticsearch (검색 - 향후 도입)

## ⚙️ 환경 설정

```yaml
server:
  port: 8083

spring:
  application:
    name: content-service
  data:
    mongodb:
      uri: mongodb://localhost:27017/codin_content
  data:
    redis:
      host: localhost
      port: 6379

aws:
  s3:
    bucket: codin-attachments
    region: ap-northeast-2
    access-key: ${AWS_ACCESS_KEY}
    secret-key: ${AWS_SECRET_KEY}

file:
  upload:
    max-size: 10MB
    allowed-types: jpg,jpeg,png,gif,pdf,doc,docx,ppt,pptx,zip
```

## 📊 데이터 모델

### Post Collection
```json
{
  "_id": "ObjectId",
  "title": "Spring Boot MSA 도입기",
  "content": "마이크로서비스 아키텍처 도입 과정을 공유합니다...",
  "authorId": "user123",
  "authorName": "홍길동",
  "category": "TECH_SHARE",
  "tags": ["Spring Boot", "MSA", "Backend"],
  "attachments": [
    {
      "fileId": "file456",
      "fileName": "architecture.pdf",
      "fileSize": 1024000,
      "fileUrl": "https://s3.../file456.pdf"
    }
  ],
  "viewCount": 127,
  "likeCount": 15,
  "commentCount": 8,
  "bookmarkCount": 3,
  "isNotice": false,
  "isPinned": false,
  "status": "PUBLISHED",
  "createdAt": "2024-12-01T10:30:00Z",
  "updatedAt": "2024-12-01T15:20:00Z"
}
```

### Comment Collection
```json
{
  "_id": "ObjectId",
  "postId": "post123",
  "parentCommentId": null,
  "authorId": "user456",
  "authorName": "김철수",
  "content": "좋은 정보 감사합니다!",
  "likeCount": 3,
  "isDeleted": false,
  "createdAt": "2024-12-01T11:00:00Z",
  "updatedAt": "2024-12-01T11:00:00Z"
}
```

### Category Collection
```json
{
  "_id": "ObjectId",
  "code": "TECH_SHARE",
  "name": "기술 공유",
  "description": "개발 관련 기술과 경험을 공유하는 게시판",
  "color": "#3498db",
  "icon": "fa-code",
  "order": 1,
  "isActive": true
}
```

### Like/Bookmark Collection
```json
{
  "_id": "ObjectId",
  "userId": "user123",
  "targetId": "post456",
  "targetType": "POST", // POST, COMMENT
  "type": "LIKE", // LIKE, BOOKMARK
  "createdAt": "2024-12-01T12:00:00Z"
}
```

## 📂 파일 관리

### 파일 업로드 프로세스
1. 클라이언트에서 파일 업로드 요청
2. 파일 유효성 검증 (크기, 형식)
3. S3에 파일 업로드
4. 파일 정보 DB 저장
5. 게시글에 첨부파일 정보 추가

### 지원 파일 형식
- **이미지**: JPG, JPEG, PNG, GIF
- **문서**: PDF, DOC, DOCX, PPT, PPTX
- **압축**: ZIP, RAR
- **기타**: TXT, MD

### 파일 크기 제한
- 개별 파일: 최대 10MB
- 게시글당 총 첨부파일: 최대 50MB

## 🔍 검색 기능

### 기본 검색
- 제목 검색 (MongoDB 텍스트 인덱스)
- 내용 검색
- 작성자 검색
- 태그 검색

### 고급 검색 (향후 Elasticsearch 도입)
- 전문 검색
- 형태소 분석
- 검색어 자동완성
- 검색 결과 하이라이팅

```java
@GetMapping("/posts/search")
public ResponseEntity<PageResponse<PostSummary>> searchPosts(
    @RequestParam String keyword,
    @RequestParam(required = false) String category,
    @RequestParam(required = false) List<String> tags,
    @PageableDefault Pageable pageable) {
    
    return ResponseEntity.ok(postService.searchPosts(keyword, category, tags, pageable));
}
```

## 📊 조회수 관리

### Redis를 활용한 조회수 시스템
```java
@Service
public class ViewCountService {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    public void incrementViewCount(String postId, String userId) {
        String key = "view:" + postId + ":" + userId;
        String dailyKey = "daily_view:" + postId + ":" + LocalDate.now();
        
        // 중복 조회 방지 (1시간)
        if (redisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofHours(1))) {
            redisTemplate.opsForValue().increment("view_count:" + postId);
            redisTemplate.opsForValue().increment(dailyKey);
        }
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
    
    @PostMapping("/users/{userId}/activities")
    void recordActivity(@PathVariable String userId, @RequestBody ActivityRequest request);
}
```

### Notification Service 연동
```java
@FeignClient(name = "notification-service")
public interface NotificationServiceClient {
    
    @PostMapping("/notifications/post-liked")
    void sendPostLikedNotification(@RequestBody PostLikedEvent event);
    
    @PostMapping("/notifications/comment-added")
    void sendCommentAddedNotification(@RequestBody CommentAddedEvent event);
}
```

## 🧪 테스트

```bash
# 단위 테스트
./gradlew :modules:codin-content-service:test

# 통합 테스트  
./gradlew :modules:codin-content-service:integrationTest

# 파일 업로드 테스트
./gradlew :modules:codin-content-service:fileUploadTest
```

## 📈 성능 최적화

### 캐싱 전략
- 인기 게시글: 1시간 캐싱
- 카테고리 목록: 24시간 캐싱
- 조회수: Redis 실시간 처리
- 댓글 수: 30분 캐싱

### 페이징 최적화
```java
@GetMapping("/posts")
public ResponseEntity<PageResponse<PostSummary>> getPosts(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size,
    @RequestParam(defaultValue = "createdAt") String sort,
    @RequestParam(defaultValue = "desc") String direction) {
    
    Pageable pageable = PageRequest.of(page, size, 
        Sort.by(Sort.Direction.fromString(direction), sort));
    
    return ResponseEntity.ok(postService.getPosts(pageable));
}
```

### 데이터베이스 인덱스
- `{ "authorId": 1, "createdAt": -1 }` - 사용자별 게시글 조회
- `{ "category": 1, "createdAt": -1 }` - 카테고리별 게시글 조회
- `{ "tags": 1, "createdAt": -1 }` - 태그별 게시글 조회
- `{ "title": "text", "content": "text" }` - 전문 검색

## 🔐 보안 및 권한

### 게시글 권한
- 작성자만 수정/삭제 가능
- 관리자는 모든 게시글 관리 가능
- 신고된 게시글 자동 숨김 처리

### 파일 보안
- 업로드 파일 바이러스 검사 (향후 도입)
- 파일 접근 권한 제어
- 임시 URL 생성 (S3 Pre-signed URL)

### Rate Limiting
- 게시글 작성: 사용자당 1분에 5개
- 댓글 작성: 사용자당 1분에 10개
- 파일 업로드: 사용자당 1분에 10개
