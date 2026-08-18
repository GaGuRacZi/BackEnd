package com.gaguraczi.paw.domain.weights.service;

import com.gaguraczi.paw.domain.pets.exception.code.PetErrorCode;
import com.gaguraczi.paw.domain.users.entity.Pet;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.domain.users.repository.PetRepository;
import com.gaguraczi.paw.domain.weights.dto.req.PetWeightCreateReq;
import com.gaguraczi.paw.domain.weights.dto.req.PetWeightUpdateReq;
import com.gaguraczi.paw.domain.weights.dto.res.PetWeightGraphRes;
import com.gaguraczi.paw.domain.weights.dto.res.PetWeightPointRes;
import com.gaguraczi.paw.domain.weights.dto.res.PetWeightRes;
import com.gaguraczi.paw.domain.weights.dto.res.PetWeightSummaryRes;
import com.gaguraczi.paw.domain.weights.entity.PetWeightEntity;
import com.gaguraczi.paw.domain.weights.enums.WeightGraphPeriodEnum;
import com.gaguraczi.paw.domain.weights.exception.code.PetWeightErrorCode;
import com.gaguraczi.paw.domain.weights.repository.PetWeightRepository;
import com.gaguraczi.paw.global.exception.GeneralException;
import com.gaguraczi.paw.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PetWeightService {


    private static final BigDecimal ABNORMAL_CHANGE_RATE = new BigDecimal("0.10");
    private static final String COMMENT_STABLE = "급격한 변화는 없어요";
    private static final String COMMENT_ABNORMAL = "최근 체중이 급격하게 변했어요";
    private static final String COMMENT_NO_RECORD = "아직 체중 기록이 없어요";

    private final PetWeightRepository petWeightRepository;
    private final PetRepository petRepository;
    private final SecurityUtils securityUtils;



    @Transactional
    public PetWeightRes create(Long petId, PetWeightCreateReq req) {
        Pet pet = loadOwnedPet(petId);
        validateRecordedAt(req.recordedAt());

        PetWeightEntity petWeight = PetWeightEntity.builder()
                .pet(pet)
                .weight(req.weight())
                .bodyType(req.bodyType())
                .appetiteType(req.appetiteType())
                .memoContent(blankToNull(req.memoContent()))
                .recordedAt(req.recordedAt())
                .build();

        petWeightRepository.save(petWeight);
        syncPetCurrentWeight(pet);

        return PetWeightRes.from(petWeight);
    }

    @Transactional
    public PetWeightRes update(Long petId, Long petWeightId, PetWeightUpdateReq req) {
        Pet pet = loadOwnedPet(petId);
        PetWeightEntity petWeight = loadRecord(pet, petWeightId);

        if (req != null) {
            validateRecordedAt(req.recordedAt());
            petWeight.update(
                    req.weight(),
                    req.bodyType(),
                    req.appetiteType(),
                    req.memoContent(),
                    req.recordedAt()
            );
        }

        syncPetCurrentWeight(pet);
        return PetWeightRes.from(petWeight);
    }

    @Transactional
    public void delete(Long petId, Long petWeightId) {
        Pet pet = loadOwnedPet(petId);
        PetWeightEntity petWeight = loadRecord(pet, petWeightId);

        petWeightRepository.delete(petWeight);
        petWeightRepository.flush();
        syncPetCurrentWeight(pet);
    }



    public PetWeightRes get(Long petId, Long petWeightId) {
        Pet pet = loadOwnedPet(petId);
        return PetWeightRes.from(loadRecord(pet, petWeightId));
    }


    public List<PetWeightRes> getMonthlyRecords(Long petId, Integer year, Integer month) {
        Pet pet = loadOwnedPet(petId);
        YearMonth target = resolveYearMonth(year, month);

        List<PetWeightEntity> records = petWeightRepository.findAllByPetAndRecordedAtBetweenOrderByRecordedAtAsc(
                pet,
                target.atDay(1).atStartOfDay(),
                target.atEndOfMonth().atTime(LocalTime.MAX)
        );

        return records.stream()
                .sorted((a, b) -> b.getRecordedAt().compareTo(a.getRecordedAt()))
                .map(PetWeightRes::from)
                .toList();
    }


    public PetWeightGraphRes getGraph(Long petId, WeightGraphPeriodEnum period) {
        Pet pet = loadOwnedPet(petId);
        WeightGraphPeriodEnum target = period != null ? period : WeightGraphPeriodEnum.ONE_MONTH;

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(target.getMonths()).plusDays(1);
        if (startDate.isAfter(endDate)) {
            throw GeneralException.of(PetWeightErrorCode.PET_WEIGHT_INVALID_PERIOD);
        }

        List<PetWeightEntity> records = petWeightRepository.findAllByPetAndRecordedAtBetweenOrderByRecordedAtAsc(
                pet, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));


        Map<LocalDate, BigDecimal> bucket = new LinkedHashMap<>();
        for (PetWeightEntity record : records) {
            bucket.put(bucketKey(record.getRecordedAt(), target), record.getWeight());
        }

        List<PetWeightPointRes> points = bucket.entrySet().stream()
                .map(entry -> PetWeightPointRes.of(entry.getKey(), entry.getValue()))
                .toList();

        BigDecimal min = points.stream().map(PetWeightPointRes::weight)
                .min(BigDecimal::compareTo).orElse(null);
        BigDecimal max = points.stream().map(PetWeightPointRes::weight)
                .max(BigDecimal::compareTo).orElse(null);

        return PetWeightGraphRes.of(target, startDate, endDate, min, max, points);
    }


    public PetWeightSummaryRes getSummary(Long petId) {
        Pet pet = loadOwnedPet(petId);

        List<PetWeightEntity> recent = petWeightRepository.findTop2ByPetOrderByRecordedAtDescPetWeightIdDesc(pet);
        if (recent.isEmpty()) {
            return PetWeightSummaryRes.of(petId, pet.getPetWeight(), null, null, false, COMMENT_NO_RECORD);
        }

        PetWeightEntity latest = recent.get(0);
        BigDecimal monthChange = calculateMonthChange(pet, latest);
        boolean abnormal = recent.size() > 1 && isAbnormalChange(recent.get(1).getWeight(), latest.getWeight());

        return PetWeightSummaryRes.of(
                petId,
                latest.getWeight(),
                latest.getRecordedAt(),
                monthChange,
                abnormal,
                abnormal ? COMMENT_ABNORMAL : COMMENT_STABLE
        );
    }



    private BigDecimal calculateMonthChange(Pet pet, PetWeightEntity latest) {
        LocalDateTime monthStart = YearMonth.now().atDay(1).atStartOfDay();

        BigDecimal baseline = petWeightRepository
                .findFirstByPetAndRecordedAtLessThanOrderByRecordedAtDescPetWeightIdDesc(pet, monthStart)
                .map(PetWeightEntity::getWeight)
                .orElse(null);

        if (baseline == null) {
            List<PetWeightEntity> thisMonth = petWeightRepository.findAllByPetAndRecordedAtBetweenOrderByRecordedAtAsc(
                    pet, monthStart, YearMonth.now().atEndOfMonth().atTime(LocalTime.MAX));
            if (thisMonth.size() < 2) {
                return null;
            }
            baseline = thisMonth.getFirst().getWeight();
        }

        return latest.getWeight().subtract(baseline).setScale(2, RoundingMode.HALF_UP);
    }

    private boolean isAbnormalChange(BigDecimal previous, BigDecimal current) {
        if (previous == null || current == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }
        BigDecimal rate = current.subtract(previous).abs()
                .divide(previous, 4, RoundingMode.HALF_UP);
        return rate.compareTo(ABNORMAL_CHANGE_RATE) >= 0;
    }


    private void syncPetCurrentWeight(Pet pet) {
        petWeightRepository.findFirstByPetOrderByRecordedAtDescPetWeightIdDesc(pet)
                .ifPresent(latest -> pet.update(
                        null, null, null, null, null, latest.getWeight(), null, null));
    }

    private LocalDate bucketKey(LocalDateTime recordedAt, WeightGraphPeriodEnum period) {
        LocalDate date = recordedAt.toLocalDate();
        return period.isMonthlyBucket() ? date.withDayOfMonth(1) : date;
    }

    private YearMonth resolveYearMonth(Integer year, Integer month) {
        if (year == null || month == null) {
            return YearMonth.now();
        }
        if (month < 1 || month > 12) {
            throw GeneralException.of(PetWeightErrorCode.PET_WEIGHT_INVALID_PERIOD);
        }
        return YearMonth.of(year, month);
    }

    private void validateRecordedAt(LocalDateTime recordedAt) {
        if (recordedAt != null && recordedAt.isAfter(LocalDateTime.now())) {
            throw GeneralException.of(PetWeightErrorCode.PET_WEIGHT_FUTURE_NOT_ALLOWED);
        }
    }

    private Pet loadOwnedPet(Long petId) {
        User user = securityUtils.currentUser();
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> GeneralException.of(PetErrorCode.PET_NOT_FOUND));
        if (!pet.getUser().getUid().equals(user.getUid())) {
            throw GeneralException.of(PetErrorCode.PET_NOT_FOUND);
        }
        return pet;
    }

    private PetWeightEntity loadRecord(Pet pet, Long petWeightId) {
        return petWeightRepository.findByPetWeightIdAndPet(petWeightId, pet)
                .orElseThrow(() -> GeneralException.of(PetWeightErrorCode.PET_WEIGHT_NOT_FOUND));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
