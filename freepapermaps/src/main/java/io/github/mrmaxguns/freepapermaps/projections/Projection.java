package io.github.mrmaxguns.freepapermaps.projections;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Projection represents a construct that allows converting between WGS84 coordinates and Projected coordinates.
 * Examples of projections include Mercator and Lambert Conformal Conic.
 */
public abstract class Projection {
    /** The WGS84 Coordinate which will be taken to be the top left corner of the map. */
    protected final WGS84Coordinate origin;

    /** Constructs a Projection given a point of origin (WGS84). */
    public Projection(WGS84Coordinate origin) {
        this.origin = origin;
    }

    /** Gets the projection's human-readable name. */
    public abstract String getName();

    public WGS84Coordinate getOrigin() {
        return origin;
    }

    /** Converts a WGS84 Coordinate to a Projected Coordinate based on the current projection. */
    public abstract ProjectedCoordinate project(WGS84Coordinate original);

    /** Projects a WGS84 Bounding Box to a Projected Bounding Box. */
    public BoundingBox<ProjectedCoordinate> project(BoundingBox<WGS84Coordinate> original) {
        return new BoundingBox<>(project(original.getTopLeftCorner()), project(original.getBottomRightCorner()));
    }

    /** Projects a list of WGS84 Coordinates into Projected Coordinates. */
    public List<ProjectedCoordinate> project(List<WGS84Coordinate> original) {
        return original.stream().map(this::project).collect(Collectors.toList());
    }
}
