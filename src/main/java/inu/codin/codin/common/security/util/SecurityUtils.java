package inu.codin.codin.common.security.util;

import inu.codin.codin.common.security.exception.JwtException;
import inu.codin.codin.common.security.exception.SecurityErrorCode;
import inu.codin.codin.domain.user.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * SecurityContext와 관련된 유틸리티 클래스.
 */
public class SecurityUtils {

    /**
     * 현재 인증된 사용자의 ID를 반환.
     *
     * @return 인증된 사용자의 ID
     * @throws IllegalStateException 인증 정보가 없는 경우 예외 발생
     */
    public static String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new JwtException(SecurityErrorCode.INVALID_CREDENTIALS);
        }

        return userDetails.getId();
    }

}
