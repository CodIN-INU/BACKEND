# MSA 완전 가이드 - CODIN Backend

## 📋 목차
- [🎯 MSA 개요](#-msa-개요)
- [🏗️ Spring Cloud Gateway](#️-spring-cloud-gateway)
- [🔍 Eureka Service Discovery](#-eureka-service-discovery)
- [⚡ 서비스 간 통신](#-서비스-간-통신)
- [💾 데이터 관리](#-데이터-관리)
- [🔐 보안 및 인증](#-보안-및-인증)
- [🚀 배포 및 운영](#-배포-및-운영)

## 🎯 MSA 개요

### 🔄 전환 배경
CODIN 프로젝트는 기존 모놀리스 아키텍처의 한계를 극복하기 위해 MSA로 전환하고 있습니다.

**모놀리스의 문제점:**
- 단일 장애점으로 인한 전체 시스템 다운
- 부분적 확장의 어려움
- 기술 스택 변경의 제약

**MSA의 장점:**
- 서비스별 독립적 개발/배포
- 기술 스택 다양성
- 확장성 및 성능 최적화
- 장애 격리

### 🏗️ CODIN MSA 구조

```
┌─────────────────────────────────────────────────────────────┐
│                     Client Layer                           │
│              (Web, Mobile, Desktop)                        │
└─────────────────┬───────────────────────────────────────────┘
                  │
┌─────────────────┼───────────────────────────────────────────┐
│                 │         API Gateway Layer               │
│    ┌────────────┼─────────────────────────────────────┐    │
│    │  Spring Cloud Gateway (:8080)                   │    │
│    │  - Authentication                                │    │
│    │  - Rate Limiting                                 │    │
│    │  - Request/Response Logging                      │    │
│    │  - Load Balancing                                │    │
│    └────────────┼─────────────────────────────────────┘    │
└─────────────────┼───────────────────────────────────────────┘
                  │
┌─────────────────┼───────────────────────────────────────────┐
│                 │     Service Discovery Layer              │
│    ┌────────────┼─────────────────────────────────────┐    │
│    │  Eureka Server (:8761)                          │    │
│    │  - Service Registration                          │    │
│    │  - Health Monitoring                             │    │
│    │  - Load Balancing Info                           │    │
│    └────────────┼─────────────────────────────────────┘    │
└─────────────────┼───────────────────────────────────────────┘
                  │
┌─────────────────┼───────────────────────────────────────────┐
│                 │        Business Logic Layer             │
│  ┌──────────────┴─────────────────────────────────────┐   │
│  │                Service Mesh                         │   │
│  │                                                     │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  │   │
│  │  │Auth Service │  │User Service │  │Content Svc  │  │   │
│  │  │   (:8081)   │  │   (:8082)   │  │   (:8083)   │  │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘  │   │
│  │                                                     │   │
│  │  ┌─────────────┐  ┌─────────────┐                   │   │
│  │  │Notification │  │Chat Service │                   │   │
│  │  │   (:8084)   │  │   (:8085)   │                   │   │
│  │  └─────────────┘  └─────────────┘                   │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────┬───────────────────────────────────────────┘
                  │
┌─────────────────┼───────────────────────────────────────────┐
│                 │         Data Layer                      │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                Data Management                       │   │
│  │                                                     │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  │   │
│  │  │  MongoDB    │  │    Redis    │  │   AWS S3    │  │   │
│  │  │  (:27017)   │  │   (:6379)   │  │             │  │   │
│  │  │             │  │             │  │             │  │   │
│  │  │ Document    │  │ Session &   │  │ File        │  │   │
│  │  │ Storage     │  │ Cache       │  │ Storage     │  │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘  │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

## 🏗️ Spring Cloud Gateway

### 🎯 역할 및 기능

**주요 역할:**
- **단일 진입점**: 모든 클라이언트 요청의 중앙화된 처리
- **라우팅**: 요청을 적절한 마이크로서비스로 전달
- **로드 밸런싱**: 여러 인스턴스 간 요청 분산
- **보안**: 인증/인가 처리
- **모니터링**: 요청/응답 로깅 및 메트릭 수집

### ⚙️ 주요 설정

```yaml
# application.yml
spring:
  cloud:
    gateway:
      routes:
        # Auth Service 라우팅
        - id: auth-service
          uri: lb://auth-service
          predicates:
            - Path=/api/auth/**
          filters:
            - RewritePath=/api/auth/(?<path>.*), /$\{path}
            
        # User Service 라우팅  
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - RewritePath=/api/users/(?<path>.*), /$\{path}
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
```

### 🔧 고급 기능

**1. Rate Limiting**
```yaml
spring:
  cloud:
    gateway:
      default-filters:
        - name: RequestRateLimiter
          args:
            redis-rate-limiter.replenishRate: 10
            redis-rate-limiter.burstCapacity: 20
            key-resolver: "#{@ipKeyResolver}"
```

**2. Circuit Breaker**
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - name: CircuitBreaker
              args:
                name: userServiceCircuitBreaker
                fallbackUri: forward:/fallback/users
```

## 🔍 Eureka Service Discovery

### 🎯 서비스 등록 및 발견

**Eureka Server 역할:**
- 서비스 인스턴스 등록 관리
- 서비스 상태 모니터링 (Health Check)
- 서비스 디스커버리 정보 제공
- 클라이언트 측 로드 밸런싱 지원

### ⚙️ 구성 요소

**1. Eureka Server**
```yaml
# codin-service-registry/application.yml
eureka:
  instance:
    hostname: localhost
  client:
    registerWithEureka: false
    fetchRegistry: false
    serviceUrl:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
  server:
    waitTimeInMsWhenSyncEmpty: 0
    enableSelfPreservation: false
```

**2. Eureka Client (각 마이크로서비스)**
```yaml
# 각 서비스의 application.yml
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
  instance:
    preferIpAddress: true
    leaseRenewalIntervalInSeconds: 10
    health-check-url-path: /actuator/health
```

### 📊 서비스 상태 모니터링

Eureka Dashboard에서 확인 가능한 정보:
- 등록된 서비스 목록
- 각 서비스의 인스턴스 상태
- 서비스별 엔드포인트 정보
- Health Check 결과

## ⚡ 서비스 간 통신

### 🔗 OpenFeign을 통한 HTTP 통신

**장점:**
- 선언적 HTTP 클라이언트
- Eureka와 자동 통합
- 로드 밸런싱 내장
- 회로 차단기 지원

**구현 예시:**

```java
// UserServiceClient.java
@FeignClient(name = "user-service")
public interface UserServiceClient {
    
    @GetMapping("/users/{userId}")
    UserResponse getUser(@PathVariable String userId);
    
    @PostMapping("/users")
    UserResponse createUser(@RequestBody CreateUserRequest request);
}

// 사용 예시
@Service
public class ContentService {
    
    private final UserServiceClient userServiceClient;
    
    public void createPost(CreatePostRequest request) {
        // 사용자 정보 조회
        UserResponse user = userServiceClient.getUser(request.getUserId());
        
        // 게시글 생성 로직
        // ...
    }
}
```

### 🔄 동기 vs 비동기 통신

**동기 통신 (OpenFeign)**
- 즉시 응답이 필요한 경우
- 사용자 정보 조회, 권한 검증 등

**비동기 통신 (메시지 큐 - 향후 도입 예정)**
- 이벤트 기반 처리
- 알림 발송, 데이터 동기화 등

## 💾 데이터 관리

### 🗄️ Database per Service 패턴

각 마이크로서비스는 자체 데이터베이스를 소유합니다:

```
Auth Service (8081)
├── Collections: users, tokens, sessions
└── MongoDB Database: codin_auth

User Service (8082)  
├── Collections: profiles, preferences, activities
└── MongoDB Database: codin_user

Content Service (8083)
├── Collections: posts, comments, files
└── MongoDB Database: codin_content

Notification Service (8084)
├── Collections: notifications, templates, subscriptions  
└── MongoDB Database: codin_notification

Chat Service (8085)
├── Collections: chatrooms, messages, participants
└── MongoDB Database: codin_chat
```

### 📊 데이터 일관성 전략

**1. 최종 일관성 (Eventual Consistency)**
- 서비스 간 데이터 동기화는 비동기로 처리
- 이벤트 기반 아키텍처 도입 예정

**2. 사가 패턴 (Saga Pattern)**
- 분산 트랜잭션 처리
- 각 서비스별 로컬 트랜잭션 + 보상 트랜잭션

**3. CQRS (Command Query Responsibility Segregation)**
- 읽기/쓰기 모델 분리
- 성능 최적화 및 확장성 향상

### 🗃️ 공통 데이터 관리

**Redis 활용:**
- 세션 데이터 (JWT 토큰, 사용자 세션)
- 캐시 데이터 (사용자 프로필, 권한 정보)
- Rate Limiting 카운터
- 실시간 데이터 (채팅 상태, 알림 큐)

## 🔐 보안 및 인증

### 🎫 JWT 기반 인증

**구조:**
```
Client → API Gateway → Auth Service (JWT 발급)
       ↓
       → Other Services (JWT 검증)
```

**JWT 토큰 구조:**
```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "user123",
    "roles": ["STUDENT", "USER"],
    "dept": "COMPUTER_ENGINEERING",
    "exp": 1640995200
  }
}
```

### 🔒 서비스 간 보안

**1. API Gateway 레벨 인증**
```java
@Component
public class AuthenticationFilter implements GatewayFilter {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = extractToken(exchange.getRequest());
        
        if (isValidToken(token)) {
            return chain.filter(exchange);
        } else {
            return unauthorized(exchange);
        }
    }
}
```

**2. 서비스별 권한 검증**
```java
@PreAuthorize("hasRole('ADMIN') or @userService.isOwner(#userId, authentication.name)")
@GetMapping("/users/{userId}")
public UserResponse getUser(@PathVariable String userId) {
    return userService.getUser(userId);
}
```

## 🚀 배포 및 운영

### 🐳 Docker 컨테이너화

**각 서비스별 Dockerfile:**
```dockerfile
FROM openjdk:17-jdk-slim

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]
```

**Docker Compose 구성:**
```yaml
version: '3.8'
services:
  eureka-server:
    build: ./modules/codin-service-registry
    ports:
      - "8761:8761"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      
  api-gateway:
    build: ./modules/codin-api-gateway
    ports:
      - "8080:8080"
    depends_on:
      - eureka-server
    environment:
      - EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka-server:8761/eureka/
```

### 📊 모니터링 및 로깅

**1. Spring Boot Actuator**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,loggers
  endpoint:
    health:
      show-details: always
```

**2. 중앙 집중식 로깅 (향후 계획)**
- ELK Stack (Elasticsearch, Logstash, Kibana)
- 각 서비스의 로그 중앙화
- 분산 트레이싱 (Zipkin/Jaeger)

### 🔄 CI/CD 파이프라인

**GitHub Actions 워크플로우:**
```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 17
        uses: actions/setup-java@v2
        with:
          java-version: '17'
      - name: Run tests
        run: ./gradlew test
        
  build:
    needs: test
    runs-on: ubuntu-latest
    steps:
      - name: Build Docker images
        run: |
          docker build -t codin/auth-service ./modules/codin-auth-service
          docker build -t codin/user-service ./modules/codin-user-service
          # 기타 서비스들...
          
  deploy:
    needs: build
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
      - name: Deploy to production
        run: |
          # 배포 스크립트 실행
          ./scripts/deploy.sh
```

---

## 🔮 향후 개선 계획

### 1. 서비스 메시 도입
- **Istio** 또는 **Consul Connect** 적용
- 서비스 간 통신 암호화
- 트래픽 정책 관리
- 카나리 배포 지원

### 2. 이벤트 기반 아키텍처
- **Apache Kafka** 또는 **RabbitMQ** 도입
- 비동기 메시지 처리
- 이벤트 소싱 패턴 적용

### 3. 쿠버네티스 마이그레이션
- 컨테이너 오케스트레이션
- 자동 스케일링
- 롤링 업데이트
- 서비스 디스커버리 고도화

### 4. 관찰 가능성 (Observability) 강화
- **분산 트레이싱**: Jaeger/Zipkin
- **메트릭 수집**: Prometheus + Grafana
- **중앙 집중식 로깅**: ELK Stack
- **알림 시스템**: PagerDuty/Slack 연동

---

**📞 Contact**: CODIN 개발팀 | [GitHub Issues](https://github.com/CodIN-INU/BACKEND/issues)
