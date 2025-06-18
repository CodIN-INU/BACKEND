package inu.codin.auth.service.impl;

import inu.codin.auth.dto.AuthRequestDto;
import inu.codin.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    @Override
    public String login(AuthRequestDto.LoginRequest request) {
        // TODO: 실제 로그인 로직 구현
        log.info("Login attempt for email: {}", request.getEmail());
        return "temporary-jwt-token";
    }

    @Override
    public String register(AuthRequestDto.RegisterRequest request) {
        // TODO: 실제 회원가입 로직 구현
        log.info("Register attempt for email: {}", request.getEmail());
        return "User registered successfully";
    }

    @Override
    public void logout(String token) {
        // TODO: 실제 로그아웃 로직 구현 (토큰 블랙리스트 처리)
        log.info("Logout with token: {}", token);
    }

    @Override
    public String refreshToken(AuthRequestDto.RefreshTokenRequest request) {
        // TODO: 실제 토큰 갱신 로직 구현
        log.info("Refresh token attempt");
        return "new-jwt-token";
    }

    @Override
    public boolean validateToken(String token) {
        // TODO: 실제 토큰 검증 로직 구현
        return true;
    }

    @Override
    public String getUserIdFromToken(String token) {
        // TODO: 실제 토큰에서 사용자 ID 추출 로직 구현
        return "user-id-from-token";
    }
}
