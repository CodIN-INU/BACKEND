package inu.codin.codin.domain.post.service;

import inu.codin.codin.common.util.ObjectIdUtil;
import inu.codin.codin.domain.block.service.BlockService;
import inu.codin.codin.domain.post.dto.response.CursorPageResponse;
import inu.codin.codin.domain.post.dto.response.PostDetailResponseDTO;
import inu.codin.codin.domain.post.entity.PostCategory;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.repository.PostReadRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostCursorService {

    private final PostReadRepository postReadRepository;
    private final BlockService blockService;
    private final PostService postService;

    /**
     * 커서 기반 조회 서비스 로직
     * @param category 게시글 카테고리
     * @param cursor 마지막 커서 id
     * @param limit 조회 개수
     * @return CursorPageResponse<PostDetailResponseDTO> 반환
     */
    public CursorPageResponse<PostDetailResponseDTO> getAllPostsByCursorIdOnly(PostCategory category, String cursor, int limit) {
        List<ObjectId> blockedUsers = blockService.getBlockedUsers();

        // objectId가 24자리 16진수 검증
        ObjectId lastId = null;
        if (cursor != null && cursor.matches("^[a-fA-F0-9]{24}$")) {
            lastId = ObjectIdUtil.toObjectId(cursor);
        } else if (cursor != null && !cursor.isBlank()) {
            throw new IllegalArgumentException("cursor는 24자리 hex(ObjectId)여야 합니다.");
        }

        List<PostEntity> batch = postReadRepository.findByCategoryWithIdCursor(category.name(), blockedUsers, lastId, limit);

        // 다음 페이지 계산
        boolean hasNext = batch.size() > limit;
        if (hasNext) batch = batch.subList(0, limit);

        // 다음 커서 id 생성
        String nextCursor = null;
        if (!batch.isEmpty()) {
            PostEntity tail = batch.get(batch.size() - 1);
            nextCursor = ObjectIdUtil.toString(tail.get_id());
        }

        List<PostDetailResponseDTO> items = postService.getPostListResponseDtos(batch);
        return CursorPageResponse.<PostDetailResponseDTO>builder()
                .items(items)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .build();
    }
}