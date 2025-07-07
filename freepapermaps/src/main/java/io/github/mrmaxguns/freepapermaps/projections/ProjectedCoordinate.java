package io.github.mrmaxguns.freepapermaps.projections;

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
