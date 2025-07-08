package io.github.mrmaxguns.freepapermaps.rendering;

import io.github.mrmaxguns.freepapermaps.projections.BoundingBox;
import io.github.mrmaxguns.freepapermaps.projections.ProjectedCoordinate;

public class Scaler {
    private final double mapScale;
    private final double scaleFactor;

    public static final int MILLIMETERS_IN_METER = 1000;

    public Scaler(double mapScale) {
        if (mapScale <= 0 || Double.isNaN(mapScale)) {
            throw new IllegalArgumentException("Map scale must be a positive, non-zero value.");
        }

        this.mapScale = mapScale;
        scaleFactor = (1.0 / mapScale) * MILLIMETERS_IN_METER;
    }

    public static Scaler newScalerFromDistance(double actualDistanceInMeters, double requiredDistanceInMillimeters) {
        double actualDistanceInMillimeters = actualDistanceInMeters * MILLIMETERS_IN_METER;
        return new Scaler(actualDistanceInMillimeters / requiredDistanceInMillimeters);
    }

    public static Scaler newScalerFromWidth(BoundingBox<ProjectedCoordinate> bounds, double widthInMillimeters) {
        return newScalerFromDistance(bounds.getWidth(), widthInMillimeters);
    }

    public static Scaler newScalerFromHeight(BoundingBox<ProjectedCoordinate> bounds, double heightInMillimeters) {
        return newScalerFromDistance(bounds.getHeight(), heightInMillimeters);
    }

    public ScaledCoordinate scale(ProjectedCoordinate c) {
        return new ScaledCoordinate(c.getX() * scaleFactor, c.getY() * scaleFactor);
    }

    public BoundingBox<ScaledCoordinate> scale(BoundingBox<ProjectedCoordinate> b) {
        return new BoundingBox<>(scale(b.getTopLeftCorner()), scale(b.getBottomRightCorner()));
    }

    public double getMapScale() {
        return mapScale;
    }

    public String toString() {
        return "Scaler(1:" + (int)mapScale + ")";
    }
}
