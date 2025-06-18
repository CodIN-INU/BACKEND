#!/bin/bash

# CODIN MSA 빌드 및 실행 스크립트

set -e

echo "🚀 CODIN MSA 빌드 시작..."

# 1. 전체 프로젝트 빌드
echo "📦 전체 프로젝트 빌드 중..."
./gradlew clean build -x test

# 2. 각 서비스별 Docker 이미지 빌드 (선택사항)
echo "🐳 Docker 이미지 빌드 중..."

# 공통 라이브러리는 이미지 빌드 제외
echo "Building codin-auth-service..."
docker build -t codin-auth-service:latest -f modules/codin-auth-service/Dockerfile .

echo "Building codin-user-service..."
docker build -t codin-user-service:latest -f modules/codin-user-service/Dockerfile .

echo "Building codin-content-service..."
docker build -t codin-content-service:latest -f modules/codin-content-service/Dockerfile .

echo "Building codin-notification-service..."
docker build -t codin-notification-service:latest -f modules/codin-notification-service/Dockerfile .

echo "Building codin-chat-service..."
docker build -t codin-chat-service:latest -f modules/codin-chat-service/Dockerfile .

echo "Building codin-api-gateway..."
docker build -t codin-api-gateway:latest -f modules/codin-api-gateway/Dockerfile .

echo "✅ 모든 서비스 빌드 완료!"

# 3. Docker Compose로 전체 시스템 실행
echo "🚀 전체 시스템 시작 중..."
docker-compose up -d

echo "🎉 CODIN MSA 시스템이 성공적으로 시작되었습니다!"
echo "📊 서비스 상태 확인: docker-compose ps"
echo "📋 로그 확인: docker-compose logs -f [service-name]"
