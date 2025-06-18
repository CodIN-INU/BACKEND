#!/bin/bash

# 개발 환경에서 각 서비스를 개별적으로 실행하는 스크립트

set -e

function start_service() {
    local service_name=$1
    local port=$2
    
    echo "🚀 Starting $service_name on port $port..."
    
    case $service_name in
        "auth")
            cd modules/codin-auth-service && ../../gradlew bootRun --args='--server.port='$port &
            ;;
        "user")
            cd modules/codin-user-service && ../../gradlew bootRun --args='--server.port='$port &
            ;;
        "content")
            cd modules/codin-content-service && ../../gradlew bootRun --args='--server.port='$port &
            ;;
        "notification")
            cd modules/codin-notification-service && ../../gradlew bootRun --args='--server.port='$port &
            ;;
        "chat")
            cd modules/codin-chat-service && ../../gradlew bootRun --args='--server.port='$port &
            ;;
        "gateway")
            cd modules/codin-api-gateway && ../../gradlew bootRun --args='--server.port='$port &
            ;;
        *)
            echo "❌ Unknown service: $service_name"
            exit 1
            ;;
    esac
    
    echo "✅ $service_name started"
}

function stop_all_services() {
    echo "🛑 Stopping all services..."
    pkill -f "codin-.*-service" || true
    pkill -f "codin-api-gateway" || true
    echo "✅ All services stopped"
}

# 사용법 출력
function show_usage() {
    echo "사용법: $0 [COMMAND] [SERVICE]"
    echo ""
    echo "Commands:"
    echo "  start [service]  - 특정 서비스 시작 (auth, user, content, notification, chat, gateway)"
    echo "  start-all        - 모든 서비스 시작"
    echo "  stop             - 모든 서비스 중지"
    echo "  status           - 실행 중인 서비스 상태 확인"
    echo ""
    echo "Examples:"
    echo "  $0 start auth           # 인증 서비스만 시작"
    echo "  $0 start-all           # 모든 서비스 시작"
    echo "  $0 stop                # 모든 서비스 중지"
}

# 메인 로직
case $1 in
    "start")
        if [ -z "$2" ]; then
            echo "❌ 서비스명을 지정해주세요."
            show_usage
            exit 1
        fi
        
        # 포트 매핑
        case $2 in
            "auth") start_service "auth" 8081 ;;
            "user") start_service "user" 8082 ;;
            "content") start_service "content" 8083 ;;
            "notification") start_service "notification" 8084 ;;
            "chat") start_service "chat" 8085 ;;
            "gateway") start_service "gateway" 8080 ;;
            *) echo "❌ 알 수 없는 서비스: $2"; show_usage; exit 1 ;;
        esac
        ;;
    
    "start-all")
        echo "🚀 모든 서비스 시작 중..."
        start_service "auth" 8081
        sleep 5
        start_service "user" 8082
        sleep 5
        start_service "content" 8083
        sleep 5
        start_service "notification" 8084
        sleep 5
        start_service "chat" 8085
        sleep 5
        start_service "gateway" 8080
        echo "✅ 모든 서비스가 시작되었습니다!"
        ;;
    
    "stop")
        stop_all_services
        ;;
    
    "status")
        echo "📊 실행 중인 CODIN 서비스들:"
        ps aux | grep -E "codin-.*-service|codin-api-gateway" | grep -v grep || echo "실행 중인 서비스가 없습니다."
        ;;
    
    *)
        show_usage
        ;;
esac
