package io.github.mrmaxguns.freepapermaps.projections;

public class DummyProjection extends Projection {
    public DummyProjection() {
        super(new WGS84Coordinate(0, 0));
    }

    public String getName() {
        return "Dummy Projection";
    }

    @Override
    public ProjectedCoordinate project(WGS84Coordinate original) {
        return new ProjectedCoordinate(original.getX(), origin.getY());
    }
}
