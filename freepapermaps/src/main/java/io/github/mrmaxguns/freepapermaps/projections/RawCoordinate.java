package io.github.mrmaxguns.freepapermaps.projections;

public class RawCoordinate extends Coordinate<RawCoordinate> {
    public RawCoordinate(double xOrLon, double yOrLat) {
        super(xOrLon, yOrLat);
    }

    protected RawCoordinate createInstance(double xOrLon, double yOrLat) {
        return new RawCoordinate(xOrLon, yOrLat);
    }

    public String getCoordinateType() {
        return "Raw";
    }

    public String getCoordinateUnit() {
        return "?";
    }
}
