package inu.codin.codin.domain.calendar.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class CalendarDayResponse {

    @Schema(description = "날짜", example = "2025-08-23")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private final LocalDate date;

    @Schema(description = "해당 날짜의 총 이벤트 수", example = "2")
    private final int totalCont;

    @Schema(description = "해당 날짜의 이벤트 목록")
    private final List<EventDto> items;

    @JsonCreator
    public CalendarDayResponse(@JsonProperty("date") LocalDate date, @JsonProperty("totalCont") int totalCont, @JsonProperty("items") List<EventDto> items) {
        this.date = date;
        this.totalCont = totalCont;
        this.items = items;
    }
}
