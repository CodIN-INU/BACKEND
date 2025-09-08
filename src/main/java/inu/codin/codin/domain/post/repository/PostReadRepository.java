package inu.codin.codin.domain.post.repository;

import inu.codin.codin.domain.post.entity.PostEntity;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.regex.Pattern;

@Repository
@RequiredArgsConstructor
public class PostReadRepository {

    private final MongoTemplate mongoTemplate;

    /**
     * 커서 기반 MongoDB posts 컬렉션 조회 기능
     * @param postCategory 게시물 카테고리 PostCategory.
     * @param blockedUsers 차단된 유저 id 리스트
     * @param cursorId 커서 id - 마지막으로 본 post
     * @param limit 페이지 크기
     * @return List<PostEntity>
     */
    public List<PostEntity> findByCategoryWithIdCursor(String postCategory, List<ObjectId> blockedUsers, ObjectId cursorId, int limit) {
        Criteria base = new Criteria().andOperator(
                new Criteria().orOperator(
                        Criteria.where("deleted_at").is(null),
                        Criteria.where("deleted_at").exists(false)
                ),
                Criteria.where("postStatus").is("ACTIVE"),
                Criteria.where("postCategory").is(postCategory)
        );

        // 차단 유저가 없을 때
        if (blockedUsers != null && !blockedUsers.isEmpty()) {
            base = new Criteria().andOperator(base, Criteria.where("userId").nin(blockedUsers));
        }
        // 첫번째 페이지 조회
        if (cursorId != null) {
            base = new Criteria().andOperator(base, Criteria.where("_id").lt(cursorId));
        }

        Query query = new Query(base)
                .with(Sort.by(Sort.Direction.DESC, "_id"))
                .limit(limit + 1);

        return mongoTemplate.find(query, PostEntity.class, "posts");
    }
}