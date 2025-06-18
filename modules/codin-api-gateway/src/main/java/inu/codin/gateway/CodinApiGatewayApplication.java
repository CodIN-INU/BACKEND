package inu.codin.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CodinApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodinApiGatewayApplication.class, args);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // 인증 서비스 라우팅
                .route("auth-service", r -> r.path("/api/auth/**")
                        .uri("lb://codin-auth-service"))
                
                // 사용자 서비스 라우팅
                .route("user-service", r -> r.path("/api/users/**")
                        .uri("lb://codin-user-service"))
                
                // 콘텐츠 서비스 라우팅
                .route("content-service-posts", r -> r.path("/api/posts/**")
                        .uri("lb://codin-content-service"))
                .route("content-service-comments", r -> r.path("/api/comments/**")
                        .uri("lb://codin-content-service"))
                .route("content-service-lectures", r -> r.path("/api/lectures/**")
                        .uri("lb://codin-content-service"))
                .route("content-service-info", r -> r.path("/api/info/**")
                        .uri("lb://codin-content-service"))
                .route("content-service-reports", r -> r.path("/api/reports/**")
                        .uri("lb://codin-content-service"))
                .route("content-service-scraps", r -> r.path("/api/scraps/**")
                        .uri("lb://codin-content-service"))
                
                // 알림 서비스 라우팅
                .route("notification-service", r -> r.path("/api/notifications/**", "/api/fcm/**")
                        .uri("lb://codin-notification-service"))
                
                // 채팅 서비스 라우팅
                .route("chat-service", r -> r.path("/api/chatroom/**", "/api/chats/**")
                        .uri("lb://codin-chat-service"))
                
                .build();
    }
}
