package inu.codin.codin.domain.post.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CursorPageResponse<T> {
    private final List<T> items;
    private final String nextCursor; // 다음 페이지 커서 값
    private final boolean hasNext;
}