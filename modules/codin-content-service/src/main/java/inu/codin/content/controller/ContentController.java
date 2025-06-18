package inu.codin.content.controller;

import inu.codin.common.response.SingleResponse;
import inu.codin.common.response.ListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
public class ContentController {

    @PostMapping
    public SingleResponse<String> createContent(@RequestHeader("Authorization") String token,
                                              @RequestBody CreateContentRequest request) {
        // TODO: 콘텐츠 생성 로직 구현
        return SingleResponse.success("콘텐츠 생성 성공");
    }

    @GetMapping
    public ListResponse<ContentSummary> getContents(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size,
                                                   @RequestParam(required = false) String category) {
        // TODO: 콘텐츠 목록 조회 로직 구현
        return ListResponse.success(List.of());
    }

    @GetMapping("/{contentId}")
    public SingleResponse<ContentDetail> getContent(@PathVariable String contentId) {
        // TODO: 콘텐츠 상세 조회 로직 구현
        return SingleResponse.success(new ContentDetail());
    }

    @PutMapping("/{contentId}")
    public SingleResponse<String> updateContent(@PathVariable String contentId,
                                              @RequestHeader("Authorization") String token,
                                              @RequestBody UpdateContentRequest request) {
        // TODO: 콘텐츠 수정 로직 구현
        return SingleResponse.success("콘텐츠 수정 성공");
    }

    @DeleteMapping("/{contentId}")
    public SingleResponse<String> deleteContent(@PathVariable String contentId,
                                              @RequestHeader("Authorization") String token) {
        // TODO: 콘텐츠 삭제 로직 구현
        return SingleResponse.success("콘텐츠 삭제 성공");
    }

    @PostMapping("/{contentId}/like")
    public SingleResponse<String> likeContent(@PathVariable String contentId,
                                            @RequestHeader("Authorization") String token) {
        // TODO: 좋아요 로직 구현
        return SingleResponse.success("좋아요 성공");
    }

    @PostMapping("/upload")
    public SingleResponse<String> uploadImage(@RequestParam("file") MultipartFile file) {
        // TODO: 이미지 업로드 로직 구현
        return SingleResponse.success("이미지 업로드 성공");
    }

    // DTOs
    public static class CreateContentRequest {
        private String title;
        private String content;
        private String category;
        private List<String> tags;
        // getters, setters
    }

    public static class UpdateContentRequest {
        private String title;
        private String content;
        private String category;
        private List<String> tags;
        // getters, setters
    }

    public static class ContentSummary {
        private String id;
        private String title;
        private String authorName;
        private String category;
        private int likeCount;
        private int commentCount;
        private LocalDateTime createdAt;
        // getters, setters
    }

    public static class ContentDetail {
        private String id;
        private String title;
        private String content;
        private String authorName;
        private String category;
        private List<String> tags;
        private int likeCount;
        private int commentCount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        // getters, setters
    }
}
