package uz.navbatuz.backend.common.geo;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

public final class Geo {
    private static final GeometryFactory GF = new GeometryFactory();
    private Geo() {}
    /** JTS Point: X=lon, Y=lat */
    public static Point point(double lat, double lon) {
        return GF.createPoint(new Coordinate(lon, lat));
    }
}