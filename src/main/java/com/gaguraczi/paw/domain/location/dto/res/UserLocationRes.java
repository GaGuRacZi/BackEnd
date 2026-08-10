package com.gaguraczi.paw.domain.location.dto.res;

import com.gaguraczi.paw.domain.region.entity.LegalRegion;
import com.gaguraczi.paw.domain.users.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "유저 위치 응답")
public class UserLocationRes {

    @Schema(description = "시군구 LegalRegion 코드", example = "1111000000")
    private final String regionCode;

    @Schema(description = "시군구 이름", example = "서울특별시 종로구")
    private final String regionName;

    @Schema(description = "표시용 주소", example = "서울특별시 종로구 세종대로 110")
    private final String address;

    @Schema(description = "위도", example = "37.5665")
    private final Double latitude;

    @Schema(description = "경도", example = "126.9780")
    private final Double longitude;

    public static UserLocationRes of(LegalRegion region, String address, Double latitude, Double longitude) {
        return UserLocationRes.builder()
                .regionCode(region.getCode())
                .regionName(region.getName())
                .address(address)
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }

    public static UserLocationRes fromUser(User user, String address) {
        Double lat = null;
        Double lng = null;
        if (user.getUserPoint() != null) {
            lat = user.getUserPoint().getY();
            lng = user.getUserPoint().getX();
        }
        return UserLocationRes.builder()
                .regionCode(user.getRegion() != null ? user.getRegion().getCode() : null)
                .regionName(user.getRegion() != null ? user.getRegion().getName() : null)
                .address(address)
                .latitude(lat)
                .longitude(lng)
                .build();
    }
}
