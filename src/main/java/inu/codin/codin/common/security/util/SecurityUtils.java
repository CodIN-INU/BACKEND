package inu.codin.codin.common.security.util;

import inu.codin.codin.common.security.exception.JwtException;
import inu.codin.codin.common.security.exception.SecurityErrorCode;
import inu.codin.codin.domain.user.entity.UserRole;
import inu.codin.codin.domain.user.security.CustomUserDetails;
import org.bson.types.ObjectId;
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
     * @throws JwtException 인증 정보가 없는 경우 예외 발생
     */
    public static ObjectId getCurrentUserId() {
        CustomUserDetails userDetails = getCustomUserDetails();
        return userDetails.getId();
    }

    /**
     * 현재 인증된 사용자의 ROLE를 반환.
     *
     * @return 인증된 사용자의 ROLE
     * @throws JwtException 인증 정보가 없는 경우 예외 발생
     */
    public static UserRole getCurrentUserRole(){
        CustomUserDetails userDetails = getCustomUserDetails();
        return userDetails.getRole();
    }

    private static CustomUserDetails getCustomUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new JwtException(SecurityErrorCode.ACCESS_DENIED);
        }
        return userDetails;
    }

    /**
     * 매개변수 id가 현재 로그인 한 유저의 _Id와 일치하는지 확인
     * @param id
     * @throws JwtException 일치하지 않을 경우 에러 발생
     */
    public static void validateUser(ObjectId id){
        ObjectId userId = getCurrentUserId();
        if (!id.equals(userId)) {
            throw new JwtException(SecurityErrorCode.ACCESS_DENIED, "현재 유저에게 권한이 없습니다.");
        }
    }

}
