# CODIN Microservices Architecture (MSA)

## 📋 프로젝트 개요

CODIN 백엔드를 모놀리스 아키텍처에서 마이크로서비스 아키텍처(MSA)로 전환한 프로젝트입니다. 
기존 단일 애플리케이션을 6개의 독립적인 마이크로서비스로 분리하여 확장성, 유지보수성, 배포 유연성을 향상시켰습니다.

## 🏗️ 아키텍처 구조

```
┌─────────────────────────────────────────────────────────────┐
│                    API Gateway (8080)                       │
│                 Spring Cloud Gateway                        │
└─────────────────────────┬───────────────────────────────────┘
                          │
          ┌───────────────┼───────────────┐
          │               │               │
          ▼               ▼               ▼
    ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
    │Auth Service │ │User Service │ │Content Svc  │
    │   (8081)    │ │   (8082)    │ │   (8083)    │
    └─────────────┘ └─────────────┘ └─────────────┘
          │               │               │
          └───────────────┼───────────────┘
                          │
          ┌───────────────┼───────────────┐
          │               │               │
          ▼               ▼               ▼
    ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
    │Notification │ │Chat Service │ │Service Reg. │
    │   (8084)    │ │   (8085)    │ │   (8761)    │
    └─────────────┘ └─────────────┘ └─────────────┘
          │               │               │
          └───────────────┼───────────────┘
                          │
    ┌─────────────────────┼─────────────────────┐
    │                     │                     │
    ▼                     ▼                     ▼
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  MongoDB    │     │    Redis    │     │Common Library│
│   (27017)   │     │   (6379)    │     │             │
└─────────────┘     └─────────────┘     └─────────────┘
```

## 🚀 마이크로서비스 구성

### 1. API Gateway (Port: 8080)
- **역할**: 모든 외부 요청의 진입점, 라우팅, 인증, CORS 처리
- **기술**: Spring Cloud Gateway
- **주요 기능**:
  - 서비스별 라우팅
  - JWT 토큰 인증 필터
  - 부하 분산
  - CORS 설정

### 2. Auth Service (Port: 8081)
- **역할**: 사용자 인증 및 권한 관리
- **데이터베이스**: MongoDB (codin-auth)
- **주요 기능**:
  - 로그인/로그아웃
  - 회원가입
  - JWT 토큰 발급/갱신
  - 패스워드 재설정

### 3. User Service (Port: 8082)
- **역할**: 사용자 정보 및 프로필 관리
- **데이터베이스**: MongoDB (codin-user)
- **주요 기능**:
  - 사용자 프로필 CRUD
  - 팔로우/언팔로우
  - 사용자 검색
  - 사용자 상태 관리

### 4. Content Service (Port: 8083)
- **역할**: 게시글, 댓글, 미디어 컨텐츠 관리
- **데이터베이스**: MongoDB (codin-content), Redis (캐싱)
- **주요 기능**:
  - 게시글 CRUD
  - 댓글 관리
  - 좋아요/북마크
  - 이미지 업로드 (S3)

### 5. Notification Service (Port: 8084)
- **역할**: 알림 및 메시지 발송 관리
- **데이터베이스**: MongoDB (codin-notification), Redis
- **주요 기능**:
  - 푸시 알림 (FCM)
  - 이메일 알림
  - 알림 설정 관리
  - 알림 히스토리

### 6. Chat Service (Port: 8085)
- **역할**: 실시간 채팅 기능
- **데이터베이스**: MongoDB (codin-chat), Redis
- **주요 기능**:
  - 실시간 채팅 (WebSocket)
  - 채팅방 관리
  - 메시지 히스토리
  - 파일 공유

### 7. Common Library
- **역할**: 모든 서비스에서 공통으로 사용하는 라이브러리
- **포함 요소**:
  - 공통 응답 클래스 (SingleResponse, ListResponse)
  - 기본 엔티티 클래스 (BaseTimeEntity)
  - 공통 DTO 및 ENUM
  - 유틸리티 클래스

## 🛠️ 기술 스택

