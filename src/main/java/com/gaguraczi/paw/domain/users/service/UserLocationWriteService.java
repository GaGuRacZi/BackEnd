package com.gaguraczi.paw.domain.users.service;

import com.gaguraczi.paw.domain.region.entity.LegalRegion;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.domain.users.repository.UserRepository;
import com.gaguraczi.paw.global.exception.GeneralException;
import com.gaguraczi.paw.domain.location.exception.code.LocationErrorCode;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserLocationWriteService {

    private final UserRepository userRepository;

    @Transactional
    public User updateLocation(UUID userId, Point point, LegalRegion region, String locationAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> GeneralException.of(LocationErrorCode.LOCATION_USER_NOT_SET));
        user.updateLocation(point, region, locationAddress);
        return user;
    }
}
