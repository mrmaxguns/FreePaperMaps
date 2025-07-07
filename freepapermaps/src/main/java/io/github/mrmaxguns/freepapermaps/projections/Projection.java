package io.github.mrmaxguns.freepapermaps.projections;

/**
 * Projection represents a construct that allows converting between WGS84 coordinates and InternalMeters coordinates.
 * Examples of projections include Mercator and Lambert Conformal Conic.
 */
public abstract class Projection {
    /** The WGS84 coordinate which will be taken to be the top left corner of the map. */
    protected final Coordinate origin;

    /** Constructs a Projection given a point of origin (WGS84). */
    public Projection(Coordinate origin) {
        if (origin.getCategory() != Coordinate.Category.WGS84) {
            throw new IllegalArgumentException("The origin of a projection must be specified as a WGS84 coordinate.");
        }
        this.origin = origin;
    }

    /** Gets the projection's human-readable name. */
    public abstract String getName();

    /** Returns the projection's origin. */
    public Coordinate getOrigin() {
        return origin;
    }

    /** Converts a WGS84 coordinate to an InternalMeters coordinate based on the current projection. */
    public abstract Coordinate project(Coordinate original);
}