### Backend Framework
- **Spring Boot 3.1.0**: 마이크로서비스 기반 프레임워크
- **Spring Cloud Gateway**: API 게이트웨이
- **Spring Security**: 보안 및 인증
- **Spring Data MongoDB**: NoSQL 데이터베이스 연동
- **Spring Data Redis**: 캐싱 및 세션 관리

### Database & Cache
- **MongoDB 6.0**: 메인 데이터베이스 (서비스별 분리)
- **Redis 7**: 캐싱 및 세션 저장소

### Service Discovery & Configuration
- **Eureka Server**: 서비스 등록 및 발견
- **Environment Variables**: 설정 관리

### Build & DevOps
- **Gradle Multi-Module**: 빌드 시스템
- **Docker & Docker Compose**: 컨테이너화
- **GitHub Actions**: CI/CD 파이프라인

## 🚀 Quick Start

### 1. 환경 설정

```bash
# 저장소 클론
git clone <repository-url>
cd BACKEND

# 환경 변수 파일 생성
cp .env.example .env.local

# 환경 변수 설정 (필수)
# .env.local 파일을 편집하여 필요한 값들을 설정하세요
```

### 2. Docker를 사용한 실행 (권장)

```bash
# Docker 관리 스크립트 사용
./scripts/docker-manager.sh up

# 또는 직접 Docker Compose 사용
docker-compose up -d

# 서비스 상태 확인
./scripts/docker-manager.sh status

# 헬스체크
./scripts/docker-manager.sh health
```

### 3. 개발 환경에서 실행

```bash
# 전체 빌드
./gradlew clean build

# 개발 스크립트 사용
./scripts/dev-runner.sh start-all

# 또는 개별 서비스 실행
./scripts/dev-runner.sh start auth-service
./scripts/dev-runner.sh start user-service
# ... 기타 서비스들
```

## 📊 서비스 엔드포인트

### API Gateway: http://localhost:8080
- **Auth**: `/api/auth/**` → Auth Service
- **Users**: `/api/users/**` → User Service  
- **Posts**: `/api/posts/**` → Content Service
- **Comments**: `/api/comments/**` → Content Service
- **Notifications**: `/api/notifications/**` → Notification Service
- **Chat**: `/api/chatroom/**`, `/api/chats/**` → Chat Service

### 직접 접근 (개발용)
- **Auth Service**: http://localhost:8081
- **User Service**: http://localhost:8082
- **Content Service**: http://localhost:8083
- **Notification Service**: http://localhost:8084
- **Chat Service**: http://localhost:8085
- **Eureka Dashboard**: http://localhost:8761

## 🔧 환경 변수 관리

### 파일 구조
```
├── .env.example          # 환경 변수 템플릿
├── .env.local           # 로컬 개발 환경 변수
├── config/
│   ├── auth-service.env     # Auth 서비스 전용 변수
│   ├── user-service.env     # User 서비스 전용 변수
│   ├── content-service.env  # Content 서비스 전용 변수
│   ├── notification-service.env # Notification 서비스 전용 변수
│   ├── chat-service.env     # Chat 서비스 전용 변수
│   └── api-gateway.env      # API Gateway 전용 변수
```

### 주요 환경 변수

```bash
# 데이터베이스
MONGODB_URI=mongodb://localhost:27017/codin-{service}
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# JWT 설정
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

# AWS S3 (Content Service)
AWS_S3_BUCKET=your-bucket
AWS_S3_REGION=ap-northeast-2
AWS_ACCESS_KEY=your-access-key
AWS_SECRET_KEY=your-secret-key

# 이메일 설정 (Notification Service)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email
MAIL_PASSWORD=your-app-password

# FCM 설정 (Notification Service)
FCM_SERVICE_ACCOUNT_KEY=your-fcm-key

# CORS 설정
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:8080
```

## 🏃‍♂️ 개발 가이드

### 새로운 API 추가

1. **적절한 서비스 선택**: 비즈니스 도메인에 따라 서비스 결정
2. **Controller 추가**: 해당 서비스의 controller 패키지에 추가
3. **Service 계층 구현**: 비즈니스 로직 구현
4. **Repository 추가**: 데이터 접근 계층
5. **API Gateway 라우팅**: 필요시 라우팅 규칙 추가

