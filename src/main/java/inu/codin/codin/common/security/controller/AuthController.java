package inu.codin.codin.common.security.controller;

import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.common.security.dto.SignUpAndLoginRequestDto;
import inu.codin.codin.common.security.service.AuthService;
import inu.codin.codin.common.security.service.JwtService;
import inu.codin.codin.domain.user.dto.request.UserNicknameRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value = "/auth")
@Tag(name = "User Auth API", description = "유저 회원가입, 로그인, 로그아웃, 리이슈 API")
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;
    private final AuthService authService;


    @Operation(
            summary = "포탈 로그인",
            description = "포탈 아이디, 비밀번호를 통해 로그인 진행 및 학적 정보 반환"
    )
    @PostMapping("/portal")
    public ResponseEntity<SingleResponse<?>> portalSignUp(@RequestBody @Valid SignUpAndLoginRequestDto signUpAndLoginRequestDto, HttpServletResponse response) {
        Integer result = authService.signUp(signUpAndLoginRequestDto, response);
        if (result.equals(0)) {
            return ResponseEntity.ok()
                    .body(new SingleResponse<>(200, "포탈 로그인 진행 완료", "기존 유저 로그인 완료"));
        } else {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new SingleResponse<>(201, "포탈 로그인 진행 완료", "새로운 유저 등록 완료"));
        }
    }

    @Operation(summary = "회원가입")
    @PostMapping(value = "/signup/{studentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SingleResponse<?>> signUpUser(
            @PathVariable("studentId") String studentId,
            @RequestPart @Valid UserNicknameRequestDto userNicknameRequestDto,
            @RequestPart(value = "userImage", required = false) MultipartFile userImage) {
        authService.createUser(studentId, userNicknameRequestDto, userImage);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "회원가입 성공", null));
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        jwtService.deleteToken();
        return ResponseEntity.ok().body(new SingleResponse<>(200, "로그아웃 성공", null));
    }

    @Operation(summary = "토큰 재발급")
    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        jwtService.reissueToken(request, response);
        return ResponseEntity.ok().body(new SingleResponse<>(200, "토큰 재발급 성공", null));
    }
}
