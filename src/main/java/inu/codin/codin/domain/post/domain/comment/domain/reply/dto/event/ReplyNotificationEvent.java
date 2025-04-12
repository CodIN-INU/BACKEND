package inu.codin.codin.domain.post.domain.comment.domain.reply.dto.event;

import inu.codin.codin.domain.post.entity.PostCategory;
import lombok.Getter;
import org.bson.types.ObjectId;
import org.springframework.context.ApplicationEvent;

@Getter
public class ReplyNotificationEvent extends ApplicationEvent {
    private final PostCategory postCategory;

    private final ObjectId userId;

    private final String postId;

    private final String content;

    public ReplyNotificationEvent(Object source, PostCategory postCategory, ObjectId userId, String postId, String content) {
        super(source);
        this.postCategory = postCategory;
        this.userId = userId;
        this.postId = postId;
        this.content = content;
    }
}
