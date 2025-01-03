package inu.codin.codin.domain.post.repository;

import inu.codin.codin.domain.post.entity.PostCategory;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.entity.PostStatus;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends MongoRepository<PostEntity, ObjectId> {

    @Query("{'_id':  ?0, 'deletedAt': null, 'postStatus':  { $in:  ['ACTIVE'] }}")
    Optional<PostEntity> findByIdAndNotDeleted(ObjectId Id);
    @Query("{'deletedAt': null, 'postStatus':  { $in:  ['ACTIVE'] }, 'postCategory': ?0 }")
    Page<PostEntity> findAllByCategoryOrderByCreatedAt(PostCategory postCategory, PageRequest pageRequest);

    @Query("{'deletedAt': null, 'postStatus':  { $in:  ['ACTIVE'] }, 'userId': ?0 }")
    Page<PostEntity> findAllByUserIdOrderByCreatedAt(ObjectId userId, PageRequest pageRequest);

    Page<PostEntity> findByPostCategoryStartingWithAndDeletedAtIsNullAndPostStatusInOrderByCreatedAt(String prefix, PostStatus postStatus, PageRequest pageRequest);

    @Query("{ '$or': [ { 'content': { $regex: ?0, $options: 'i' } }, { 'title': { $regex: ?0, $options: 'i' } } ] }")
    Page<PostEntity> findAllByKeywordAndDeletedAtIsNull(String keyword, PageRequest pageRequest);
}
