package io.github.mrmaxguns.freepapermaps.projections;

// Naming convention: w: WGS84 coordinate, p: projected coordinate
public abstract class Projection {
    protected String name;
    protected Coordinate origin;

    public Projection(double originLon, double originLat) {
        origin = Coordinate.createWGS84(originLon, originLat);
    }

    public String get_name() {
        return name;
    }

    public abstract Coordinate project(Coordinate original);
}

