#!/bin/bash

# 모든 서비스의 헬스 체크를 수행하는 스크립트

set -e

# 색상 정의
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 서비스 정의
declare -A services=(
    ["auth-service"]="8081"
    ["user-service"]="8082"
    ["content-service"]="8083"
    ["notification-service"]="8084"
    ["chat-service"]="8085"
    ["api-gateway"]="8080"
)

function check_service_health() {
    local service_name=$1
    local port=$2
    local url="http://localhost:${port}/api/health"
    
    echo -n "Checking ${service_name} (port ${port})... "
    
    if curl -s -f "${url}" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ UP${NC}"
        
        # 서비스 정보 출력
        local response=$(curl -s "${url}")
        echo "   Response: ${response}"
        echo ""
    else
        echo -e "${RED}❌ DOWN${NC}"
        echo "   URL: ${url}"
        echo ""
    fi
}

function main() {
    echo -e "${YELLOW}🔍 CODIN MSA Health Check${NC}"
    echo "=================================="
    echo ""
    
    for service in "${!services[@]}"; do
        check_service_health "${service}" "${services[$service]}"
    done
    
    echo "=================================="
    echo -e "${YELLOW}Health check completed${NC}"
}

main
