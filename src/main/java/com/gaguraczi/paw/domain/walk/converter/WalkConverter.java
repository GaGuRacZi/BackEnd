package com.gaguraczi.paw.domain.walk.converter;

import com.gaguraczi.paw.domain.users.entity.Pet;
import com.gaguraczi.paw.domain.walk.dto.request.WalkCreateRequest;
import com.gaguraczi.paw.domain.walk.dto.request.WalkFinishRequest;
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
import com.gaguraczi.paw.domain.walk.redis.WalkInProgressSession;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;


public class WalkConverter {


    public static WalkEntity toWalk(WalkCreateRequest request, Pet pet) {
        return WalkEntity.builder()
                .pet(pet)
                .walkDate(request.getStartTime().toLocalDate())
                .weatherType(WeatherTypeEnum.from(request.getWeatherType()))
                .temp(request.getTemp())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .walkingAmount(request.getWalkingAmount())
                .walkType(WalkTypeEnum.from(request.getWalkType()))
                .isUrine(request.getIsUrine())
                .isStool(request.getIsStool())
                .significant(request.getSignificant())
                .walkStatus(WalkStatusEnum.COMPLETED)
                .build();
    }


    public static WalkInProgressSession toInProgressSession(Long petId, LocalDateTime startTime) {
        return WalkInProgressSession.builder()
                .petId(petId)
                .startTime(startTime)
                .walkDate(startTime.toLocalDate())
                .build();
    }


    public static WalkEntity toFinishedWalk(WalkInProgressSession session,
                                            Pet pet,
                                            WalkFinishRequest request,
                                            LocalDateTime endTime) {
        return WalkEntity.builder()
                .pet(pet)
                .walkDate(session.getWalkDate())
                .weatherType(WeatherTypeEnum.from(request.getWeatherType()))
                .temp(request.getTemp())
                .startTime(session.getStartTime())
                .endTime(endTime)
                .walkingAmount(request.getWalkingAmount())
                .walkType(WalkTypeEnum.from(request.getWalkType()))
                .isUrine(request.getIsUrine())
                .isStool(request.getIsStool())
                .significant(request.getSignificant())
                .walkStatus(WalkStatusEnum.COMPLETED)
                .build();
    }


    public static WalkResponse toWalkResponse(WalkEntity walk) {
        return WalkResponse.builder()
                .walkId(walk.getId())
                .petId(walk.getPet().getPetId())
                .walkDate(walk.getWalkDate())
                .weatherType(walk.getWeatherType())
                .temp(walk.getTemp())
                .startTime(walk.getStartTime())
                .endTime(walk.getEndTime())
                .durationMinutes(walk.getDurationMinutes())
                .walkingAmount(walk.getWalkingAmount())
                .walkType(walk.getWalkType())
                .isUrine(walk.getIsUrine())
                .isStool(walk.getIsStool())
                .significant(walk.getSignificant())
                .walkStatus(walk.getWalkStatus())
                .build();
    }

    public static WalkResponse toInProgressWalkResponse(WalkInProgressSession session) {
        return WalkResponse.builder()
                .walkId(null)
                .petId(session.getPetId())
                .walkDate(session.getWalkDate())
                .weatherType(null)
                .temp(null)
                .startTime(session.getStartTime())
                .endTime(null)
                .durationMinutes(null)
                .walkingAmount(BigDecimal.ZERO)
                .walkType(WalkTypeEnum.NORMAL)
                .isUrine(Boolean.FALSE)
                .isStool(Boolean.FALSE)
                .significant(null)
                .walkStatus(WalkStatusEnum.IN_PROGRESS)
                .build();
    }

    public static WalkSummaryResponse toWalkSummaryResponse(WalkEntity walk) {
        return WalkSummaryResponse.builder()
                .walkId(walk.getId())
                .walkDate(walk.getWalkDate())
                .startTime(walk.getStartTime())
                .endTime(walk.getEndTime())
                .durationMinutes(walk.getDurationMinutes())
                .walkingAmount(walk.getWalkingAmount())
                .walkType(walk.getWalkType())
                .walkStatus(walk.getWalkStatus())
                .build();
    }

    public static List<WalkSummaryResponse> toWalkSummaryResponseList(List<WalkEntity> walks) {
        return walks.stream()
                .map(WalkConverter::toWalkSummaryResponse)
                .toList();
    }

    public static WalkStartResponse toWalkStartResponse(WalkInProgressSession session) {
        return WalkStartResponse.builder()
                .petId(session.getPetId())
                .walkDate(session.getWalkDate())
                .startTime(session.getStartTime())
                .walkStatus(WalkStatusEnum.IN_PROGRESS)
                .build();
    }

    public static WalkIdResponse toWalkIdResponse(Long walkId) {
        return WalkIdResponse.builder()
                .walkId(walkId)
                .build();
    }


    public static WalkDailyStatResponse toWalkDailyStatResponse(LocalDate date,
                                                                long totalMinutes,
                                                                BigDecimal totalDistance,
                                                                int walkCount) {
        return WalkDailyStatResponse.builder()
                .walkDate(date)
                .dayOfWeek(toKoreanDayOfWeek(date))
                .totalMinutes(totalMinutes)
                .totalDistance(totalDistance)
                .walkCount(walkCount)
                .build();
    }

    public static WalkWeeklySummaryResponse toWalkWeeklySummaryResponse(LocalDate weekStartDate,
                                                                        LocalDate weekEndDate,
                                                                        long averageMinutes,
                                                                        long lastWeekAverageMinutes,
                                                                        int walkCount,
                                                                        long totalMinutes,
                                                                        BigDecimal totalDistance) {
        return WalkWeeklySummaryResponse.builder()
                .weekStartDate(weekStartDate)
                .weekEndDate(weekEndDate)
                .averageMinutes(averageMinutes)
                .lastWeekAverageMinutes(lastWeekAverageMinutes)
                .diffMinutes(averageMinutes - lastWeekAverageMinutes)
                .walkCount(walkCount)
                .totalMinutes(totalMinutes)
                .totalDistance(totalDistance)
                .build();
    }


    private static String toKoreanDayOfWeek(LocalDate date) {
        return date.getDayOfWeek().getDisplayName(TextStyle.NARROW, Locale.KOREAN);
    }
}
