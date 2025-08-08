package io.github.mrmaxguns.freepapermaps.projections;

import io.github.mrmaxguns.freepapermaps.geometry.Coordinate;


/**
 * ProjectedCoordinate represents an internally used coordinate system similar to the one used in computer graphics:
 * <ul>
 *     <li>The origin is located at (0, 0) and is in the top left corner of the map</li>
 *     <li>
 *         Point values are x and y distances in meters. Distances are only positive and represent distance to
 *         the right (x) or down (y)
 *     </li>
 *     <li>A ProjectedCoordinate is typically the result of applying a Projection to a WGS84 Coordinate</li>
 * </ul>
 */
public class ProjectedCoordinate extends Coordinate<ProjectedCoordinate> {
    public ProjectedCoordinate(double x, double y) {
        super(x, y);
    }

    protected ProjectedCoordinate createInstance(double x, double y) {
        return new ProjectedCoordinate(x, y);
    }

    public String getCoordinateType() {
        return "Projected";
    }

    public String getCoordinateUnit() {
        return "m";
    }
}
