# 인증/인가 서버 분리 상세 계획

## 1. 개요

현재 모놀리스 애플리케이션의 인증/인가 기능을 독립적인 마이크로서비스로 분리합니다. 이는 MSA 전환의 첫 번째이자 가장 중요한 단계입니다.

## 2. 인증 서비스 아키텍처

### 2.1 서비스 구성요소

```
codin-auth-service/
├── controller/
│   ├── AuthController.java           # 인증 API 엔드포인트
│   └── InternalAuthController.java   # 서비스 간 통신용 내부 API
├── service/
│   ├── JwtService.java              # JWT 토큰 관리
│   ├── OAuth2AuthService.java       # OAuth2 인증 처리
│   ├── UserAuthService.java         # 사용자 인증 비즈니스 로직
│   └── TokenValidationService.java  # 토큰 검증 서비스
├── security/
│   ├── config/SecurityConfig.java   # 보안 설정
│   ├── filter/JwtAuthFilter.java    # JWT 필터
│   └── handler/OAuth2Handlers.java  # OAuth2 성공/실패 핸들러
├── entity/
│   ├── UserEntity.java             # 사용자 엔티티 (인증 관련만)
│   └── RefreshTokenEntity.java     # 리프레시 토큰 엔티티
└── repository/
    ├── UserRepository.java         # 사용자 리포지토리
    └── RefreshTokenRepository.java # 리프레시 토큰 리포지토리
```

### 2.2 데이터베이스 스키마

#### users 컬렉션 (인증 정보만)
```json
{
  "_id": "ObjectId",
  "email": "string",
  "password": "string (optional for OAuth)",
  "role": "USER|ADMIN|MANAGER",
  "status": "ACTIVE|DISABLED|SUSPENDED",
  "provider": "GOOGLE|APPLE|LOCAL",
  "providerId": "string",
  "createdAt": "Date",
  "updatedAt": "Date"
}
```

#### refresh_tokens 컬렉션
```json
{
  "_id": "ObjectId",
  "userId": "ObjectId",
  "token": "string",
  "expiryDate": "Date",
  "createdAt": "Date"
}
```

## 3. API 설계

### 3.1 외부 API (클라이언트용)

#### 인증 관련 API
```
POST /auth/login
POST /auth/logout
POST /auth/signup
POST /auth/refresh
GET  /auth/google
GET  /auth/apple
```

#### OAuth2 콜백
```
GET /oauth2/callback/google
GET /oauth2/callback/apple
```

### 3.2 내부 API (서비스 간 통신용)

#### 토큰 검증 API
```
POST /internal/auth/validate
Request: { "token": "jwt_token" }
Response: { 
  "valid": true,
  "userId": "user_id",
  "email": "user@example.com",
  "role": "USER",
  "expiresAt": "2024-01-01T00:00:00Z"
}
```

#### 사용자 기본 정보 API
```
GET /internal/users/{userId}
Response: {
  "id": "user_id",
  "email": "user@example.com",
  "role": "USER",
  "status": "ACTIVE"
}
```

## 4. 주요 구현 클래스

### 4.1 AuthController.java
```java
@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request);
    
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request);
    
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request);
    
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody SignupRequest request);
}
```

### 4.2 InternalAuthController.java
```java
@RestController
@RequestMapping("/internal/auth")
public class InternalAuthController {
    
    @PostMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(@RequestBody TokenValidationRequest request);
    
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserBasicInfo> getUserBasicInfo(@PathVariable String userId);
}
```

### 4.3 JwtService.java
```java
@Service
public class JwtService {
    
    public String generateAccessToken(UserEntity user);
    public String generateRefreshToken(UserEntity user);
    public boolean validateToken(String token);
    public Claims extractClaims(String token);
    public void blacklistToken(String token);
}
```

## 5. 보안 설정

### 5.1 JWT 설정
- **Access Token**: 1시간 유효기간
- **Refresh Token**: 7일 유효기간, Redis에 저장
- **알고리즘**: RS256 (비대칭 키 사용)

### 5.2 OAuth2 설정
- Google OAuth2 연동
- Apple OAuth2 연동
- 도메인 제한: @inu.ac.kr

### 5.3 보안 헤더
```yaml
security:
  headers:
    frame-options: DENY
    content-type-options: nosniff
    xss-protection: "1; mode=block"
```

## 6. 설정 파일

### 6.1 application.yml
```yaml
spring:
  application:
    name: codin-auth-service
  data:
    mongodb:
      uri: ${MONGODB_URI:mongodb://localhost:27017/codin-auth}
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope: profile,email
          apple:
            client-id: ${APPLE_CLIENT_ID}
            client-secret: ${APPLE_CLIENT_SECRET}

jwt:
  secret: ${JWT_SECRET}
  access-token:
    expiration: 3600000  # 1 hour
  refresh-token:
    expiration: 604800000  # 7 days

server:
  port: 8081

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

## 7. 이주 전략

### 7.1 1단계: 기본 구조 생성
- 프로젝트 구조 생성
- 기본 의존성 설정
- MongoDB 및 Redis 연결 설정

### 7.2 2단계: 인증 로직 이동
- JWT 관련 클래스 이동
- OAuth2 설정 이동
- 사용자 엔티티 및 리포지토리 이동

### 7.3 3단계: API 구현
- 외부 API 구현
- 내부 API 구현
- 토큰 검증 로직 구현

### 7.4 4단계: 통합 테스트
- API 게이트웨이와 연동 테스트
- 다른 서비스와의 통신 테스트
- 보안 테스트

## 8. 모니터링 및 로깅

### 8.1 로그 설정
```yaml
logging:
  level:
    inu.codin.auth: DEBUG
    org.springframework.security: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level [%logger{36}] - %msg%n"
```

### 8.2 메트릭 수집
- 로그인 성공/실패 횟수
- 토큰 발급 횟수
- OAuth2 인증 시도 횟수
- 응답 시간 모니터링

## 9. 장애 대응

### 9.1 Circuit Breaker 설정
- 외부 OAuth2 서비스 호출 시 Circuit Breaker 적용
- 타임아웃: 5초
- 실패 임계값: 5회

### 9.2 Fallback 메커니즘
- OAuth2 서비스 장애 시 적절한 에러 메시지 반환
- 토큰 검증 서비스 장애 시 기본 검증 로직 활용

## 10. 성능 최적화

### 10.1 캐싱 전략
- JWT 공개키 캐싱 (Redis)
- 사용자 기본 정보 캐싱 (Redis, TTL: 30분)
- OAuth2 토큰 정보 캐싱

### 10.2 데이터베이스 최적화
- 이메일 필드 인덱스 생성
- providerId 필드 인덱스 생성
- 복합 인덱스 활용

## 11. 테스트 전략

### 11.1 단위 테스트
- JWT 토큰 생성/검증 테스트
- OAuth2 인증 플로우 테스트
- 사용자 인증 로직 테스트

### 11.2 통합 테스트
- API 엔드포인트 테스트
- 데이터베이스 연동 테스트
- Redis 연동 테스트

### 11.3 보안 테스트
- JWT 토큰 위조 시도 테스트
- OAuth2 공격 시나리오 테스트
- 권한 부여 테스트
