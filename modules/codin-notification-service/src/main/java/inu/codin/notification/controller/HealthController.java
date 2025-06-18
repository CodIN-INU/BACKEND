package inu.codin.notification.controller;

import inu.codin.common.response.SingleResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public SingleResponse<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("service", "codin-notification-service");
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("port", 8084);
        return SingleResponse.success(health);
    }
}
