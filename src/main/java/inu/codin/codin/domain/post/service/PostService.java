package inu.codin.codin.domain.post.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.common.security.exception.JwtException;
import inu.codin.codin.common.security.exception.SecurityErrorCode;
import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.post.domain.like.entity.LikeType;
import inu.codin.codin.domain.post.domain.like.service.LikeService;
import inu.codin.codin.domain.post.domain.scrap.service.ScrapService;
import inu.codin.codin.domain.post.dto.request.PostAnonymousUpdateRequestDTO;
import inu.codin.codin.domain.post.dto.request.PostContentUpdateRequestDTO;
import inu.codin.codin.domain.post.dto.request.PostCreateRequestDTO;
import inu.codin.codin.domain.post.dto.request.PostStatusUpdateRequestDTO;
import inu.codin.codin.domain.post.dto.response.PostDetailResponseDTO;
import inu.codin.codin.domain.post.dto.response.PostDetailResponseDTO.UserInfo;
import inu.codin.codin.domain.post.dto.response.PostPageResponse;
import inu.codin.codin.domain.post.entity.PostCategory;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.entity.PostStatus;
import inu.codin.codin.domain.post.repository.PostRepository;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.entity.UserRole;
import inu.codin.codin.domain.user.repository.UserRepository;
import inu.codin.codin.infra.redis.RedisService;
import inu.codin.codin.infra.s3.S3Service;
import inu.codin.codin.infra.s3.exception.ImageRemoveException;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;

    private final S3Service s3Service;
    private final LikeService likeService;
    private final ScrapService scrapService;
    private final RedisService redisService;
    private final UserRepository userRepository;

    public void createPost(PostCreateRequestDTO postCreateRequestDTO, List<MultipartFile> postImages) {
        List<String> imageUrls = s3Service.handleImageUpload(postImages);
        ObjectId userId = SecurityUtils.getCurrentUserId();

        if (SecurityUtils.getCurrentUserRole().equals(UserRole.USER) &&
                postCreateRequestDTO.getPostCategory().toString().split("_")[0].equals("EXTRACURRICULAR")){
            throw new JwtException(SecurityErrorCode.ACCESS_DENIED, "비교과 게시글에 대한 권한이 없습니다.");
        }

        PostEntity postEntity = PostEntity.builder()
                .userId(userId)
                .title(postCreateRequestDTO.getTitle())
                .content(postCreateRequestDTO.getContent())
                //이미지 Url List 저장
                .postImageUrls(imageUrls)
                .isAnonymous(postCreateRequestDTO.isAnonymous())
                .postCategory(postCreateRequestDTO.getPostCategory())
                //Default Status = Active
                .postStatus(PostStatus.ACTIVE)
                .build();
        postRepository.save(postEntity);
    }


    public void updatePostContent(String postId, PostContentUpdateRequestDTO requestDTO, List<MultipartFile> postImages) {

        PostEntity post = postRepository.findByIdAndNotDeleted(new ObjectId(postId))
                .orElseThrow(()->new NotFoundException("해당 게시물 없음"));
        validateUserAndPost(post);

        List<String> imageUrls = s3Service.handleImageUpload(postImages);

        post.updatePostContent(requestDTO.getContent(), imageUrls);
        postRepository.save(post);
    }

    public void updatePostAnonymous(String postId, PostAnonymousUpdateRequestDTO requestDTO) {
        PostEntity post = postRepository.findByIdAndNotDeleted(new ObjectId(postId))
                .orElseThrow(()->new NotFoundException("해당 게시물 없음"));
        validateUserAndPost(post);

        post.updatePostAnonymous(requestDTO.isAnonymous());
        postRepository.save(post);
    }
    public void updatePostStatus(String postId, PostStatusUpdateRequestDTO requestDTO) {
        PostEntity post = postRepository.findByIdAndNotDeleted(new ObjectId(postId))
                .orElseThrow(()->new NotFoundException("해당 게시물 없음"));
        validateUserAndPost(post);

        post.updatePostStatus(requestDTO.getPostStatus());
        postRepository.save(post);
    }
    private void validateUserAndPost(PostEntity post) {
        if (SecurityUtils.getCurrentUserRole().equals(UserRole.USER) &&
                post.getPostCategory().toString().split("_")[0].equals("EXTRACURRICULAR")){
            throw new JwtException(SecurityErrorCode.ACCESS_DENIED, "비교과 게시글에 대한 권한이 없습니다.");
        }
        SecurityUtils.validateUser(post.getUserId());
    }

    // 모든 글 반환 ::  게시글 내용 + 댓글+대댓글의 수 + 좋아요,스크랩 count 수 반환
    public PostPageResponse getAllPosts(PostCategory postCategory, int pageNumber) {
        PageRequest pageRequest = PageRequest.of(pageNumber, 20, Sort.by("createdAt").descending());
        Page<PostEntity> page;
        if (postCategory.equals(PostCategory.REQUEST) || postCategory.equals(PostCategory.COMMUNICATION) || postCategory.equals(PostCategory.EXTRACURRICULAR))
            page = postRepository.findByPostCategoryStartingWithOrderByCreatedAt(postCategory.toString(), pageRequest);
        else page = postRepository.findAllByCategoryOrderByCreatedAt(postCategory, pageRequest);
        return PostPageResponse.of(getPostListResponseDtos(page.getContent()), page.getTotalPages() - 1, page.hasNext() ? page.getPageable().getPageNumber() + 1 : -1);
    }

    public List<PostDetailResponseDTO> getPostListResponseDtos(List<PostEntity> posts) {
        // 1. 사용자 ID와 닉네임을 한 번에 조회하여 Map으로 변환
        // 닉네임 조회를 스트림 내부에서 진행하지 않고 매핑을 통해 한번에 처리 (중복 호출 최소화)
        Map<ObjectId, String> userNicknameMap = userRepository.findAllById(
                posts.stream().map(PostEntity::getUserId).distinct().toList()
        ).stream().collect(Collectors.toMap(UserEntity::get_id, UserEntity::getNickname));

        // 2. 게시글 처리
        return posts.stream()
                .sorted(Comparator.comparing(PostEntity::getCreatedAt).reversed())
                .map(post -> {
                    String nickname = post.isAnonymous() ? "익명" : userNicknameMap.get(post.getUserId());
                    return new PostDetailResponseDTO(
                            post.getUserId().toString(),
                            post.get_id().toString(),
                            post.getTitle(),
                            post.getContent(),
                            nickname,
                            post.getPostCategory(),
                            post.getPostImageUrls(),
                            post.isAnonymous(),
                            likeService.getLikeCount(LikeType.valueOf("POST"), post.get_id()), // 좋아요 수
                            scrapService.getScrapCount(post.get_id()), // 스크랩 수
                            redisService.getHitsCount(post.get_id()),
                            post.getCreatedAt(),
                            post.getCommentCount(),
                            getUserInfoAboutPost(post.get_id())
                    );
                })
                .toList();
    }

    //게시물 상세 조회 :: 게시글 (내용 + 좋아요,스크랩 count 수)  + 댓글 +대댓글 (내용 +좋아요,스크랩 count 수 ) 반환
    public PostDetailResponseDTO getPostWithDetail(String postId) {
        PostEntity post = postRepository.findByIdAndNotDeleted(new ObjectId(postId))
                .orElseThrow(() -> new NotFoundException("게시물을 찾을 수 없습니다."));

        ObjectId userId = SecurityUtils.getCurrentUserId();
        if (redisService.validateHits(post.get_id(), userId))
            redisService.addHits(post.get_id(), userId);

        String nickname = post.isAnonymous() ? "익명" : getNicknameByUserId(post.getUserId());
        return new PostDetailResponseDTO(
                post.getUserId().toString(),
                post.get_id().toString(),
                post.getTitle(),
                post.getContent(),
                nickname,
                post.getPostCategory(),
                post.getPostImageUrls(),
                post.isAnonymous(),
                likeService.getLikeCount(LikeType.valueOf("POST"),post.get_id()),   // 좋아요 수
                scrapService.getScrapCount(post.get_id()),   // 스크랩 수
                redisService.getHitsCount(post.get_id()),
                post.getCreatedAt(),
                post.getCommentCount(),
                getUserInfoAboutPost(post.get_id())
        );
    }

    public void softDeletePost(String postId) {
        PostEntity post = postRepository.findByIdAndNotDeleted(new ObjectId(postId))
                .orElseThrow(()-> new NotFoundException("게시물을 찾을 수 없음."));
        validateUserAndPost(post);

        post.delete();
        postRepository.save(post);

    }

    public void deletePostImage(String postId, String imageUrl) {
        PostEntity post = postRepository.findByIdAndNotDeleted(new ObjectId(postId))
                .orElseThrow(() -> new NotFoundException("게시물을 찾을 수 없습니다."));
        validateUserAndPost(post);

        if (!post.getPostImageUrls().contains(imageUrl))
            throw new NotFoundException("이미지가 게시물에 존재하지 않습니다.");

        try {
            // S3에서 이미지 삭제
            s3Service.deleteFile(imageUrl);
            // 게시물의 이미지 리스트에서 제거
            post.removePostImage(imageUrl);
            postRepository.save(post);
        } catch (Exception e) {
            throw new ImageRemoveException("이미지 삭제 중 오류 발생: " + imageUrl);
        }
    }

    public UserInfo getUserInfoAboutPost(ObjectId postId){
        ObjectId userId = SecurityUtils.getCurrentUserId();
        return UserInfo.builder()
                .isLike(redisService.isPostLiked(postId, userId))
                .isScrap(redisService.isPostScraped(postId, userId))
                .build();
    }

    //user id 기반 nickname 반환
    public String getNicknameByUserId(ObjectId userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        return user.getNickname();
    }

    public List<PostDetailResponseDTO> getTop3BestPosts() {
        Set<String> postIds = redisService.getTopNPosts(3);
        List<PostEntity> bestPosts = postIds.stream()
                .map(postId ->
                    postRepository.findById(new ObjectId(postId))
                            .orElseThrow(() -> new NotFoundException("게시글을 찾을 수 없습니다."))
                ).toList();
        return getPostListResponseDtos(bestPosts);
    }
}

