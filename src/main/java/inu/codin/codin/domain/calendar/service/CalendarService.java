package inu.codin.codin.domain.calendar.service;

import inu.codin.codin.common.util.ObjectIdUtil;
import inu.codin.codin.domain.calendar.dto.*;
import inu.codin.codin.domain.calendar.entity.CalendarEntity;
import inu.codin.codin.domain.calendar.exception.CalendarErrorCode;
import inu.codin.codin.domain.calendar.exception.CalendarException;
import inu.codin.codin.domain.calendar.repository.CalendarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarService {

    private final CalendarRepository calendarRepository;

    private final String CACHE_NAME =  "calendar";
    private final CacheManager cacheManager;

    /**
     * 캘린더 조회
     * + 조회 캐시 Write-Through 전략 사용
     * @param year 년도 ex) 2025
     * @param month 월 ex) 8
     * @return CalendarMonthResponse
     */
    @Cacheable(value = CACHE_NAME, key = "'month:' + T(java.time.YearMonth).of(#year, #month)")
    public CalendarMonthResponse getMonth(int year, int month) {
        log.info("Getting month for year {} and month {}", year, month);
        if (month < 1 || month > 12) {
            throw new CalendarException(CalendarErrorCode.DATE_FORMAT_ERROR);
        }

        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

        List<CalendarEntity> calendarEventList = calendarRepository
                .findByEndDateGreaterThanEqualAndStartDateLessThanEqual(monthStart, monthEnd).stream()
                .filter(e -> e.getStartDate() != null && e.getEndDate() != null)
                .toList();

        // 날짜 별로 클램핑
        Map<LocalDate, List<CalendarEntity>> days = new HashMap<>();
        for (CalendarEntity e : calendarEventList) {
            LocalDate startDate = e.getStartDate().isBefore(monthStart) ? monthStart : e.getStartDate();
            LocalDate endDate = e.getEndDate().isAfter(monthEnd) ? monthEnd : e.getEndDate();
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                days.computeIfAbsent(date, k -> new ArrayList<>()).add(e);
            }
        }

        // 날짜 별로 정렬하기
        List<CalendarDayResponse> dayResponses = new ArrayList<>();
        for (LocalDate date = monthStart; !date.isAfter(monthEnd); date = date.plusDays(1)) {
            List<CalendarEntity> list = new ArrayList<>(days.getOrDefault(date, Collections.emptyList()));
            list.sort(Comparator.comparing(CalendarEntity::getStartDate));

            int total = list.size();
            List<EventDto> events = list.stream()
                    .map(EventDto::of)
                    .toList();
            dayResponses.add(new CalendarDayResponse(date, total, events));
        }
        return CalendarMonthResponse.builder()
                .year(year)
                .month(month)
                .days(dayResponses)
                .build();
    }

    /**
     * 켈린더 이벤트 생성
     * + 기존 엔티티의 시간을 기준으로 캐시를 무효화
     * @param request CalendarCreateRequest
     * @return CalendarCreateResponse
     */
    public CalendarCreateResponse create(CalendarCreateRequest request) {
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new CalendarException(CalendarErrorCode.DATE_CANNOT_NULL);
        }
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new CalendarException(CalendarErrorCode.DATE_FORMAT_ERROR);
        }

        CalendarEntity entity = CalendarEntity.builder()
                .content(request.getContent())
                .department(request.getDepartment())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        CalendarEntity savedEntity = calendarRepository.save(entity);
        evictMonthBetween(savedEntity.getStartDate(), savedEntity.getEndDate());

        return CalendarCreateResponse.of(savedEntity);
    }

    /**
     * 켈린더 이벤트 삭제(SoftDelete)
     * + 기존 엔티티의 시간을 기준으로 캐시를 무효화
     * @param id
     */
    public void delete(String id) {
        ObjectId objectId = ObjectIdUtil.toObjectId(id);
        CalendarEntity calendar = calendarRepository.findByIdAndNotDeleted(objectId)
                .orElseThrow(() -> new CalendarException(CalendarErrorCode.CALENDAR_EVENT_NOT_FOUND));

        calendar.delete();
        calendarRepository.save(calendar);

        LocalDate start = calendar.getStartDate();
        LocalDate end = calendar.getEndDate();
        evictMonthBetween(start, end);
    }

    /**
     * 캐시 무효화
     * @param startDate
     * @param endDate
     */
    private void evictMonthBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) return;
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache == null) return;

        YearMonth from = YearMonth.from(startDate);
        YearMonth to = YearMonth.from(endDate);

        for (YearMonth ym = from; !ym.isAfter(to); ym = ym.plusMonths(1)) {
            String key = "month:" + ym;
            log.info("Evicting month {} from {} to {}", ym, from, to);
            cache.evictIfPresent(key);
        }
    }
}
