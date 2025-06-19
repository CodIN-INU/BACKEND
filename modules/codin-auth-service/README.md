# Auth Service - 인증/인가 서비스

## 📋 개요
CODIN 플랫폼의 인증 및 인가를 담당하는 마이크로서비스입니다.

## 🎯 주요 기능
- JWT 토큰 기반 인증
- OAuth2 소셜 로그인 (Google, Kakao)
- 사용자 권한 관리
- 세션 관리
- 비밀번호 암호화

## 🚀 API 엔드포인트

### 인증 관련
- `POST /auth/login` - 로그인
- `POST /auth/logout` - 로그아웃
- `POST /auth/refresh` - 토큰 갱신
- `GET /auth/me` - 현재 사용자 정보

### OAuth2 소셜 로그인
- `GET /auth/oauth2/google` - Google 로그인
- `GET /auth/oauth2/kakao` - Kakao 로그인

## 🔧 기술 스택
- Spring Boot 3.3.5
- Spring Security 6
- JWT
- OAuth2
- MongoDB
- Redis

## ⚙️ 환경 설정

```yaml
server:
  port: 8081

spring:
  application:
    name: auth-service
  data:
    mongodb:
      uri: mongodb://localhost:27017/codin_auth
  data:
    redis:
      host: localhost
      port: 6379

jwt:
  secret: ${JWT_SECRET:your-secret-key}
  expiration: 86400000 # 24 hours

oauth2:
  google:
    client-id: ${GOOGLE_CLIENT_ID}
    client-secret: ${GOOGLE_CLIENT_SECRET}
  kakao:
    client-id: ${KAKAO_CLIENT_ID}
    client-secret: ${KAKAO_CLIENT_SECRET}
```

## 📊 데이터 모델

### User Collection
```json
{
  "_id": "ObjectId",
  "email": "student@inu.ac.kr",
  "password": "encrypted_password",
  "roles": ["STUDENT", "USER"],
  "department": "COMPUTER_ENGINEERING",
  "createdAt": "2024-12-01T00:00:00Z",
  "lastLoginAt": "2024-12-01T10:30:00Z"
}
```

### Token Collection (Redis)
```json
{
  "accessToken": "jwt_access_token",
  "refreshToken": "jwt_refresh_token", 
  "userId": "user_id",
  "expiresAt": "2024-12-02T00:00:00Z"
}
```

## 🔐 보안 기능

### JWT 토큰 구조
```json
{
  "sub": "user123",
  "email": "student@inu.ac.kr",
  "roles": ["STUDENT", "USER"],
  "dept": "COMPUTER_ENGINEERING",
  "iat": 1640995200,
  "exp": 1641081600
}
```

### 권한 레벨
- `ADMIN`: 관리자 권한
- `PROFESSOR`: 교수 권한  
- `STUDENT`: 학생 권한
- `USER`: 기본 사용자 권한

## 🧪 테스트

```bash
# 단위 테스트 실행
./gradlew :modules:codin-auth-service:test

# 통합 테스트 실행
./gradlew :modules:codin-auth-service:integrationTest
```

## 📈 모니터링

### Health Check
- `GET /actuator/health`
- `GET /actuator/info`

### 메트릭
- 로그인 성공/실패 횟수
- 토큰 발급/갱신 횟수
- 활성 세션 수
