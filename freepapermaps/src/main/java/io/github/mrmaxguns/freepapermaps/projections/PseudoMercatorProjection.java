package io.github.mrmaxguns.freepapermaps.projections;

import java.lang.Math;

/**
 * The PseudoMercatorProjection is a Mercator projection that treats the Earth as a sphere. Unlike the Web Mercator,
 * this projection does not account for zoom level. This projection has significant distortions as we move away from
 * the equator.
 * <p>
 * Based on <a href="https://wiki.openstreetmap.org/wiki/Mercator">the OpenStreetMap wiki</a>
 */
public class PseudoMercatorProjection extends Projection {
    /** Earth's radius at the equator. */
    public static final double RADIUS = 6378137.0;

    /** Most negative longitude value. */
    public static final double MIN_LON = -180;
    /**
     * Most positive longitude value. This is not exactly 90 degrees because if we used 90, our formulas would return
     * infinity.
     */
    public static final double MAX_LAT = 85.05113;

    /** Most positive longitude value. */
    public static final double MAX_LON = 180;
    /** Most negative longitude value. Not exactly 90 degrees for the same reason as before. */
    public static final double MIN_LAT = 85.05113;

    /** The origin value that was passed into this projection, converted into x/y units by the Mercator formula. */
    private final Coordinate localOrigin;
    /** The "global origin" converted into x/y units by the Mercator formula. This is the top-left corner of a world map. */
    private final Coordinate globalOrigin;
    /**
     * The diagonal to the "global origin" converted into x/y units by the Mercator formula. This is the bottom-right corner
     * of the world map.
     */
    private final Coordinate globalOriginDiag;

    /** Constructs a Pseudo-Mercator Projection given a point of origin (WGS84). */
    public PseudoMercatorProjection(Coordinate origin) {
        super(origin);

        localOrigin = projectRaw(this.origin);
        globalOrigin = projectRaw(Coordinate.newWGS84Coordinate(MIN_LON, MAX_LAT));
        globalOriginDiag = projectRaw(Coordinate.newWGS84Coordinate(MAX_LON, MIN_LAT));
    }

    /**
     * Converts a WGS84 coordinate into an x/y coordinate in meters using the Mercator projection. The value returned
     * from this function is "raw" because while it is a value in meters, it follows the x/y coordinate system rather
     * than our onscreen coordinate system where the chosen point of origin is (0, 0).
     * @param original the original WGS84 coordinate.
     * @return the output of applying the mercator projection, in meters (a Raw coordinate)
     */
    public Coordinate projectRaw(Coordinate original) {
        if (original.getCategory() != Coordinate.Category.WGS84) {
            throw new IllegalArgumentException("Parameter 'original' must be a WGS84 coordinate.");
        }

        double lon = original.getX();
        double lat = original.getY();

        double xRaw = Math.log(Math.tan(Math.PI / 4 + Math.toRadians(lon) / 2)) * RADIUS;
        double yRaw = Math.toRadians(lat) * RADIUS;

        return Coordinate.newRawCoordinate(xRaw, yRaw);
    }

    /**
     * Projects a WGS84 coordinate to our InternalMeters coordinate system with the help of the Mercator projection.
     * @param original the original WGS84 coordinate
     * @return an InternalMeters coordinate after applying a mercator projection to original
     */
    public Coordinate project(Coordinate original) {
        Coordinate projected = projectRaw(original);
        double xRaw = projected.getX();
        double yRaw = projected.getY();

        // Account for a point to the left of our origin point, by wrapping it around to the right.
        if (xRaw < localOrigin.getX()) {
            xRaw = localOrigin.getX() + (xRaw - globalOrigin.getX());
        }

        // Account for a point above our origin point, by wrapping it around to be below.
        if (yRaw > localOrigin.getY()) {
            yRaw = localOrigin.getY() - (globalOrigin.getY() - yRaw);
        }

        // Shift the coordinate so that it is correct relative to our origin point, which is (0, 0) in our new coordinate
        // system. We need to take the absolute value of y because despite the fact that our origin is technically
        // at the top, we count up going down as is standard in the computer graphics world.
        return Coordinate.newInternalCoordinate(xRaw - localOrigin.getX(), Math.abs(yRaw - localOrigin.getY()));
    }

    public String getName() {
        return "Pseudo-Mercator Projection";
    }
}
