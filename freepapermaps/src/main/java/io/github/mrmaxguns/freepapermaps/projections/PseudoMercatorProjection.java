package io.github.mrmaxguns.freepapermaps.projections;

import java.lang.Math;

// Based on https://wiki.openstreetmap.org/wiki/Mercator
public class PseudoMercatorProjection extends Projection {
    public static final double RADIUS = 6378137.0;

    public static final double MIN_LON = -180;
    public static final double MAX_LAT = 85.05113;

    public static final double MAX_LON = 180;
    public static final double MIN_LAT = 85.05113;

    public Coordinate localOrigin;
    public Coordinate globalOrigin;
    public Coordinate globalOriginDiag;

    public PseudoMercatorProjection(double originLon, double originLat) {
        super(originLon, originLat);
        name = "Pseudo-Mercator Projection";
        localOrigin = projectRaw(origin);
        globalOrigin = projectRaw(Coordinate.createWGS84(MIN_LON, MAX_LAT));
        globalOriginDiag = projectRaw(Coordinate.createWGS84(MAX_LON, MIN_LAT));
    }

    public Coordinate projectRaw(Coordinate original) {
        double lon = original.getX();
        double lat = original.getY();

        double xRaw = Math.log(Math.tan(Math.PI / 4 + Math.toRadians(lon) / 2)) * RADIUS;
        double yRaw = Math.toRadians(lat) * RADIUS;

        return Coordinate.createRaw(xRaw, yRaw);
    }

    public Coordinate project(Coordinate original) {
        Coordinate projected = projectRaw(original);
        double xRaw = projected.getX();
        double yRaw = projected.getY();

        if (xRaw < localOrigin.getX()) {
            xRaw = localOrigin.getX() + (xRaw - globalOrigin.getX());
        }

        if (yRaw > localOrigin.getY()) {
            yRaw = localOrigin.getY() - (globalOrigin.getY() - yRaw);
        }

        return Coordinate.createInternal(xRaw - localOrigin.getX(), Math.abs(yRaw - localOrigin.getY()));
    }
}

