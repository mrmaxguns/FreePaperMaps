package io.github.mrmaxguns.freepapermaps.rendering;

import io.github.mrmaxguns.freepapermaps.projections.Coordinate;

public class ScaledCoordinate extends Coordinate<ScaledCoordinate> {
    public ScaledCoordinate(double x, double y) {
        super(x, y);
    }

    protected ScaledCoordinate createInstance(double x, double y) {
        return new ScaledCoordinate(x, y);
    }

    public String getCoordinateType() {
        return "Scaled";
    }

    public String getCoordinateUnit() {
        return "mm";
    }
}
