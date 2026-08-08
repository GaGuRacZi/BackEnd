package com.gaguraczi.paw.domain.location.dto.res;

import com.gaguraczi.paw.domain.region.entity.LegalRegion;
import com.gaguraczi.paw.domain.users.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserLocationRes {

    private final String regionCode;
    private final String regionName;
    private final String address;
    private final Double latitude;
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
