package com.gaguraczi.paw.domain.walkcourse.service;

import com.gaguraczi.paw.domain.users.entity.Pet;
import com.gaguraczi.paw.domain.users.repository.PetRepository;
import com.gaguraczi.paw.domain.walkcourse.converter.WalkCourseConverter;
import com.gaguraczi.paw.domain.walkcourse.dto.request.WalkCourseCreateRequest;
import com.gaguraczi.paw.domain.walkcourse.dto.request.WalkCourseUpdateRequest;
import com.gaguraczi.paw.domain.walkcourse.dto.response.WalkCourseIdResponse;
import com.gaguraczi.paw.domain.walkcourse.dto.response.WalkCourseResponse;
import com.gaguraczi.paw.domain.walkcourse.dto.response.WalkCourseSummaryResponse;
import com.gaguraczi.paw.domain.walkcourse.entity.WalkCourseEntity;
import com.gaguraczi.paw.domain.walkcourse.exception.WalkCourseErrorCode;
import com.gaguraczi.paw.domain.walkcourse.repository.WalkCourseRepository;
import com.gaguraczi.paw.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalkCourseService {


    private static final int DEFAULT_FREQUENT_SIZE = 3;

    private final WalkCourseRepository walkCourseRepository;
    private final PetRepository petRepository;


    @Transactional
    public WalkCourseResponse createCourse(WalkCourseCreateRequest request) {
        Pet pet = petRepository.findById(request.getPetId())
                .orElseThrow(() -> new GeneralException(WalkCourseErrorCode.COURSE_PET_NOT_FOUND));

        if (walkCourseRepository.existsByPet_PetIdAndName(request.getPetId(), request.getName())) {
            throw new GeneralException(WalkCourseErrorCode.COURSE_NAME_DUPLICATED);
        }

        WalkCourseEntity course = WalkCourseConverter.toWalkCourse(request, pet);
        WalkCourseEntity saved = walkCourseRepository.save(course);

        return WalkCourseConverter.toWalkCourseResponse(saved);
    }


    public WalkCourseResponse getCourse(Long courseId) {
        return WalkCourseConverter.toWalkCourseResponse(getCourseOrThrow(courseId));
    }


    public List<WalkCourseSummaryResponse> getCourses(Long petId) {
        List<WalkCourseEntity>courses = walkCourseRepository.findAllByPet_PetIdOrderByLastUsedAtDescIdDesc(petId);
        return WalkCourseConverter.toWalkCourseSummaryResponseList(courses);
    }


    public List<WalkCourseSummaryResponse> getFrequentCourses(Long petId, Integer size) {
        int limit = (size != null && size > 0) ? size : DEFAULT_FREQUENT_SIZE;

        List<WalkCourseEntity> courses = walkCourseRepository
                .findAllByPet_PetIdOrderByUseCountDescLastUsedAtDesc(petId, Limit.of(limit));

        return WalkCourseConverter.toWalkCourseSummaryResponseList(courses);
    }


    @Transactional
    public WalkCourseResponse updateCourse(Long courseId, WalkCourseUpdateRequest request) {
        WalkCourseEntity course = getCourseOrThrow(courseId);

        if (request.getName() != null
                && !request.getName().equals(course.getName())
                && walkCourseRepository.existsByPet_PetIdAndName(course.getPet().getPetId(), request.getName())) {
            throw new GeneralException(WalkCourseErrorCode.COURSE_NAME_DUPLICATED);
        }

        course.update(
                request.getName(),
                request.getDistance(),
                request.getThumbnailUrl(),
                WalkCourseConverter.toPathJson(request.getPath())
        );

        return WalkCourseConverter.toWalkCourseResponse(course);
    }


    @Transactional
    public WalkCourseIdResponse deleteCourse(Long courseId) {
        WalkCourseEntity course = getCourseOrThrow(courseId);
        walkCourseRepository.delete(course);
        return WalkCourseConverter.toWalkCourseIdResponse(courseId);
    }

    private WalkCourseEntity getCourseOrThrow(Long courseId) {
        return walkCourseRepository.findById(courseId)
                .orElseThrow(() -> new GeneralException(WalkCourseErrorCode.COURSE_NOT_FOUND));
    }
}