### 서비스 간 통신

```java
// Feign Client 사용 예시
@FeignClient(name = "user-service")
public interface UserServiceClient {
    @GetMapping("/users/{userId}")
    SingleResponse<UserDto> getUser(@PathVariable Long userId);
}
```

### 공통 응답 형식

```java
// 단일 데이터 응답
return SingleResponse.success(data);

// 목록 데이터 응답
return ListResponse.success(dataList, totalCount);

// 에러 응답
return SingleResponse.error("에러 메시지");
```

## 📝 API 문서

각 서비스별 API 문서는 다음 위치에서 확인할 수 있습니다:

- **Swagger UI**: 각 서비스 포트 + `/swagger-ui.html`
- **API Docs**: 각 서비스 포트 + `/v3/api-docs`

예시:
- Auth Service: http://localhost:8081/swagger-ui.html
- User Service: http://localhost:8082/swagger-ui.html

## 🧪 테스트

### 전체 테스트 실행
```bash
./gradlew test
```

### 서비스별 테스트
```bash
./gradlew :modules:codin-auth-service:test
./gradlew :modules:codin-user-service:test
```

### 통합 테스트
```bash
./scripts/health-check.sh
```

## 🚀 배포

### Docker를 사용한 배포

```bash
# 프로덕션 환경 빌드 및 배포
SPRING_PROFILES_ACTIVE=production ./scripts/docker-manager.sh up

# 스케일링
./scripts/docker-manager.sh scale user-service 3
```

### CI/CD Pipeline

GitHub Actions를 통한 자동 배포:

1. **테스트**: Pull Request 시 자동 테스트 실행
2. **빌드**: main/develop 브랜치 푸시 시 Docker 이미지 빌드
3. **배포**: 
   - develop → staging 환경
   - main → production 환경

## 🔍 모니터링

### 헬스체크 엔드포인트
- 모든 서비스: `/api/health`
- 상세 정보: `/actuator/health`

### 로그 확인
```bash
# 전체 서비스 로그
./scripts/docker-manager.sh logs

# 특정 서비스 로그
./scripts/docker-manager.sh logs auth-service

# 실시간 로그 모니터링
docker-compose logs -f
```

### 메트릭스
- **Actuator**: 각 서비스의 `/actuator/metrics`
- **Eureka Dashboard**: http://localhost:8761

## 🔒 보안

### JWT 토큰 기반 인증
- Access Token: 1시간 (기본값)
- Refresh Token: 7일 (기본값)

### API Gateway 보안 필터
- 인증이 필요한 엔드포인트 자동 필터링
- CORS 설정을 통한 Cross-Origin 요청 제어

### 환경 변수 보안
- 민감한 정보는 환경 변수로 관리
- GitHub Secrets를 통한 CI/CD 보안

## 🛠️ 트러블슈팅

### 일반적인 문제들

#### 1. 서비스 시작 실패
```bash
# 로그 확인
./scripts/docker-manager.sh logs <service-name>

# 포트 충돌 확인
lsof -i :8081
```

#### 2. 데이터베이스 연결 실패
```bash
# MongoDB 상태 확인
docker-compose exec mongodb mongo --eval "db.runCommand('ping')"

# Redis 상태 확인
docker-compose exec redis redis-cli ping
```

#### 3. Eureka 등록 실패
- Eureka Server 실행 상태 확인
- 네트워크 연결 확인
- 환경 변수 설정 확인

## 📚 참고 자료

- [Spring Cloud Gateway 문서](https://spring.io/projects/spring-cloud-gateway)
- [Spring Boot 문서](https://spring.io/projects/spring-boot)
- [Docker Compose 문서](https://docs.docker.com/compose/)
- [MongoDB 문서](https://docs.mongodb.com/)
- [Redis 문서](https://redis.io/documentation)

## 🤝 기여 가이드

1. Feature 브랜치 생성
2. 변경사항 구현
3. 테스트 추가/수정
4. Pull Request 생성
5. 코드 리뷰 후 머지

## 📞 지원

문제가 발생하거나 질문이 있으시면 이슈를 생성해 주세요.

---

**CODIN Team** - Microservices Architecture Migration Project
