# CODIN MSA 리팩토링 - 의존성 분석 및 클래스 이동 계획

## 1. 공통 라이브러리 (codin-common)로 이동할 클래스들

### 공통 응답 및 예외 처리
- `inu.codin.codin.common.response.*` - 모든 응답 클래스
- `inu.codin.codin.common.exception.*` - 공통 예외 처리
- `inu.codin.codin.common.dto.BaseTimeEntity` - 기본 엔티티
- `inu.codin.codin.common.dto.Department` - 학과 enum

### 공통 유틸리티
- `inu.codin.codin.common.util.*` - 공통 유틸리티 클래스
- `inu.codin.codin.common.ratelimit.*` - Rate Limiting 관련

### 공통 S3 서비스
- `inu.codin.codin.infra.s3.*` - S3 관련 클래스들

## 2. 인증 서비스 (codin-auth-service)로 이동할 클래스들

### 보안 관련
- `inu.codin.codin.common.security.*` - 모든 보안 관련 클래스
  - JWT 서비스 및 필터
  - OAuth2 설정 및 핸들러
  - 사용자 인증 서비스

### 사용자 인증 관련
- `inu.codin.codin.domain.user.security.*` - 사용자 인증 관련
- OAuth2 관련 컨트롤러 및 서비스

## 3. 사용자 서비스 (codin-user-service)로 이동할 클래스들

### 사용자 관리
- `inu.codin.codin.domain.user.*` (보안 관련 제외)
  - UserController, UserService
  - UserEntity, UserRepository
  - 사용자 프로필 관리

### 차단 기능
- `inu.codin.codin.domain.block.*` - 사용자 차단 기능

## 4. 콘텐츠 서비스 (codin-content-service)로 이동할 클래스들

### 게시판 관련
- `inu.codin.codin.domain.post.*` - 게시물 관련 모든 클래스
  - PostController, PostService
  - CommentController, CommentService
  - ReplyCommentController, ReplyCommentService

### 좋아요 및 스크랩
- `inu.codin.codin.domain.like.*` - 좋아요 기능
- `inu.codin.codin.domain.scrap.*` - 스크랩 기능

### 강의 및 정보
- `inu.codin.codin.domain.lecture.*` - 강의 정보
- `inu.codin.codin.domain.info.*` - 학과 정보 (교수, 연구실, 사무실)

### 신고 기능
- `inu.codin.codin.domain.report.*` - 신고 기능

## 5. 알림 서비스 (codin-notification-service)로 이동할 클래스들

### 알림 관련
- `inu.codin.codin.domain.notification.*` - 알림 기능
- `inu.codin.codin.infra.fcm.*` - FCM 푸시 알림

### 이메일 서비스
- `inu.codin.codin.domain.email.*` - 이메일 발송 기능

## 6. 채팅 서비스 (codin-chat-service)로 이동할 클래스들

### 채팅 관련
- `inu.codin.codin.domain.chat.*` - 모든 채팅 관련 클래스
  - ChatRoomController, ChatRoomService
  - ChattingController, ChattingService
  - 채팅 엔티티 및 리포지토리

### WebSocket 설정
- `inu.codin.codin.common.stomp.*` - STOMP 메시지 처리
- WebSocket 관련 설정

## 7. API 게이트웨이 (codin-api-gateway)

### 새로 생성할 클래스들
- GatewayConfig - 라우팅 설정
- AuthenticationFilter - JWT 토큰 검증
- RateLimitingFilter - 속도 제한
- CorsConfig - CORS 설정

## 8. 서비스 간 통신을 위한 DTO 클래스들

각 서비스는 다른 서비스와 통신하기 위한 별도의 DTO를 정의해야 합니다:

### 인증 서비스 DTO
- UserAuthDto - 사용자 인증 정보
- TokenValidationDto - 토큰 검증 결과

### 사용자 서비스 DTO
- UserProfileDto - 사용자 프로필 정보
- UserBasicInfoDto - 기본 사용자 정보

### 콘텐츠 서비스 DTO
- PostSummaryDto - 게시물 요약 정보
- CommentNotificationDto - 댓글 알림용 정보

## 9. 공통 설정 클래스 분리

### 각 서비스별 설정
- DatabaseConfig (MongoDB 설정)
- RedisConfig (Redis 설정)
- SwaggerConfig (API 문서화)
- SecurityConfig (각 서비스별 보안 설정)

## 10. 의존성 해결 전략

### 1단계: 공통 라이브러리 생성
- 모든 공통 클래스를 codin-common으로 이동
- 각 서비스에서 공통 라이브러리 의존성 추가

### 2단계: 인증 서비스 분리
- 보안 관련 모든 클래스 이동
- JWT 토큰 발급 및 검증 로직 독립화

### 3단계: 도메인 서비스 분리
- 각 도메인별로 순차적 분리
- 서비스 간 통신 인터페이스 정의

### 4단계: API 게이트웨이 구현
- 모든 외부 요청의 진입점 역할
- 인증, 라우팅, Rate Limiting 처리
