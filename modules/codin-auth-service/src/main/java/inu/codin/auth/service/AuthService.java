package inu.codin.auth.service;

import inu.codin.auth.dto.AuthRequestDto;

public interface AuthService {
    String login(AuthRequestDto.LoginRequest request);
    String register(AuthRequestDto.RegisterRequest request);
    void logout(String token);
    String refreshToken(AuthRequestDto.RefreshTokenRequest request);
    boolean validateToken(String token);
    String getUserIdFromToken(String token);
}
