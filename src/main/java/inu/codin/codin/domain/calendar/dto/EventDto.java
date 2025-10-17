package inu.codin.codin.domain.calendar.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import inu.codin.codin.common.dto.Department;
import inu.codin.codin.common.util.ObjectIdUtil;
import inu.codin.codin.domain.calendar.entity.CalendarEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
public class EventDto {

    @Schema(description = "이벤트 ID", example = "6214897124abc...")
    private final String eventId;

    @Schema(description = "이벤트 내용", example = "테스트 이벤트")
    private final String content;

    @Schema(description = "학과 또는 대학 전체 행사(OTHERS로 지정) ", example = "COMPUTER_SCI")
    private final Department department;

    @Builder
    @JsonCreator
    public EventDto(@JsonProperty("eventId") String eventId, @JsonProperty("content") String content, @JsonProperty("department") Department department) {
        this.eventId = eventId;
        this.content = content;
        this.department = department;
    }

    public static EventDto of(CalendarEntity calendarEntity) {
        return EventDto.builder()
                .eventId(ObjectIdUtil.toString(calendarEntity.getId()))
                .content(calendarEntity.getContent())
                .department(calendarEntity.getDepartment() != null ? calendarEntity.getDepartment() : null)
                .build();
    }
}
