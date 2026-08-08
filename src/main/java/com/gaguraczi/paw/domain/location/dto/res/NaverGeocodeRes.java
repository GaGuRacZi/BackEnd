package com.gaguraczi.paw.domain.location.dto.res;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import java.util.List;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NaverGeocodeRes(
        String status,
        Meta meta,
        List<Address> addresses,
        String errorMessage
) {
    public Optional<Point> firstPoint() {
        if (addresses == null || addresses.isEmpty()) {
            return Optional.empty();
        }
        Address first = addresses.getFirst();
        try {
            double lng = Double.parseDouble(first.x());
            double lat = Double.parseDouble(first.y());
            GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
            Point point = factory.createPoint(new Coordinate(lng, lat));
            point.setSRID(4326);
            return Optional.of(point);
        } catch (NumberFormatException | NullPointerException e) {
            return Optional.empty();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Meta(int totalCount, int page, int count) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Address(
            String roadAddress,
            String jibunAddress,
            String englishAddress,
            String x,
            String y
    ) {
    }
}
