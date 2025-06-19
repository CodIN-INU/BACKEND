# 로컬 개발 환경 설정 가이드

## 📋 목차
- [💾 사전 요구사항](#-사전-요구사항)
- [🚀 빠른 시작](#-빠른-시작)
- [🔧 상세 설정](#-상세-설정)
- [🐳 Docker 개발 환경](#-docker-개발-환경)
- [🛠️ IDE 설정](#️-ide-설정)
- [🧪 테스트 환경](#-테스트-환경)

## 💾 사전 요구사항

### 필수 소프트웨어
- **Java 17** 이상
- **Git** 최신 버전
- **Docker** & **Docker Compose**
- **IDE**: IntelliJ IDEA (권장) 또는 VS Code

### 선택 설치
- **MongoDB Compass** (데이터베이스 GUI)
- **Redis CLI** 또는 **Redis Desktop Manager**
- **Postman** 또는 **Insomnia** (API 테스트)

## 🚀 빠른 시작

### 1. 저장소 클론
```bash
git clone https://github.com/CodIN-INU/BACKEND.git
cd BACKEND
```

### 2. 환경 변수 설정
```bash
# .env 파일 생성
cp .env.example .env

# 환경 변수 수정
vim .env
```

### 3. 인프라 서비스 시작
```bash
# MongoDB, Redis 시작
docker-compose up -d mongodb redis

# 서비스 상태 확인
docker-compose ps
```

### 4. 애플리케이션 실행
```bash
# 1. Service Registry 시작 (필수 - 가장 먼저)
./gradlew :modules:codin-service-registry:bootRun

# 2. API Gateway 시작 (2번째)
./gradlew :modules:codin-api-gateway:bootRun

# 3. 나머지 서비스들 (순서 무관)
./gradlew :modules:codin-auth-service:bootRun
./gradlew :modules:codin-user-service:bootRun
./gradlew :modules:codin-content-service:bootRun
./gradlew :modules:codin-notification-service:bootRun
./gradlew :modules:codin-chat-service:bootRun
```

### 5. 서비스 확인
- **Eureka Dashboard**: http://localhost:8761
- **API Gateway**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html

## 🔧 상세 설정

### 환경 변수 설정 (.env)
```bash
# Database
MONGODB_URI=mongodb://localhost:27017/codin
REDIS_HOST=localhost
REDIS_PORT=6379

# JWT
JWT_SECRET=your-super-secret-jwt-key-here-min-32-chars
JWT_EXPIRATION=86400000

# AWS S3
AWS_ACCESS_KEY=your-aws-access-key
AWS_SECRET_KEY=your-aws-secret-key
AWS_REGION=ap-northeast-2
AWS_S3_BUCKET=codin-dev-bucket

# Email
EMAIL_USERNAME=your-email@gmail.com
EMAIL_PASSWORD=your-app-password

# OAuth2
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
KAKAO_CLIENT_ID=your-kakao-client-id
KAKAO_CLIENT_SECRET=your-kakao-client-secret

# Firebase
FIREBASE_PROJECT_ID=your-firebase-project-id
FIREBASE_CONFIG_PATH=classpath:firebase-service-account.json
```

### MongoDB 초기 데이터 설정
```bash
# MongoDB 접속
mongo mongodb://localhost:27017/codin

# 초기 데이터 스크립트 실행
load('scripts/init-mongo.js')
```

### Redis 설정 확인
```bash
# Redis CLI 접속
redis-cli -h localhost -p 6379

# 연결 테스트
127.0.0.1:6379> ping
PONG

# 키 목록 확인
127.0.0.1:6379> keys *
```

## 🐳 Docker 개발 환경

### 전체 서비스 Docker로 실행
```bash
# 모든 서비스 빌드 및 실행
docker-compose up --build

# 백그라운드 실행
docker-compose up -d

# 특정 서비스만 실행
docker-compose up -d mongodb redis eureka-server
```

### 하이브리드 개발 환경
```bash
# 인프라만 Docker로 실행
docker-compose up -d mongodb redis

# 애플리케이션은 로컬에서 실행
./gradlew bootRun
```

### Docker 유용한 명령어
```bash
# 로그 확인
docker-compose logs -f [service-name]

# 컨테이너 내부 접속
docker-compose exec mongodb bash
docker-compose exec redis redis-cli

# 볼륨 및 이미지 정리
docker-compose down -v
docker system prune -a
```

## 🛠️ IDE 설정

### IntelliJ IDEA 설정

#### 1. 프로젝트 Import
- `File > Open` → `build.gradle` 선택
- `Open as Project` 선택
- Gradle Auto-Import 활성화

#### 2. Java 버전 설정
- `File > Project Structure > Project`
- Project SDK: Java 17
- Project Language Level: 17

#### 3. 코드 스타일 설정
- `File > Settings > Editor > Code Style`
- Scheme: `Google Java Style` (플러그인 설치 필요)

#### 4. 플러그인 설치
- **Lombok**: Lombok 어노테이션 지원
- **Spring Boot Helper**: Spring Boot 개발 지원
- **Docker**: Docker 파일 지원
- **MongoDB Plugin**: MongoDB 쿼리 지원

#### 5. 실행 구성 설정
```yaml
# Run Configuration for Service Registry
Name: Service Registry
Main class: inu.codin.registry.CodinServiceRegistryApplication
Module: codin-service-registry.main
Environment variables: SPRING_PROFILES_ACTIVE=dev
```

### VS Code 설정

#### 1. 필수 확장 프로그램
- **Extension Pack for Java**
- **Spring Boot Extension Pack**
- **Docker**
- **Thunder Client** (API 테스트)

#### 2. settings.json 설정
```json
{
  "java.home": "/path/to/java-17",
  "java.configuration.runtimes": [
    {
      "name": "JavaSE-17",
      "path": "/path/to/java-17"
    }
  ],
  "spring-boot.ls.java.home": "/path/to/java-17"
}
```

## 🧪 테스트 환경

### 단위 테스트 실행
```bash
# 전체 테스트 실행
./gradlew test

# 특정 모듈 테스트
./gradlew :modules:codin-auth-service:test

# 테스트 커버리지 리포트
./gradlew test jacocoTestReport
```

### 통합 테스트 환경
```bash
# 테스트용 데이터베이스 설정
export SPRING_PROFILES_ACTIVE=test

# 통합 테스트 실행
./gradlew integrationTest
```

### API 테스트
```bash
# Health Check
curl http://localhost:8080/actuator/health

# Eureka 상태 확인
curl http://localhost:8761/eureka/apps

# 서비스별 Health Check
curl http://localhost:8081/actuator/health # Auth Service
curl http://localhost:8082/actuator/health # User Service
curl http://localhost:8083/actuator/health # Content Service
```

## 🔍 디버깅 및 모니터링

### 로그 설정
```yaml
# application-dev.yml
logging:
  level:
    inu.codin: DEBUG
    org.springframework.cloud: DEBUG
    org.springframework.security: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### JVM 메모리 모니터링
```bash
# JVM 옵션 추가
export JAVA_OPTS="-Xms512m -Xmx2g -XX:+HeapDumpOnOutOfMemoryError"

# VisualVM으로 모니터링
jvisualvm
```

### 성능 프로파일링
```bash
# Spring Boot Actuator 메트릭
curl http://localhost:8080/actuator/metrics

# JFR (Java Flight Recorder) 활성화
java -XX:+FlightRecorder -XX:StartFlightRecording=duration=60s,filename=profile.jfr
```

## 🚨 문제 해결

### 자주 발생하는 문제들

#### 1. 포트 충돌
```bash
# 포트 사용 중인 프로세스 확인
lsof -i :8080
netstat -an | grep 8080

# 프로세스 종료
kill -9 <PID>
```

#### 2. Eureka 서비스 등록 실패
```bash
# Eureka Server 상태 확인
curl http://localhost:8761/eureka/apps

# 네트워크 연결 확인
telnet localhost 8761
```

#### 3. MongoDB 연결 실패
```bash
# MongoDB 상태 확인
docker-compose ps mongodb

# MongoDB 로그 확인
docker-compose logs mongodb

# 연결 테스트
mongo mongodb://localhost:27017/codin
```

#### 4. Redis 연결 실패
```bash
# Redis 상태 확인
docker-compose ps redis

# Redis 연결 테스트
redis-cli -h localhost -p 6379 ping
```

### 성능 최적화 팁

#### 1. JVM 힙 메모리 조정
```bash
# 서비스별 메모리 설정
export JAVA_OPTS_GATEWAY="-Xms256m -Xmx512m"
export JAVA_OPTS_AUTH="-Xms512m -Xmx1g"
export JAVA_OPTS_USER="-Xms512m -Xmx1g"
```

#### 2. Gradle 빌드 최적화
```bash
# Gradle 병렬 빌드 활성화
echo "org.gradle.parallel=true" >> gradle.properties
echo "org.gradle.daemon=true" >> gradle.properties
```

#### 3. Docker 리소스 제한
```yaml
# docker-compose.yml
services:
  mongodb:
    deploy:
      resources:
        limits:
          memory: 512M
        reservations:
          memory: 256M
```

## 📚 추가 리소스

### 공식 문서
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Cloud Reference](https://docs.spring.io/spring-cloud/docs/current/reference/html/)
- [MongoDB Manual](https://docs.mongodb.com/manual/)

### 유용한 도구
- **Spring Boot DevTools**: 자동 재시작
- **Lombok**: 보일러플레이트 코드 제거
- **MapStruct**: 객체 매핑
- **TestContainers**: 통합 테스트용 컨테이너
