package io.github.mrmaxguns.freepapermaps.projections;

/**
 * Projection represents a construct that allows converting between WGS84 coordinates and InternalMeters coordinates.
 * Examples of projections include Mercator and Lambert Conformal Conic.
 */
public abstract class Projection {
    /** The WGS84 coordinate which will be taken to be the top left corner of the map. */
    protected final WGS84Coordinate origin;

    /** Constructs a Projection given a point of origin (WGS84). */
    public Projection(WGS84Coordinate origin) {
        this.origin = origin;
    }

    /** Gets the projection's human-readable name. */
    public abstract String getName();

    /** Returns the projection's origin. */
    public WGS84Coordinate getOrigin() {
        return origin;
    }

    /**
     * Converts a WGS84 coordinate to an InternalMeters coordinate based on the current projection.
     */
    public abstract ProjectedCoordinate project(WGS84Coordinate original);
}
