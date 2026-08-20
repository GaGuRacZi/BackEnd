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
import com.gaguraczi.paw.domain.weights.entity.PetWeightPhoto;
import com.gaguraczi.paw.domain.weights.enums.WeightGraphPeriodEnum;
import com.gaguraczi.paw.domain.weights.exception.code.PetWeightErrorCode;
import com.gaguraczi.paw.domain.weights.repository.PetWeightRepository;
import com.gaguraczi.paw.domain.weights.support.PetWeightImageValidator;
import com.gaguraczi.paw.global.exception.GeneralException;
import com.gaguraczi.paw.global.security.SecurityUtils;
import com.gaguraczi.paw.utils.S3.S3Dto;
import com.gaguraczi.paw.utils.S3.S3Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PetWeightService {


    private final PetWeightRepository petWeightRepository;
    private final PetRepository petRepository;
    private final SecurityUtils securityUtils;
    private final S3Utils s3Utils;



    @Transactional
    public PetWeightRes create(Long petId, PetWeightCreateReq req, List<MultipartFile> images) {
        Pet pet = loadOwnedPet(petId);
        validateRecordedAt(req.recordedAt());

        List<MultipartFile> files = normalizeFiles(images);
        validatePhotoCount(files.size());
        List<S3Dto> uploaded = uploadAll(files);
        scheduleUploadedCleanupOnRollback(uploaded);

        try {
            PetWeightEntity petWeight = PetWeightEntity.builder()
                    .pet(pet)
                    .weight(req.weight())
                    .bodyType(req.bodyType())
                    .appetiteType(req.appetiteType())
                    .memoContent(blankToNull(req.memoContent()))
                    .recordedAt(req.recordedAt())
                    .build();

            petWeight.replacePhotos(toPhotos(uploaded));
            petWeightRepository.save(petWeight);
            syncPetCurrentWeight(pet);

            return PetWeightRes.from(petWeight);
        } catch (RuntimeException e) {
            uploaded.forEach(u -> s3Utils.deleteQuietly(u.getKey()));
            throw e;
        }
    }

    @Transactional
    public PetWeightRes update(Long petId, Long petWeightId, PetWeightUpdateReq req, List<MultipartFile> images) {
        Pet pet = loadOwnedPet(petId);
        PetWeightEntity petWeight = loadRecord(pet, petWeightId);

        List<String> keepUrls = req != null ? req.keepPhotoUrls() : null;
        List<MultipartFile> files = normalizeFiles(images);
        int keptCount = keepUrls != null ? keepUrls.size() : petWeight.getPhotos().size();
        validatePhotoCount(keptCount + files.size());

        List<PetWeightPhoto> removed = new ArrayList<>();
        if (keepUrls != null) {
            Set<String> keepSet = new HashSet<>(keepUrls);
            for (PetWeightPhoto photo : petWeight.getPhotos()) {
                if (!keepSet.contains(photo.getPhotoS3Url())) {
                    removed.add(photo);
                }
            }
        }

        List<S3Dto> uploaded = uploadAll(files);
        scheduleUploadedCleanupOnRollback(uploaded);

        try {
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

            petWeight.syncPhotos(keepUrls, toPhotos(uploaded));

            List<String> removedKeys = removed.stream()
                    .map(PetWeightPhoto::getPhotoKey)
                    .filter(Objects::nonNull)
                    .toList();
            afterCommit(() -> removedKeys.forEach(s3Utils::deleteQuietly));

            syncPetCurrentWeight(pet);
            return PetWeightRes.from(petWeight);
        } catch (RuntimeException e) {
            uploaded.forEach(u -> s3Utils.deleteQuietly(u.getKey()));
            throw e;
        }
    }

    @Transactional
    public void delete(Long petId, Long petWeightId) {
        Pet pet = loadOwnedPet(petId);
        PetWeightEntity petWeight = loadRecord(pet, petWeightId);

        List<String> keys = petWeight.getPhotos().stream()
                .map(PetWeightPhoto::getPhotoKey)
                .filter(Objects::nonNull)
                .toList();

        petWeightRepository.delete(petWeight);
        petWeightRepository.flush();
        afterCommit(() -> keys.forEach(s3Utils::deleteQuietly));
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

        PetWeightEntity latest = petWeightRepository
                .findFirstByPetOrderByRecordedAtDescPetWeightIdDesc(pet)
                .orElse(null);

        if (latest == null) {
            return PetWeightSummaryRes.of(petId, pet.getPetWeight(), null, null);
        }

        return PetWeightSummaryRes.of(
                petId,
                latest.getWeight(),
                latest.getRecordedAt(),
                calculateMonthChange(pet, latest)
        );
    }



    private BigDecimal calculateMonthChange(Pet pet, PetWeightEntity latest) {
        LocalDateTime monthStart = YearMonth.now().atDay(1).atStartOfDay();
        if (latest.getRecordedAt().isBefore(monthStart)) {
            return null;
        }

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

    private void afterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
            return;
        }
        action.run();
    }

    private void scheduleUploadedCleanupOnRollback(List<S3Dto> uploaded) {
        if (uploaded == null || uploaded.isEmpty()) {
            return;
        }
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        List<String> keys = uploaded.stream()
                .map(S3Dto::getKey)
                .filter(Objects::nonNull)
                .toList();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    keys.forEach(s3Utils::deleteQuietly);
                }
            }
        });
    }

    private List<MultipartFile> normalizeFiles(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }
        List<MultipartFile> files = new ArrayList<>();
        for (MultipartFile image : images) {
            if (image != null && !image.isEmpty()) {
                PetWeightImageValidator.validate(image);
                files.add(image);
            }
        }
        return files;
    }

    private void validatePhotoCount(int count) {
        if (count > PetWeightImageValidator.MAX_PHOTOS) {
            throw GeneralException.of(PetWeightErrorCode.PET_WEIGHT_PHOTO_LIMIT_400);
        }
    }

    private List<S3Dto> uploadAll(List<MultipartFile> files) {
        List<S3Dto> uploaded = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                uploaded.add(s3Utils.uploadMultipartUnderDirectory(file, "pet-weight"));
            }
            return uploaded;
        } catch (RuntimeException e) {
            uploaded.forEach(u -> s3Utils.deleteQuietly(u.getKey()));
            throw e;
        }
    }

    private List<PetWeightPhoto> toPhotos(List<S3Dto> uploaded) {
        List<PetWeightPhoto> photos = new ArrayList<>();
        for (S3Dto dto : uploaded) {
            photos.add(PetWeightPhoto.builder()
                    .photoS3Url(dto.getUrl())
                    .photoKey(dto.getKey())
                    .sortOrder(0)
                    .build());
        }
        return photos;
    }
}