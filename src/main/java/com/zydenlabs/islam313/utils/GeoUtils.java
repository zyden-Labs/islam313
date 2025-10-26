package com.zydenlabs.islam313.utils;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

public class GeoUtils {

    private static final GeometryFactory geometryFactory =
            new GeometryFactory(new PrecisionModel(), 4326);

    public static Point createPoint(double lng, double lat) {
        // x = longitude, y = latitude
        Point point = geometryFactory.createPoint(new Coordinate(lng, lat));
        point.setSRID(4326);
        return point;
    }
}

