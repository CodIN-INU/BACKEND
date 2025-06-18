#!/bin/bash

# CODIN MSA Docker Management Script
# This script helps manage the complete CODIN microservices architecture using Docker

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PROJECT_NAME="codin-msa"
COMPOSE_FILE="docker-compose.yml"

# Functions
print_banner() {
    echo -e "${BLUE}"
    echo "╔══════════════════════════════════════════════════════════════╗"
    echo "║                    CODIN MSA Docker Manager                  ║"
    echo "║              Microservices Architecture Manager              ║"
    echo "╚══════════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
}

print_usage() {
    echo -e "${YELLOW}Usage: $0 [COMMAND]${NC}"
    echo ""
    echo "Commands:"
    echo "  build         Build all microservices JAR files"
    echo "  up            Start all services (with build)"
    echo "  down          Stop all services"
    echo "  restart       Restart all services"
    echo "  logs          Show logs for all services"
    echo "  logs <service> Show logs for specific service"
    echo "  status        Show status of all services"
    echo "  health        Check health of all services"
    echo "  clean         Clean up containers, images, and volumes"
    echo "  shell <service> Connect to service container shell"
    echo "  scale <service> <count> Scale a service"
    echo "  help          Show this help message"
    echo ""
    echo "Services:"
    echo "  mongodb, redis, eureka-server"
    echo "  auth-service, user-service, content-service"
    echo "  notification-service, chat-service, api-gateway"
}

check_prerequisites() {
    if ! command -v docker &> /dev/null; then
        echo -e "${RED}Error: Docker is not installed${NC}"
        exit 1
    fi

    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        echo -e "${RED}Error: Docker Compose is not installed${NC}"
        exit 1
    fi

    if [ ! -f "$COMPOSE_FILE" ]; then
        echo -e "${RED}Error: docker-compose.yml not found${NC}"
        exit 1
    fi

    if [ ! -f ".env.local" ]; then
        echo -e "${YELLOW}Warning: .env.local not found. Creating from .env.example...${NC}"
        if [ -f ".env.example" ]; then
            cp .env.example .env.local
            echo -e "${GREEN}Created .env.local from .env.example${NC}"
        else
            echo -e "${RED}Error: .env.example not found${NC}"
            exit 1
        fi
    fi
}

build_services() {
    echo -e "${BLUE}Building all microservices...${NC}"
    
    # Build all Gradle modules
    echo -e "${YELLOW}Building JAR files...${NC}"
    ./gradlew clean build -x test
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ All services built successfully${NC}"
    else
        echo -e "${RED}✗ Build failed${NC}"
        exit 1
    fi
}

start_services() {
    echo -e "${BLUE}Starting CODIN MSA services...${NC}"
    
    # Build first
    build_services
    
    # Start infrastructure services first
    echo -e "${YELLOW}Starting infrastructure services...${NC}"
    docker-compose up -d mongodb redis eureka-server
    
    # Wait for infrastructure to be ready
    echo -e "${YELLOW}Waiting for infrastructure services to be ready...${NC}"
    sleep 30
    
    # Start application services
    echo -e "${YELLOW}Starting microservices...${NC}"
    docker-compose up -d auth-service user-service content-service notification-service chat-service
    
    # Wait for services to start
    sleep 20
    
    # Start API Gateway last
    echo -e "${YELLOW}Starting API Gateway...${NC}"
    docker-compose up -d api-gateway
    
    echo -e "${GREEN}✓ All services started successfully${NC}"
    echo -e "${BLUE}API Gateway available at: http://localhost:8080${NC}"
    echo -e "${BLUE}Eureka Dashboard available at: http://localhost:8761${NC}"
}

stop_services() {
    echo -e "${BLUE}Stopping CODIN MSA services...${NC}"
    docker-compose down
    echo -e "${GREEN}✓ All services stopped${NC}"
}

restart_services() {
    echo -e "${BLUE}Restarting CODIN MSA services...${NC}"
    stop_services
    start_services
}

show_logs() {
    if [ $# -eq 1 ]; then
        echo -e "${BLUE}Showing logs for all services...${NC}"
        docker-compose logs -f
    else
        echo -e "${BLUE}Showing logs for $2...${NC}"
        docker-compose logs -f "$2"
    fi
}

show_status() {
    echo -e "${BLUE}CODIN MSA Services Status:${NC}"
    docker-compose ps
}

check_health() {
    echo -e "${BLUE}Checking health of all services...${NC}"
    
    services=("auth-service:8081" "user-service:8082" "content-service:8083" 
              "notification-service:8084" "chat-service:8085" "api-gateway:8080")
    
    for service in "${services[@]}"; do
        IFS=':' read -r name port <<< "$service"
        echo -n "Checking $name... "
        
        if curl -f -s "http://localhost:$port/api/health" > /dev/null; then
            echo -e "${GREEN}✓ Healthy${NC}"
        else
            echo -e "${RED}✗ Unhealthy${NC}"
        fi
    done
    
    # Check infrastructure
    echo -n "Checking MongoDB... "
    if docker-compose exec -T mongodb mongo --eval "db.runCommand('ping')" > /dev/null 2>&1; then
        echo -e "${GREEN}✓ Healthy${NC}"
    else
        echo -e "${RED}✗ Unhealthy${NC}"
    fi
    
    echo -n "Checking Redis... "
    if docker-compose exec -T redis redis-cli ping > /dev/null 2>&1; then
        echo -e "${GREEN}✓ Healthy${NC}"
    else
        echo -e "${RED}✗ Unhealthy${NC}"
    fi
}

clean_up() {
    echo -e "${BLUE}Cleaning up Docker resources...${NC}"
    
    # Stop all services
    docker-compose down
    
    # Remove containers
    docker-compose rm -f
    
    # Remove images
    docker-compose down --rmi all
    
    # Remove volumes (optional - comment out to keep data)
    # docker-compose down --volumes
    
    # Prune unused Docker resources
    docker system prune -f
    
    echo -e "${GREEN}✓ Cleanup completed${NC}"
}

connect_shell() {
    if [ $# -ne 2 ]; then
        echo -e "${RED}Error: Please specify a service name${NC}"
        return 1
    fi
    
    service_name="$2"
    echo -e "${BLUE}Connecting to $service_name shell...${NC}"
    docker-compose exec "$service_name" /bin/bash
}

scale_service() {
    if [ $# -ne 3 ]; then
        echo -e "${RED}Error: Please specify service name and count${NC}"
        return 1
    fi
    
    service_name="$2"
    count="$3"
    echo -e "${BLUE}Scaling $service_name to $count instances...${NC}"
    docker-compose up -d --scale "$service_name=$count" "$service_name"
}

# Main script
print_banner
check_prerequisites

case "${1:-help}" in
    build)
        build_services
        ;;
    up)
        start_services
        ;;
    down)
        stop_services
        ;;
    restart)
        restart_services
        ;;
    logs)
        show_logs "$@"
        ;;
    status)
        show_status
        ;;
    health)
        check_health
        ;;
    clean)
        clean_up
        ;;
    shell)
        connect_shell "$@"
        ;;
    scale)
        scale_service "$@"
        ;;
    help|*)
        print_usage
        ;;
esac
