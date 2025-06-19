# User Service - 사용자 관리 서비스

## 📋 개요
사용자 프로필, 개인정보, 학과 정보 등을 관리하는 마이크로서비스입니다.

## 🎯 주요 기능
- 사용자 프로필 관리
- 프로필 이미지 업로드
- 학과 정보 관리
- 사용자 활동 이력
- 이메일 인증

## 🚀 API 엔드포인트

### 사용자 관리
- `GET /users/{userId}` - 사용자 정보 조회
- `PUT /users/{userId}` - 사용자 정보 수정
- `DELETE /users/{userId}` - 사용자 삭제
- `GET /users/{userId}/profile` - 프로필 조회

### 프로필 관리
- `POST /users/{userId}/profile/image` - 프로필 이미지 업로드
- `PUT /users/{userId}/profile` - 프로필 수정
- `GET /users/{userId}/activities` - 활동 이력 조회

### 학과 정보
- `GET /departments` - 학과 목록 조회
- `GET /departments/{deptId}` - 학과 상세 정보

## 🔧 기술 스택
- Spring Boot 3.3.5
- Spring Data MongoDB
- AWS S3 (파일 업로드)
- Redis (캐싱)
- OpenFeign (서비스 간 통신)

## ⚙️ 환경 설정

```yaml
server:
  port: 8082

spring:
  application:
    name: user-service
  data:
    mongodb:
      uri: mongodb://localhost:27017/codin_user
  data:
    redis:
      host: localhost
      port: 6379

aws:
  s3:
    bucket: codin-profile-images
    region: ap-northeast-2
    access-key: ${AWS_ACCESS_KEY}
    secret-key: ${AWS_SECRET_KEY}

email:
  smtp:
    host: smtp.gmail.com
    port: 587
    username: ${EMAIL_USERNAME}
    password: ${EMAIL_PASSWORD}
```

## 📊 데이터 모델

### User Profile Collection
```json
{
  "_id": "ObjectId",
  "userId": "user123",
  "studentId": "202012345",
  "name": "홍길동",
  "email": "student@inu.ac.kr",
  "department": "COMPUTER_ENGINEERING",
  "grade": 3,
  "profileImageUrl": "https://s3.../profile.jpg",
  "bio": "컴퓨터공학과 3학년입니다.",
  "interests": ["Java", "Spring", "React"],
  "socialLinks": {
    "github": "https://github.com/username",
    "blog": "https://blog.com/username"
  },
  "emailVerified": true,
  "createdAt": "2024-12-01T00:00:00Z",
  "updatedAt": "2024-12-01T10:30:00Z"
}
```

### Department Collection
```json
{
  "_id": "ObjectId",
  "code": "COMPUTER_ENGINEERING",
  "name": "컴퓨터공학과",
  "college": "정보기술대학",
  "description": "컴퓨터공학과 소개...",
  "website": "https://computer.inu.ac.kr",
  "contactEmail": "computer@inu.ac.kr"
}
```

### Activity Collection
```json
{
  "_id": "ObjectId", 
  "userId": "user123",
  "type": "POST_CREATED",
  "description": "새 게시글을 작성했습니다",
  "metadata": {
    "postId": "post456",
    "title": "Spring Boot 게시글"
  },
  "createdAt": "2024-12-01T10:30:00Z"
}
```

## 📂 파일 업로드

### 프로필 이미지 업로드
- 지원 형식: JPG, PNG, GIF
- 최대 크기: 5MB
- 자동 리사이징: 200x200px
- S3 저장소 사용

```java
@PostMapping("/users/{userId}/profile/image")
public ResponseEntity<String> uploadProfileImage(
    @PathVariable String userId,
    @RequestParam("file") MultipartFile file) {
    
    String imageUrl = s3Service.uploadProfileImage(userId, file);
    userService.updateProfileImage(userId, imageUrl);
    
    return ResponseEntity.ok(imageUrl);
}
```

## 📧 이메일 서비스

### 이메일 인증
- 회원가입 시 인증 이메일 발송
- 6자리 인증 코드 생성
- 10분 유효시간
- Redis에 인증 코드 저장

### 이메일 템플릿
- 회원가입 인증
- 비밀번호 재설정
- 프로필 변경 알림

## 🔄 서비스 간 통신

### Auth Service 연동
```java
@FeignClient(name = "auth-service")
public interface AuthServiceClient {
    @GetMapping("/auth/validate/{token}")
    ValidationResponse validateToken(@PathVariable String token);
}
```

### Content Service 연동
```java
@FeignClient(name = "content-service")
public interface ContentServiceClient {
    @GetMapping("/posts/user/{userId}/count")
    Integer getUserPostCount(@PathVariable String userId);
}
```

## 🧪 테스트

```bash
# 단위 테스트
./gradlew :modules:codin-user-service:test

# 통합 테스트
./gradlew :modules:codin-user-service:integrationTest

# S3 업로드 테스트
./gradlew :modules:codin-user-service:s3Test
```

## 📈 모니터링

### Health Check
- `GET /actuator/health`
- S3 연결 상태 확인
- MongoDB 연결 상태 확인
- Email SMTP 연결 상태 확인

### 캐싱 전략
- 사용자 프로필: 1시간 캐싱
- 학과 정보: 24시간 캐싱
- 활동 이력: 30분 캐싱

## 🔐 보안 고려사항

### 데이터 보호
- 개인정보 암호화 저장
- 프로필 이미지 접근 권한 제어
- 이메일 주소 마스킹 처리

### API 보안
- JWT 토큰 검증
- 사용자 본인만 정보 수정 가능
- Rate Limiting 적용
