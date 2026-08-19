package com.gaguraczi.paw.domain.walk.service;

import com.gaguraczi.paw.domain.users.entity.Pet;
import com.gaguraczi.paw.domain.users.repository.PetRepository;
import com.gaguraczi.paw.domain.walk.converter.WalkConverter;
import com.gaguraczi.paw.domain.walk.dto.request.WalkCreateRequest;
import com.gaguraczi.paw.domain.walk.dto.request.WalkFinishRequest;
import com.gaguraczi.paw.domain.walk.dto.request.WalkStartRequest;
import com.gaguraczi.paw.domain.walk.dto.request.WalkUpdateRequest;
import com.gaguraczi.paw.domain.walk.dto.response.WalkDailyStatResponse;
import com.gaguraczi.paw.domain.walk.dto.response.WalkIdResponse;
import com.gaguraczi.paw.domain.walk.dto.response.WalkResponse;
import com.gaguraczi.paw.domain.walk.dto.response.WalkStartResponse;
import com.gaguraczi.paw.domain.walk.dto.response.WalkSummaryResponse;
import com.gaguraczi.paw.domain.walk.dto.response.WalkWeeklySummaryResponse;
import com.gaguraczi.paw.domain.walk.entity.WalkEntity;
import com.gaguraczi.paw.domain.walk.enums.WalkStatusEnum;
import com.gaguraczi.paw.domain.walk.enums.WalkTypeEnum;
import com.gaguraczi.paw.domain.walk.enums.WeatherTypeEnum;
import com.gaguraczi.paw.domain.walk.exception.WalkErrorCode;
import com.gaguraczi.paw.domain.walk.repository.WalkRepository;
import com.gaguraczi.paw.domain.walkcourse.entity.WalkCourseEntity;
import com.gaguraczi.paw.domain.walkcourse.repository.WalkCourseRepository;
import com.gaguraczi.paw.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalkService {


    private static final long MAX_STAT_DAYS = 366;

    private final WalkRepository walkRepository;
    private final PetRepository petRepository;
    private final WalkCourseRepository walkCourseRepository;

   // 산책 기록 수동 저장
    @Transactional
    public WalkResponse createWalk(WalkCreateRequest request) {
        Pet pet = getPetOrThrow(request.getPetId());
        WalkCourseEntity course = findCourseOrNull(request.getCourseId(), request.getPetId());

        validateWalkTime(request.getStartTime(), request.getEndTime());
        validateNotFuture(request.getWalkDate());

        WalkEntity walk = WalkConverter.toWalk(request, pet, course);
        WalkEntity saved = walkRepository.save(walk);

        markCourseUsed(course, saved.getStartTime());

        return WalkConverter.toWalkResponse(saved);
    }

    //타이머 시작
    @Transactional
    public WalkStartResponse startWalk(WalkStartRequest request) {
        Pet pet = getPetOrThrow(request.getPetId());
        WalkCourseEntity course = findCourseOrNull(request.getCourseId(), request.getPetId());

        if (walkRepository.existsByPet_PetIdAndWalkStatus(request.getPetId(), WalkStatusEnum.IN_PROGRESS)) {
            throw new GeneralException(WalkErrorCode.WALK_ALREADY_IN_PROGRESS);
        }

        LocalDateTime startTime = (request.getStartTime() != null)
                ? request.getStartTime()
                : LocalDateTime.now();
        validateNotFuture(startTime.toLocalDate());


        WalkEntity walk = WalkConverter.toStartedWalk(request, pet, course, startTime);
        WalkEntity saved = walkRepository.save(walk);

        return WalkConverter.toWalkStartResponse(saved);
    }

   // 타이머 종료
    @Transactional
    public WalkResponse finishWalk(Long walkId, WalkFinishRequest request) {
        WalkEntity walk = getWalkOrThrow(walkId);

        if (walk.isCompleted()) {
            throw new GeneralException(WalkErrorCode.WALK_ALREADY_FINISHED);
        }

        LocalDateTime endTime = (request.getEndTime() != null)
                ? request.getEndTime()
                : LocalDateTime.now();

        validateWalkTime(walk.getStartTime(), endTime);

        WalkCourseEntity course = findCourseOrNull(request.getCourseId(), walk.getPet().getPetId());

        walk.finish(
                endTime,
                course,
                request.getWalkingAmount(),
                WalkTypeEnum.from(request.getWalkType()),
                request.getIsStool(),
                request.getIsUrine(),
                request.getSignificant()
        );

        markCourseUsed(walk.getWalkCourse(), endTime);

        return WalkConverter.toWalkResponse(walk);
    }


    public WalkResponse getInProgressWalk(Long petId) {
        WalkEntity walk = walkRepository
                .findFirstByPet_PetIdAndWalkStatusOrderByStartTimeDesc(petId, WalkStatusEnum.IN_PROGRESS)
                .orElseThrow(() -> new GeneralException(WalkErrorCode.WALK_IN_PROGRESS_NOT_FOUND));

        return WalkConverter.toWalkResponse(walk);
    }

    //산책 기록 조회
    public WalkResponse getWalk(Long walkId) {
        return WalkConverter.toWalkResponse(getWalkOrThrow(walkId));
    }

    //산책 기록 목록 조회
    public List<WalkSummaryResponse> getWalks(Long petId, LocalDate date,
                                              LocalDate startDate, LocalDate endDate) {
        List<WalkEntity> walks;

        if (date != null) {
            walks = walkRepository.findAllByPet_PetIdAndWalkDateOrderByStartTimeDesc(petId, date);
        } else if (startDate != null && endDate != null) {
            validateDateRange(startDate, endDate);
            walks = walkRepository
                    .findAllByPet_PetIdAndWalkDateBetweenOrderByWalkDateDescStartTimeDesc(petId, startDate, endDate);
        } else {
            walks = walkRepository.findAllByPet_PetIdOrderByWalkDateDescStartTimeDesc(petId);
        }

        return WalkConverter.toWalkSummaryResponseList(walks);
    }



   //주간 요약
    public WalkWeeklySummaryResponse getWeeklySummary(Long petId, LocalDate baseDate) {
        LocalDate base = (baseDate != null) ? baseDate : LocalDate.now();

        LocalDate thisWeekStart = base.with(DayOfWeek.MONDAY);
        LocalDate thisWeekEnd = thisWeekStart.plusDays(6);
        LocalDate lastWeekStart = thisWeekStart.minusWeeks(1);
        LocalDate lastWeekEnd = thisWeekStart.minusDays(1);

        List<WalkEntity> thisWeek = walkRepository
                .findAllByPet_PetIdAndWalkDateBetweenOrderByWalkDateDescStartTimeDesc(
                        petId, thisWeekStart, thisWeekEnd);
        List<WalkEntity> lastWeek = walkRepository
                .findAllByPet_PetIdAndWalkDateBetweenOrderByWalkDateDescStartTimeDesc(
                        petId, lastWeekStart, lastWeekEnd);

        long thisWeekTotal = sumMinutes(thisWeek);
        int thisWeekCount = countCompleted(thisWeek);

        return WalkConverter.toWalkWeeklySummaryResponse(
                thisWeekStart,
                thisWeekEnd,
                average(thisWeekTotal, thisWeekCount),
                average(sumMinutes(lastWeek), countCompleted(lastWeek)),
                thisWeekCount,
                thisWeekTotal,
                sumDistance(thisWeek)
        );
    }

   //일 별 통계
    public List<WalkDailyStatResponse> getDailyStats(Long petId, LocalDate startDate, LocalDate endDate) {
        LocalDate end = (endDate != null) ? endDate : LocalDate.now();
        LocalDate start = (startDate != null) ? startDate : end.minusDays(6); // 기본 최근 7일

        validateDateRange(start, end);

        List<WalkEntity> walks = walkRepository
                .findAllByPet_PetIdAndWalkDateBetweenOrderByWalkDateDescStartTimeDesc(petId, start, end);

        List<WalkDailyStatResponse> result = new ArrayList<>();

        for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1)) {
            final LocalDate target = day;
            List<WalkEntity> ofDay = walks.stream()
                    .filter(w -> target.equals(w.getWalkDate()))
                    .toList();

            result.add(WalkConverter.toWalkDailyStatResponse(
                    target,
                    sumMinutes(ofDay),
                    sumDistance(ofDay),
                    countCompleted(ofDay)
            ));
        }

        return result;
    }


    @Transactional
    public WalkResponse updateWalk(Long walkId, WalkUpdateRequest request) {
        WalkEntity walk = getWalkOrThrow(walkId);

        LocalDateTime newStart = (request.getStartTime() != null) ? request.getStartTime() : walk.getStartTime();
        LocalDateTime newEnd = (request.getEndTime() != null) ? request.getEndTime() : walk.getEndTime();
        validateWalkTime(newStart, newEnd);

        if (request.getWalkDate() != null) {
            validateNotFuture(request.getWalkDate());
        }

        WalkCourseEntity course = findCourseOrNull(request.getCourseId(), walk.getPet().getPetId());

        walk.update(
                course,
                WeatherTypeEnum.fromNullable(request.getWeatherType()),
                request.getWalkingAmount(),
                WalkTypeEnum.fromNullable(request.getWalkType()),
                request.getStartTime(),
                request.getEndTime(),
                request.getWalkDate(),
                request.getTemp(),
                request.getIsStool(),
                request.getIsUrine(),
                request.getSignificant()
        );

        return WalkConverter.toWalkResponse(walk);
    }


    @Transactional
    public WalkIdResponse deleteWalk(Long walkId) {
        WalkEntity walk = getWalkOrThrow(walkId);
        walkRepository.delete(walk);
        return WalkConverter.toWalkIdResponse(walkId);
    }


    private WalkEntity getWalkOrThrow(Long walkId) {
        return walkRepository.findById(walkId)
                .orElseThrow(() -> new GeneralException(WalkErrorCode.WALK_NOT_FOUND));
    }

    private Pet getPetOrThrow(Long petId) {
        return petRepository.findById(petId)
                .orElseThrow(() -> new GeneralException(WalkErrorCode.PET_NOT_FOUND));
    }


    private WalkCourseEntity findCourseOrNull(Long courseId, Long petId) {
        if (courseId == null) {
            return null;
        }
        WalkCourseEntity course = walkCourseRepository.findById(courseId)
                .orElseThrow(() -> new GeneralException(WalkErrorCode.WALK_COURSE_NOT_FOUND));

        if (!course.isOwnedBy(petId)) {
            throw new GeneralException(WalkErrorCode.WALK_COURSE_FORBIDDEN);
        }
        return course;
    }

    private void markCourseUsed(WalkCourseEntity course, LocalDateTime usedAt) {
        if (course != null) {
            course.markUsed(usedAt);
        }
    }


    private void validateWalkTime(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return;
        }
        if (endTime.isBefore(startTime)) {
            throw new GeneralException(WalkErrorCode.WALK_TIME_INVALID);
        }
    }

    private void validateNotFuture(LocalDate walkDate) {
        if (walkDate != null && walkDate.isAfter(LocalDate.now())) {
            throw new GeneralException(WalkErrorCode.WALK_FUTURE_DATE);
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new GeneralException(WalkErrorCode.WALK_DATE_RANGE_INVALID);
        }
        if (ChronoUnit.DAYS.between(startDate, endDate) >= MAX_STAT_DAYS) {
            throw new GeneralException(WalkErrorCode.WALK_STAT_RANGE_TOO_LONG);
        }
    }


    private long sumMinutes(List<WalkEntity> walks) {
        return walks.stream()
                .map(WalkEntity::getDurationMinutes)
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .sum();
    }

    private BigDecimal sumDistance(List<WalkEntity> walks) {
        return walks.stream()
                .map(WalkEntity::getWalkingAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int countCompleted(List<WalkEntity> walks) {
        return (int) walks.stream()
                .filter(WalkEntity::isCompleted)
                .count();
    }


    private long average(long total, int count) {
        return (count == 0) ? 0L : total / count;
    }
}
