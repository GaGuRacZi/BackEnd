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
import com.gaguraczi.paw.domain.walk.enums.WalkTypeEnum;
import com.gaguraczi.paw.domain.walk.enums.WeatherTypeEnum;
import com.gaguraczi.paw.domain.walk.exception.WalkErrorCode;
import com.gaguraczi.paw.domain.walk.redis.WalkInProgressRedisStore;
import com.gaguraczi.paw.domain.walk.redis.WalkInProgressSession;
import com.gaguraczi.paw.domain.walk.repository.WalkRepository;
import com.gaguraczi.paw.global.exception.GeneralException;
import com.gaguraczi.paw.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.Clock;
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
    private final WalkInProgressRedisStore walkInProgressRedisStore;
    private final SecurityUtils securityUtils;
    private final Clock clock;

   // 산책 기록 수동 저장
    @Transactional
    public WalkResponse createWalk(WalkCreateRequest request) {
        Pet pet = loadOwnedPet(request.getPetId());

        validateWalkTime(request.getStartTime(), request.getEndTime());
        validateNotFuture(request.getStartTime().toLocalDate());

        WalkEntity walk = WalkConverter.toWalk(request, pet);
        WalkEntity saved = walkRepository.save(walk);

        return WalkConverter.toWalkResponse(saved);
    }

    //타이머 시작 — DB에 쓰지 않고 Redis에 6시간 보관
    public WalkStartResponse startWalk(WalkStartRequest request) {
        loadOwnedPet(request.getPetId());

        LocalDateTime startTime = (request.getStartTime() != null)
                ? request.getStartTime()
                : LocalDateTime.now(clock);
        validateNotFuture(startTime.toLocalDate());

        WalkInProgressSession session = WalkConverter.toInProgressSession(
                request.getPetId(),
                startTime
        );

        if (!walkInProgressRedisStore.saveIfAbsent(session)) {
            throw GeneralException.of(WalkErrorCode.WALK_ALREADY_IN_PROGRESS);
        }

        return WalkConverter.toWalkStartResponse(session);
    }

   // 타이머 종료 — Redis 세션을 꺼내 완료 기록만 DB에 저장
    @Transactional
    public WalkResponse finishWalk(WalkFinishRequest request) {
        Pet pet = loadOwnedPet(request.getPetId());

        String json = walkInProgressRedisStore.getRaw(request.getPetId())
                .orElseThrow(() -> GeneralException.of(WalkErrorCode.WALK_IN_PROGRESS_NOT_FOUND));
        WalkInProgressSession session = walkInProgressRedisStore.parse(json);
        if (session.isProcessing()) {
            throw GeneralException.of(WalkErrorCode.WALK_ALREADY_FINISHED);
        }

        String processingJson = walkInProgressRedisStore.markProcessingIfUnchanged(request.getPetId(), json)
                .orElseThrow(() -> GeneralException.of(WalkErrorCode.WALK_ALREADY_FINISHED));
        registerFinishSessionCleanup(request.getPetId(), processingJson, json);

        LocalDateTime endTime = (request.getEndTime() != null)
                ? request.getEndTime()
                : LocalDateTime.now(clock);

        validateWalkTime(session.getStartTime(), endTime);

        WalkEntity walk = WalkConverter.toFinishedWalk(session, pet, request, endTime);
        WalkEntity saved = walkRepository.save(walk);

        return WalkConverter.toWalkResponse(saved);
    }


    public WalkResponse getInProgressWalk(Long petId) {
        loadOwnedPet(petId);
        WalkInProgressSession session = walkInProgressRedisStore.getAndRefreshTtl(petId)
                .orElseThrow(() -> GeneralException.of(WalkErrorCode.WALK_IN_PROGRESS_NOT_FOUND));

        return WalkConverter.toInProgressWalkResponse(session);
    }

    //산책 기록 조회
    public WalkResponse getWalk(Long walkId) {
        return WalkConverter.toWalkResponse(loadOwnedWalk(walkId));
    }

    //산책 기록 목록 조회
    public List<WalkSummaryResponse> getWalks(Long petId, LocalDate date,
                                              LocalDate startDate, LocalDate endDate) {
        loadOwnedPet(petId);
        List<WalkEntity> walks;

        if (date != null) {
            walks = walkRepository.findAllByPet_PetIdAndWalkDateOrderByStartTimeDesc(petId, date);
        } else if (startDate != null && endDate != null) {
            validateDateRange(startDate, endDate);
            walks = walkRepository
                    .findAllByPet_PetIdAndWalkDateBetweenOrderByWalkDateDescStartTimeDesc(petId, startDate, endDate);
        } else if (startDate != null || endDate != null) {
            throw GeneralException.of(WalkErrorCode.WALK_DATE_RANGE_INVALID);
        } else {
            walks = walkRepository.findAllByPet_PetIdOrderByWalkDateDescStartTimeDesc(petId);
        }

        return WalkConverter.toWalkSummaryResponseList(walks);
    }



   //주간 요약
    public WalkWeeklySummaryResponse getWeeklySummary(Long petId, LocalDate baseDate) {
        loadOwnedPet(petId);
        LocalDate base = (baseDate != null) ? baseDate : LocalDate.now(clock);

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
        loadOwnedPet(petId);
        LocalDate end = (endDate != null) ? endDate : LocalDate.now(clock);
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
        WalkEntity walk = loadOwnedWalk(walkId);

        LocalDateTime newStart = (request.getStartTime() != null) ? request.getStartTime() : walk.getStartTime();
        LocalDateTime newEnd = (request.getEndTime() != null) ? request.getEndTime() : walk.getEndTime();
        validateWalkTime(newStart, newEnd);

        if (request.getWalkDate() != null) {
            validateNotFuture(request.getWalkDate());
        }

        walk.update(
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
        WalkEntity walk = loadOwnedWalk(walkId);
        walkRepository.delete(walk);
        return WalkConverter.toWalkIdResponse(walkId);
    }


    private WalkEntity loadOwnedWalk(Long walkId) {
        WalkEntity walk = walkRepository.findById(walkId)
                .orElseThrow(() -> GeneralException.of(WalkErrorCode.WALK_NOT_FOUND));
        if (!walk.getPet().getUser().getUid().equals(securityUtils.currentUser().getUid())) {
            throw GeneralException.of(WalkErrorCode.WALK_FORBIDDEN);
        }
        return walk;
    }

    private Pet loadOwnedPet(Long petId) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> GeneralException.of(WalkErrorCode.PET_NOT_FOUND));
        if (!pet.getUser().getUid().equals(securityUtils.currentUser().getUid())) {
            throw GeneralException.of(WalkErrorCode.PET_NOT_FOUND);
        }
        return pet;
    }


    private void validateWalkTime(LocalDateTime startTime, LocalDateTime endTime) {
        if (endTime != null && endTime.isAfter(LocalDateTime.now(clock))) {
            throw GeneralException.of(WalkErrorCode.WALK_TIME_INVALID);
        }
        if (startTime == null || endTime == null) {
            return;
        }
        if (endTime.isBefore(startTime)) {
            throw GeneralException.of(WalkErrorCode.WALK_TIME_INVALID);
        }
    }

    private void validateNotFuture(LocalDate walkDate) {
        if (walkDate != null && walkDate.isAfter(LocalDate.now(clock))) {
            throw GeneralException.of(WalkErrorCode.WALK_FUTURE_DATE);
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw GeneralException.of(WalkErrorCode.WALK_DATE_RANGE_INVALID);
        }
        if (ChronoUnit.DAYS.between(startDate, endDate) >= MAX_STAT_DAYS) {
            throw GeneralException.of(WalkErrorCode.WALK_STAT_RANGE_TOO_LONG);
        }
    }

    private void registerFinishSessionCleanup(Long petId, String processingJson, String originalJson) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            walkInProgressRedisStore.removeIfUnchanged(petId, processingJson);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                walkInProgressRedisStore.removeIfUnchanged(petId, processingJson);
            }

            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    walkInProgressRedisStore.restoreIfUnchanged(petId, processingJson, originalJson);
                }
            }
        });
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
