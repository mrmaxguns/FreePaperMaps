package io.github.mrmaxguns.freepapermaps.projections;

import io.github.mrmaxguns.freepapermaps.geometry.Coordinate;


/**
 * Represents a coordinate that requires further processing. Since the meaning of this coordinate is ambiguous on
 * purpose, it should not be used in public interfaces.
 */
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
