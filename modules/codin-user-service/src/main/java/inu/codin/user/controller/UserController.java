package inu.codin.user.controller;

import inu.codin.common.response.SingleResponse;
import inu.codin.common.response.ListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    @GetMapping("/profile")
    public SingleResponse<UserProfile> getProfile(@RequestHeader("Authorization") String token) {
        // TODO: 사용자 프로필 조회 로직 구현
        return SingleResponse.success(new UserProfile());
    }

    @PutMapping("/profile")
    public SingleResponse<String> updateProfile(@RequestHeader("Authorization") String token, 
                                              @RequestBody UpdateProfileRequest request) {
        // TODO: 프로필 업데이트 로직 구현
        return SingleResponse.success("프로필 업데이트 성공");
    }

    @GetMapping("/search")
    public ListResponse<UserProfile> searchUsers(@RequestParam String query) {
        // TODO: 사용자 검색 로직 구현
        return ListResponse.success(List.of());
    }

    @PostMapping("/follow/{targetUserId}")
    public SingleResponse<String> followUser(@PathVariable String targetUserId,
                                           @RequestHeader("Authorization") String token) {
        // TODO: 팔로우 로직 구현
        return SingleResponse.success("팔로우 성공");
    }

    @DeleteMapping("/follow/{targetUserId}")
    public SingleResponse<String> unfollowUser(@PathVariable String targetUserId,
                                             @RequestHeader("Authorization") String token) {
        // TODO: 언팔로우 로직 구현
        return SingleResponse.success("언팔로우 성공");
    }

    // DTOs
    public static class UserProfile {
        private String id;
        private String email;
        private String name;
        private String department;
        private String profileImage;
        // getters, setters
    }

    public static class UpdateProfileRequest {
        private String name;
        private String department;
        private String profileImage;
        // getters, setters
    }
}
