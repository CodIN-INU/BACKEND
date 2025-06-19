# Docker 가이드 - CODIN MSA

## 📋 목차
- [🐳 Docker 개요](#-docker-개요)
- [🚀 빠른 시작](#-빠른-시작)
- [🔧 Docker Compose 구성](#-docker-compose-구성)
- [📦 개별 서비스 컨테이너](#-개별-서비스-컨테이너)
- [🛠️ 개발 환경 설정](#️-개발-환경-설정)
- [🔍 모니터링 및 디버깅](#-모니터링-및-디버깅)

## 🐳 Docker 개요

CODIN MSA는 Docker를 활용하여 다음과 같은 이점을 제공합니다:

- **환경 일관성**: 개발, 테스트, 프로덕션 환경 동일화
- **격리성**: 각 서비스가 독립적인 컨테이너에서 실행
- **확장성**: 필요에 따라 서비스별 스케일링
- **배포 용이성**: 컨테이너 기반 무중단 배포

## 🚀 빠른 시작

### 전체 시스템 실행
```bash
# 전체 서비스 빌드 및 실행
docker-compose up --build

# 백그라운드 실행
docker-compose up -d

# 로그 실시간 확인
docker-compose logs -f
```

### 개발 모드 실행 (인프라만 Docker)
```bash
# MongoDB, Redis만 Docker로 실행
docker-compose up -d mongodb redis

# 애플리케이션은 로컬에서 개발
./gradlew :modules:codin-service-registry:bootRun
./gradlew :modules:codin-api-gateway:bootRun
```

## 🔧 Docker Compose 구성

### 기본 구성 파일 (docker-compose.yml)
```yaml
version: '3.8'

services:
  # 인프라 서비스
  mongodb:
    image: mongo:7.0
    container_name: codin-mongodb
    restart: unless-stopped
    ports:
      - "27017:27017"
    environment:
      MONGO_INITDB_ROOT_USERNAME: admin
      MONGO_INITDB_ROOT_PASSWORD: password
      MONGO_INITDB_DATABASE: codin
    volumes:
      - mongodb_data:/data/db
      - ./scripts/init-mongo.js:/docker-entrypoint-initdb.d/init-mongo.js:ro
    networks:
      - codin-network
    healthcheck:
      test: ["CMD", "mongosh", "--eval", "db.adminCommand('ping')"]
      interval: 30s
      timeout: 10s
      retries: 3

  redis:
    image: redis:7.2-alpine
    container_name: codin-redis
    restart: unless-stopped
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes --requirepass redispass
    volumes:
      - redis_data:/data
    networks:
      - codin-network
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 30s
      timeout: 10s
      retries: 3

  # Service Registry
  eureka-server:
    build:
      context: ./modules/codin-service-registry
      dockerfile: Dockerfile
    container_name: codin-eureka-server
    restart: unless-stopped
    ports:
      - "8761:8761"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    networks:
      - codin-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8761/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  # API Gateway
  api-gateway:
    build:
      context: ./modules/codin-api-gateway
      dockerfile: Dockerfile
    container_name: codin-api-gateway
    restart: unless-stopped
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - SPRING_DATA_REDIS_HOST=redis
      - SPRING_DATA_REDIS_PORT=6379
      - SPRING_DATA_REDIS_PASSWORD=redispass
    depends_on:
      eureka-server:
        condition: service_healthy
      redis:
        condition: service_healthy
    networks:
      - codin-network

  # Auth Service
  auth-service:
    build:
      context: ./modules/codin-auth-service
      dockerfile: Dockerfile
    container_name: codin-auth-service
    restart: unless-stopped
    ports:
      - "8081:8081"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - SPRING_DATA_MONGODB_URI=mongodb://admin:password@mongodb:27017/codin_auth?authSource=admin
      - SPRING_DATA_REDIS_HOST=redis
      - SPRING_DATA_REDIS_PASSWORD=redispass
      - JWT_SECRET=${JWT_SECRET}
    depends_on:
      eureka-server:
        condition: service_healthy
      mongodb:
        condition: service_healthy
      redis:
        condition: service_healthy
    networks:
      - codin-network

  # User Service
  user-service:
    build:
      context: ./modules/codin-user-service
      dockerfile: Dockerfile
    container_name: codin-user-service
    restart: unless-stopped
    ports:
      - "8082:8082"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - SPRING_DATA_MONGODB_URI=mongodb://admin:password@mongodb:27017/codin_user?authSource=admin
      - SPRING_DATA_REDIS_HOST=redis
      - AWS_ACCESS_KEY=${AWS_ACCESS_KEY}
      - AWS_SECRET_KEY=${AWS_SECRET_KEY}
    depends_on:
      eureka-server:
        condition: service_healthy
      mongodb:
        condition: service_healthy
    networks:
      - codin-network

  # Content Service
  content-service:
    build:
      context: ./modules/codin-content-service
      dockerfile: Dockerfile
    container_name: codin-content-service
    restart: unless-stopped
    ports:
      - "8083:8083"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - SPRING_DATA_MONGODB_URI=mongodb://admin:password@mongodb:27017/codin_content?authSource=admin
      - AWS_ACCESS_KEY=${AWS_ACCESS_KEY}
      - AWS_SECRET_KEY=${AWS_SECRET_KEY}
    depends_on:
      eureka-server:
        condition: service_healthy
      mongodb:
        condition: service_healthy
    networks:
      - codin-network

  # Notification Service
  notification-service:
    build:
      context: ./modules/codin-notification-service
      dockerfile: Dockerfile
    container_name: codin-notification-service
    restart: unless-stopped
    ports:
      - "8084:8084"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - SPRING_DATA_MONGODB_URI=mongodb://admin:password@mongodb:27017/codin_notification?authSource=admin
      - FIREBASE_PROJECT_ID=${FIREBASE_PROJECT_ID}
      - EMAIL_USERNAME=${EMAIL_USERNAME}
      - EMAIL_PASSWORD=${EMAIL_PASSWORD}
    depends_on:
      eureka-server:
        condition: service_healthy
      mongodb:
        condition: service_healthy
    networks:
      - codin-network

  # Chat Service
  chat-service:
    build:
      context: ./modules/codin-chat-service
      dockerfile: Dockerfile
    container_name: codin-chat-service
    restart: unless-stopped
    ports:
      - "8085:8085"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - SPRING_DATA_MONGODB_URI=mongodb://admin:password@mongodb:27017/codin_chat?authSource=admin
      - SPRING_DATA_REDIS_HOST=redis
    depends_on:
      eureka-server:
        condition: service_healthy
      mongodb:
        condition: service_healthy
      redis:
        condition: service_healthy
    networks:
      - codin-network

volumes:
  mongodb_data:
    driver: local
  redis_data:
    driver: local

networks:
  codin-network:
    driver: bridge
```

## 📦 개별 서비스 컨테이너

### 표준 Dockerfile 템플릿
```dockerfile
# Multi-stage build for optimization
FROM openjdk:17-jdk-slim as builder

WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

# Build the application
RUN chmod +x ./gradlew
RUN ./gradlew bootJar --no-daemon

# Production stage
FROM openjdk:17-jdk-slim

# Add application user
RUN addgroup --system spring && adduser --system spring --ingroup spring

# Install required packages
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy JAR file from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Change ownership
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring:spring

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

### 서비스별 Dockerfile 특화

#### Gateway Dockerfile
```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app
COPY build/libs/*.jar app.jar

# Gateway specific health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["java", \
    "-Xms256m", \
    "-Xmx512m", \
    "-Dspring.profiles.active=docker", \
    "-jar", \
    "app.jar"]
```

#### Service Registry Dockerfile
```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app
COPY build/libs/*.jar app.jar

# Eureka specific health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=5 \
  CMD curl -f http://localhost:8761/actuator/health || exit 1

EXPOSE 8761

ENTRYPOINT ["java", \
    "-Xms256m", \
    "-Xmx512m", \
    "-Dspring.profiles.active=docker", \
    "-jar", \
    "app.jar"]
```

## 🛠️ 개발 환경 설정

### 환경별 Docker Compose

#### 개발 환경 (docker-compose.dev.yml)
```yaml
version: '3.8'

services:
  mongodb:
    ports:
      - "27017:27017"
    volumes:
      - ./data/mongodb:/data/db
    command: mongod --replSet rs0

  redis:
    ports:
      - "6379:6379"
    volumes:
      - ./data/redis:/data

  # Development tools
  mongodb-express:
    image: mongo-express:latest
    container_name: codin-mongo-express
    restart: unless-stopped
    ports:
      - "8081:8081"
    environment:
      ME_CONFIG_MONGODB_ADMINUSERNAME: admin
      ME_CONFIG_MONGODB_ADMINPASSWORD: password
      ME_CONFIG_MONGODB_URL: mongodb://admin:password@mongodb:27017/
    depends_on:
      - mongodb
    networks:
      - codin-network

  redis-commander:
    image: rediscommander/redis-commander:latest
    container_name: codin-redis-commander
    restart: unless-stopped
    ports:
      - "8082:8081"
    environment:
      - REDIS_HOSTS=local:redis:6379:0:redispass
    depends_on:
      - redis
    networks:
      - codin-network
```

#### 프로덕션 환경 (docker-compose.prod.yml)
```yaml
version: '3.8'

services:
  mongodb:
    restart: always
    deploy:
      resources:
        limits:
          memory: 1G
          cpus: '0.5'
        reservations:
          memory: 512M
          cpus: '0.25'
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"

  redis:
    restart: always
    deploy:
      resources:
        limits:
          memory: 256M
          cpus: '0.25'
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"

  # Add monitoring services
  prometheus:
    image: prom/prometheus:latest
    container_name: codin-prometheus
    restart: unless-stopped
    ports:
      - "9090:9090"
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml
    networks:
      - codin-network

  grafana:
    image: grafana/grafana:latest
    container_name: codin-grafana
    restart: unless-stopped
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
    volumes:
      - grafana_data:/var/lib/grafana
    networks:
      - codin-network
```

### Docker 유틸리티 스크립트

#### 빌드 및 실행 스크립트 (scripts/docker-manager.sh)
```bash
#!/bin/bash

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Functions
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Build all services
build_all() {
    log_info "Building all services..."
    
    # Build Gradle projects first
    ./gradlew build -x test
    
    # Build Docker images
    docker-compose build --no-cache
    
    log_info "Build completed successfully!"
}

# Start infrastructure only
start_infra() {
    log_info "Starting infrastructure services..."
    docker-compose up -d mongodb redis
    
    # Wait for services to be ready
    log_info "Waiting for services to be ready..."
    sleep 10
    
    # Check health
    if docker-compose exec mongodb mongosh --eval "db.adminCommand('ping')" &>/dev/null; then
        log_info "MongoDB is ready"
    else
        log_error "MongoDB failed to start"
        exit 1
    fi
    
    if docker-compose exec redis redis-cli ping &>/dev/null; then
        log_info "Redis is ready"
    else
        log_error "Redis failed to start"
        exit 1
    fi
}

# Start all services
start_all() {
    log_info "Starting all services..."
    docker-compose up -d
    
    # Wait for Eureka to be ready
    log_info "Waiting for Eureka Server..."
    timeout=300
    elapsed=0
    
    while [ $elapsed -lt $timeout ]; do
        if curl -f http://localhost:8761/actuator/health &>/dev/null; then
            log_info "Eureka Server is ready"
            break
        fi
        sleep 5
        elapsed=$((elapsed + 5))
    done
    
    if [ $elapsed -ge $timeout ]; then
        log_error "Eureka Server failed to start within timeout"
        exit 1
    fi
    
    log_info "All services started successfully!"
}

# Stop all services
stop_all() {
    log_info "Stopping all services..."
    docker-compose down
    log_info "All services stopped"
}

# Clean up
cleanup() {
    log_warn "Cleaning up Docker resources..."
    docker-compose down -v
    docker system prune -f
    log_info "Cleanup completed"
}

# Show logs
show_logs() {
    if [ -z "$1" ]; then
        docker-compose logs -f
    else
        docker-compose logs -f "$1"
    fi
}

# Show status
show_status() {
    log_info "Service Status:"
    docker-compose ps
    
    log_info "\nHealth Status:"
    echo "Eureka: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8761/actuator/health)"
    echo "Gateway: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health)"
}

# Main script
case "$1" in
    build)
        build_all
        ;;
    start-infra)
        start_infra
        ;;
    start)
        start_all
        ;;
    stop)
        stop_all
        ;;
    restart)
        stop_all
        start_all
        ;;
    logs)
        show_logs "$2"
        ;;
    status)
        show_status
        ;;
    cleanup)
        cleanup
        ;;
    *)
        echo "Usage: $0 {build|start-infra|start|stop|restart|logs [service]|status|cleanup}"
        exit 1
        ;;
esac
```

## 🔍 모니터링 및 디버깅

### 컨테이너 상태 확인
```bash
# 실행 중인 컨테이너 확인
docker-compose ps

# 컨테이너 상세 정보
docker-compose logs -f [service-name]

# 컨테이너 리소스 사용량
docker stats $(docker-compose ps -q)
```

### 헬스 체크
```bash
# 서비스별 헬스 체크
curl http://localhost:8761/actuator/health  # Eureka
curl http://localhost:8080/actuator/health  # Gateway
curl http://localhost:8081/actuator/health  # Auth Service

# MongoDB 연결 확인
docker-compose exec mongodb mongosh --eval "db.adminCommand('ping')"

# Redis 연결 확인
docker-compose exec redis redis-cli ping
```

### 디버깅 도구

#### 컨테이너 내부 접속
```bash
# MongoDB 컨테이너 접속
docker-compose exec mongodb bash
mongosh mongodb://admin:password@localhost:27017/codin

# Redis 컨테이너 접속
docker-compose exec redis redis-cli
auth redispass
```

#### 네트워크 디버깅
```bash
# 네트워크 정보 확인
docker network ls
docker network inspect codin-network

# 컨테이너 간 연결 테스트
docker-compose exec api-gateway ping eureka-server
docker-compose exec auth-service ping mongodb
```

### 성능 모니터링

#### 리소스 사용량 모니터링
```bash
# CPU, 메모리 사용량 실시간 확인
docker stats --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}"

# 특정 컨테이너 모니터링
docker stats codin-api-gateway
```

#### 로그 분석
```bash
# 에러 로그만 필터링
docker-compose logs | grep ERROR

# 특정 시간대 로그
docker-compose logs --since="2024-12-01T10:00:00"

# 로그 파일로 저장
docker-compose logs > codin-logs.txt
```

### 백업 및 복구

#### 데이터 백업
```bash
# MongoDB 백업
docker-compose exec mongodb mongodump --uri="mongodb://admin:password@localhost:27017/codin" --out=/backup

# Redis 백업
docker-compose exec redis redis-cli --rdb /data/backup.rdb save
```

#### 볼륨 관리
```bash
# 볼륨 목록 확인
docker volume ls

# 볼륨 백업
docker run --rm -v codin_mongodb_data:/data -v $(pwd):/backup alpine tar czf /backup/mongodb-backup.tar.gz -C /data .

# 볼륨 복구
docker run --rm -v codin_mongodb_data:/data -v $(pwd):/backup alpine tar xzf /backup/mongodb-backup.tar.gz -C /data
```

## 🚨 문제 해결

### 자주 발생하는 문제들

#### 1. 컨테이너 시작 실패
```bash
# 컨테이너 로그 확인
docker-compose logs [service-name]

# 컨테이너 상태 확인
docker-compose ps

# 강제 재시작
docker-compose restart [service-name]
```

#### 2. 포트 충돌
```bash
# 포트 사용 확인
netstat -tulpn | grep :8080

# 기존 프로세스 종료
sudo kill -9 $(sudo lsof -t -i:8080)
```

#### 3. 디스크 공간 부족
```bash
# Docker 이미지 정리
docker image prune -a

# 사용하지 않는 볼륨 정리
docker volume prune

# 전체 시스템 정리
docker system prune -a --volumes
```

#### 4. 네트워크 연결 문제
```bash
# 네트워크 재생성
docker-compose down
docker network prune
docker-compose up -d
```
