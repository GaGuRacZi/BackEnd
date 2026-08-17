package com.gaguraczi.paw.domain.walkcourse.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaguraczi.paw.domain.users.entity.Pet;
import com.gaguraczi.paw.domain.walkcourse.dto.Coordinate;
import com.gaguraczi.paw.domain.walkcourse.dto.request.WalkCourseCreateRequest;
import com.gaguraczi.paw.domain.walkcourse.dto.response.WalkCourseIdResponse;
import com.gaguraczi.paw.domain.walkcourse.dto.response.WalkCourseResponse;
import com.gaguraczi.paw.domain.walkcourse.dto.response.WalkCourseSummaryResponse;
import com.gaguraczi.paw.domain.walkcourse.entity.WalkCourseEntity;
import com.gaguraczi.paw.domain.walkcourse.exception.WalkCourseErrorCode;
import com.gaguraczi.paw.global.exception.GeneralException;

import java.util.Collections;
import java.util.List;


public class WalkCourseConverter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private WalkCourseConverter() {
    }


    public static String toPathJson(List<Coordinate> path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(path);
        } catch (Exception e) {
            throw new GeneralException(WalkCourseErrorCode.COURSE_PATH_INVALID, e);
        }
    }


    public static List<Coordinate> toPath(String pathJson) {
        if (pathJson == null || pathJson.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return OBJECT_MAPPER.readValue(pathJson, new TypeReference<List<Coordinate>>() {
            });
        } catch (Exception e) {
            throw new GeneralException(WalkCourseErrorCode.COURSE_PATH_INVALID, e);
        }
    }



    public static WalkCourseEntity toWalkCourse(WalkCourseCreateRequest request, Pet pet) {
        return WalkCourseEntity.builder()
                .pet(pet)
                .name(request.getName())
                .distance(request.getDistance())
                .thumbnailUrl(request.getThumbnailUrl())
                .pathJson(toPathJson(request.getPath()))
                .build();
    }


    public static WalkCourseResponse toWalkCourseResponse(WalkCourseEntity course) {
        return WalkCourseResponse.builder()
                .courseId(course.getId())
                .petId(course.getPet().getPetId())
                .name(course.getName())
                .distance(course.getDistance())
                .thumbnailUrl(course.getThumbnailUrl())
                .path(toPath(course.getPathJson()))
                .useCount(course.getUseCount())
                .lastUsedAt(course.getLastUsedAt())
                .build();
    }

    public static WalkCourseSummaryResponse toWalkCourseSummaryResponse(WalkCourseEntity course) {
        return WalkCourseSummaryResponse.builder()
                .courseId(course.getId())
                .name(course.getName())
                .distance(course.getDistance())
                .thumbnailUrl(course.getThumbnailUrl())
                .useCount(course.getUseCount())
                .build();
    }

    public static List<WalkCourseSummaryResponse> toWalkCourseSummaryResponseList(List<WalkCourseEntity> courses) {
        return courses.stream()
                .map(WalkCourseConverter::toWalkCourseSummaryResponse)
                .toList();
    }

    public static WalkCourseIdResponse toWalkCourseIdResponse(Long courseId) {
        return WalkCourseIdResponse.builder()
                .courseId(courseId)
                .build();
    }
}
