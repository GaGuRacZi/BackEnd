package com.gaguraczi.paw.domain.location.service;

import com.gaguraczi.paw.domain.location.dto.res.AddressRes;
import com.gaguraczi.paw.domain.location.dto.res.CoordinateResolveRes;
import com.gaguraczi.paw.domain.location.dto.res.LegalDistrictAddressRes;
import com.gaguraczi.paw.domain.location.dto.res.UserLocationRes;
import com.gaguraczi.paw.domain.location.exception.code.LocationErrorCode;
import com.gaguraczi.paw.domain.region.entity.LegalRegion;
import com.gaguraczi.paw.domain.region.service.LegalRegionService;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.global.exception.GeneralException;
import com.gaguraczi.paw.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationService {

    private final NaverMapService naverMapService;
    private final LegalRegionService legalRegionService;
    private final SecurityUtils securityUtils;

    public UserLocationRes getMyLocation() {
        User user = securityUtils.currentUser();
        if (user.getRegion() == null || user.getUserPoint() == null) {
            throw GeneralException.of(LocationErrorCode.LOCATION_USER_NOT_SET);
        }
        return UserLocationRes.fromUser(user, user.getRegion().getName());
    }

    @Transactional
    public UserLocationRes certifyMyLocation(double latitude, double longitude) {
        validateLatLng(latitude, longitude);
        User user = securityUtils.currentUser();

        LegalDistrictAddressRes resolved = naverMapService.resolveLegalDistrictCodeAndAddress(longitude, latitude);
        LegalRegion region = legalRegionService.requireActiveSigunguByLegalDistrictCode(resolved.legalDistrictCode());
        Point point = toPoint(longitude, latitude);

        user.updateLocation(point, region);
        return UserLocationRes.of(region, resolved.address(), latitude, longitude);
    }

    public AddressRes getRoadAddress(double latitude, double longitude) {
        validateLatLng(latitude, longitude);
        String address = naverMapService.resolveRoadAddress(longitude, latitude);
        return new AddressRes(address);
    }

    public CoordinateResolveRes resolve(double latitude, double longitude) {
        validateLatLng(latitude, longitude);
        LegalDistrictAddressRes resolved = naverMapService.resolveLegalDistrictCodeAndAddress(longitude, latitude);
        LegalRegion region = legalRegionService.requireActiveSigunguByLegalDistrictCode(resolved.legalDistrictCode());
        return CoordinateResolveRes.builder()
                .regionCode(region.getCode())
                .regionName(region.getName())
                .address(resolved.address())
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }

    private static Point toPoint(double longitude, double latitude) {
        GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
        Point point = factory.createPoint(new Coordinate(longitude, latitude));
        point.setSRID(4326);
        return point;
    }

    private static void validateLatLng(double latitude, double longitude) {
        if (Double.isNaN(latitude) || Double.isNaN(longitude)
                || Double.isInfinite(latitude) || Double.isInfinite(longitude)) {
            throw GeneralException.of(LocationErrorCode.LOCATION_INVALID_REQUEST);
        }
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            throw GeneralException.of(LocationErrorCode.LOCATION_INVALID_REQUEST);
        }
    }
}
