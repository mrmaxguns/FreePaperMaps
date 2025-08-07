package io.github.mrmaxguns.freepapermaps.geometry;


/**
 * BoundingBox represents a box in space defined as the rectangular region between a top left point and a bottom right
 * point. BoundingBoxes are immutable, just like Coordinates.
 */
public class BoundingBox<C extends BaseCoordinate<C>> {
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

    public double getMinX()   { return Math.min(topLeftCorner.getX(), bottomRightCorner.getX()); }

    public double getMinLon() { return getMinX(); }

    public double getMaxX()   { return Math.max(topLeftCorner.getX(), bottomRightCorner.getX()); }

    public double getMaxLon() { return getMaxX(); }

    public double getMinY()   { return Math.min(topLeftCorner.getY(), bottomRightCorner.getY()); }

    public double getMinLat() { return getMinY(); }

    public double getMaxY()   { return Math.max(topLeftCorner.getY(), bottomRightCorner.getY()); }

    public double getMaxLat() { return getMaxY(); }

    /** Returns true if the top left and bottom right coordinates of both bounding boxes are exactly equal. */
    public boolean equals(BoundingBox<C> other) {
        return equals(other, 0.0);
    }

    /**
     * Returns true if the top left and bottom right coordinates of both bounding boxes are within (inclusive) of some
     * <code>epsilon</code>.
     */
    public boolean equals(BoundingBox<C> other, double epsilon) {
        return equals(other, epsilon, epsilon);
    }

    /**
     * Returns true if the top left and bottom right coordinates of both bounding boxes are within (inclusive) of some
     * x and y epsilon.
     */
    public boolean equals(BoundingBox<C> other, double epsilonX, double epsilonY) {
        return topLeftCorner.equals(other.getTopLeftCorner(), epsilonX, epsilonY) &&
               bottomRightCorner.equals(other.getBottomRightCorner(), epsilonX, epsilonY);
    }

    @Override
    public String toString() {
        return "BoundingBox(topLeftCorner=" + topLeftCorner + ", bottomRightCorner=" + bottomRightCorner + ")";
    }
}
