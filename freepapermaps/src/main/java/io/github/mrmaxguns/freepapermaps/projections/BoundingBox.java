package io.github.mrmaxguns.freepapermaps.projections;

/**
 * BoundingBox represents a box in space defined as the rectangular region between a top left point and a bottom right
 * point. BoundingBoxes are immutable, just like Coordinates.
 */
public class BoundingBox<C extends Coordinate<C>> {
    /** The top left corner (minimum x/lon, maximum y/lat). */
    private final C topLeftCorner;
    /** The bottom right corner (maximum x/lon, minimum y/lat). */
    private final C bottomRightCorner;

    /** The x-distance between the two corners. */
    private final double width;
    /** The y-distance between the two corners. */
    private final double height;

    /** Constructs a bounding box given a top left and bottom right corner, defined by coordinates of the same type. */
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

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getMinX() { return topLeftCorner.getX(); }
    public double getMinLon() { return topLeftCorner.getLon(); }

    public double getMaxX() { return bottomRightCorner.getX(); }
    public double getMaxLon() { return bottomRightCorner.getLon(); }

    public double getMinY() { return bottomRightCorner.getY(); }
    public double getMinLat() { return bottomRightCorner.getLat(); }

    public double getMaxY() { return topLeftCorner.getY(); }
    public double getMaxLat() { return topLeftCorner.getLat(); }

    /** Returns true if the corners of both bounding boxes are equal, as defined by the Coordinate equals method. */
    public boolean equals(BoundingBox<C> other) {
        return topLeftCorner.equals(other.getTopLeftCorner()) && bottomRightCorner.equals(other.getBottomRightCorner());
    }
}
