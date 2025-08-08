package io.github.mrmaxguns.freepapermaps.projections;

import io.github.mrmaxguns.freepapermaps.geometry.BoundingBox;


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
    /** Most negative longitude value. Not exactly 90 degrees for the same reason as <code>MAX_LAT</code>. */
    public static final double MIN_LAT = -85.05113;

    /** The origin value that was passed into this projection, converted into x/y units by the Mercator formula. */
    private final RawCoordinate localOrigin;

    /** Bounding box of the world map, as outputted by the Mercator projection. */
    private final BoundingBox<RawCoordinate> globalBoundingBox;

    /** Constructs a Pseudo-Mercator Projection given a point of origin (WGS84). */
    public PseudoMercatorProjection(WGS84Coordinate origin) {
        super(origin);
        checkBounds(origin);

        localOrigin = projectRaw(this.origin);
        globalBoundingBox = new BoundingBox<>(
                projectRaw(new WGS84Coordinate(MIN_LON, MAX_LAT)),
                projectRaw(new WGS84Coordinate(MAX_LON, MIN_LAT)));
    }

    /**
     * Converts a WGS84 coordinate into an x/y coordinate in meters using the Mercator projection. The value returned
     * from this function is "raw" because while it is a value in meters, it follows the x/y coordinate system rather
     * than our onscreen coordinate system where the chosen point of origin is (0, 0).
     * @param original the original WGS84 coordinate.
     * @return the output of applying the mercator projection, in meters (a Raw coordinate)
     */
    public RawCoordinate projectRaw(WGS84Coordinate original) {
        checkBounds(original);

        double lon = original.getX();
        double lat = original.getY();

        double xRaw = Math.toRadians(lon) * RADIUS;
        double yRaw = Math.log(Math.tan(Math.PI / 4 + Math.toRadians(lat) / 2)) * RADIUS;

        return new RawCoordinate(xRaw, yRaw);
    }

    /**
     * Projects a WGS84 Coordinate to our Projected Coordinate system with the help of the Mercator projection.
     *
     * @param original the original WGS84 coordinate
     * @return an InternalMeters coordinate after applying a mercator projection to original
     */
    public ProjectedCoordinate project(WGS84Coordinate original) {
        RawCoordinate projected = projectRaw(original);
        double xRaw = projected.getX();
        double yRaw = projected.getY();

        // Account for a point to the left of our origin point, by wrapping it around to the right.
        if (xRaw < localOrigin.getX()) {
            xRaw = xRaw + globalBoundingBox.getWidth();
        }

        // Y-wraparound is not supported because it causes issues, especially due to our cutoff at 85 degrees.
        if (yRaw > localOrigin.getY()) {
            throw new IllegalArgumentException("Wrapping around across latitude is not supported.");
        }

        // Shift the coordinate so that it is correct relative to our origin point, which is (0, 0) in our new coordinate
        // system. We need to take the absolute value of y because despite the fact that our origin is technically
        // at the top, we count up going down as is standard in the computer graphics world.
        return new ProjectedCoordinate(xRaw - localOrigin.getX(), Math.abs(yRaw - localOrigin.getY()));
    }

    public String getName() {
        return "Pseudo-Mercator Projection";
    }

    /** Throws an exception if a point is not an appropriate input for this projection. */
    private void checkBounds(WGS84Coordinate c) {
        if (c.getLon() < MIN_LON || c.getLon() > MAX_LON || c.getLat() < MIN_LAT || c.getLat() > MAX_LAT) {
            throw new IllegalArgumentException(
                    "Coordinate " + c + " is outside the bounds of what can be projected by the Mercator.");
        }
    }
}
