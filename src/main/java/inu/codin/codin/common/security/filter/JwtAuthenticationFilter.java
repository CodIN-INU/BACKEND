package inu.codin.codin.common.security.filter;

import inu.codin.codin.common.security.jwt.JwtAuthenticationToken;
import inu.codin.codin.common.security.jwt.JwtTokenProvider;
import inu.codin.codin.common.security.jwt.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 토큰을 검증하여 인증하는 필터
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String accessToken = jwtUtils.getAccessToken(request);

        // Access Token이 있는 경우
        if (accessToken != null && jwtTokenProvider.validateAccessToken(accessToken)) {
            setAuthentication(accessToken);
        } else {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthentication(String token) {
        String username = jwtTokenProvider.getUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // 토큰이 유효하고, SecurityContext에 Authentication 객체가 없는 경우
        if (userDetails != null) {
            // Authentication 객체 생성 후 SecurityContext에 저장 (인증 완료)
            JwtAuthenticationToken authentication = new JwtAuthenticationToken(userDetails, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }


}
