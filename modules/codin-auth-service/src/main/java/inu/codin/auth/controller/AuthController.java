package inu.codin.auth.controller;

import inu.codin.common.response.SingleResponse;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/login")
    public SingleResponse<String> login(@RequestBody LoginRequest request) {
        // TODO: 로그인 로직 구현
        return SingleResponse.success("JWT 토큰 반환 예정");
    }

    @PostMapping("/register")
    public SingleResponse<String> register(@RequestBody RegisterRequest request) {
        // TODO: 회원가입 로직 구현
        return SingleResponse.success("회원가입 성공");
    }

    @PostMapping("/logout")
    public SingleResponse<String> logout(@RequestHeader("Authorization") String token) {
        // TODO: 로그아웃 로직 구현
        return SingleResponse.success("로그아웃 성공");
    }

    @PostMapping("/refresh")
    public SingleResponse<String> refreshToken(@RequestBody RefreshTokenRequest request) {
        // TODO: 토큰 갱신 로직 구현
        return SingleResponse.success("토큰 갱신 성공");
    }

    @GetMapping("/validate")
    public SingleResponse<Boolean> validateToken(@RequestHeader("Authorization") String token) {
        // TODO: 토큰 검증 로직 구현
        return SingleResponse.success(true);
    }

    // Request DTOs
    @Data
    public static class LoginRequest {
        private String email;
        private String password;
    }

    @Data
    public static class RegisterRequest {
        private String email;
        private String password;
        private String name;
        private String department;
    }

    @Data
    public static class RefreshTokenRequest {
        private String refreshToken;
    }
}
