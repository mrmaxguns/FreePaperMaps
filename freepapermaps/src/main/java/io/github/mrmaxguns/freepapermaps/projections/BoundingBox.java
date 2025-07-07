package io.github.mrmaxguns.freepapermaps.projections;

public class BoundingBox<C extends Coordinate<C>> {
    private final C topLeftCorner;
    private final C bottomRightCorner;

    private final double width;
    private final double height;

    public BoundingBox(C topLeftCorner, C bottomRightCorner) {
        this.topLeftCorner = topLeftCorner;
        this.bottomRightCorner = bottomRightCorner;
        this.width = this.getMaxX() - this.getMinX();
        this.height = this.getMaxY() - this.getMinY();
    }

    public C getTopLeftCorner() {
        return topLeftCorner;
    }

    public C getBottomRightCorner() {
        return bottomRightCorner;
    }

    public double getMinX() { return topLeftCorner.getX(); }
    public double getMinLon() { return topLeftCorner.getLon(); }
    public double getMaxX() { return bottomRightCorner.getX(); }
    public double getMaxLon() { return bottomRightCorner.getLon(); }
    public double getMinY() { return bottomRightCorner.getY(); }
    public double getMinLat() { return bottomRightCorner.getLat(); }
    public double getMaxY() { return topLeftCorner.getY(); }
    public double getMaxLat() { return topLeftCorner.getLat(); }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public boolean equals(BoundingBox<C> other) {
        return topLeftCorner.equals(other.getTopLeftCorner()) && bottomRightCorner.equals(other.getBottomRightCorner());
    }
}
