package inu.codin.auth.dto;

import lombok.Data;

public class AuthRequestDto {

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
