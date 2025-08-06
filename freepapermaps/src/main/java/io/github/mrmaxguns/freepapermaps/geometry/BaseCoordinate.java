package io.github.mrmaxguns.freepapermaps.geometry;

/** BaseCoordinate represents an x/y or longitude/latitude position in space. Coordinates are immutable. */
public abstract class BaseCoordinate<T extends BaseCoordinate<T>> implements Coordinate {
    /** Represents x or longitude. */
    protected final double x;
    /** Represents y or latitude. */
    protected final double y;

    public BaseCoordinate(double xOrLon, double yOrLat) {
        x = xOrLon;
        y = yOrLat;
    }

    /**
     * Creates an instance of a BaseCoordinate subclass. This is needed so that methods defined in the abstract
     * BaseCoordinate
     * class can create instances of the subclass.
     */
    protected abstract T createInstance(double xOrLon, double yOrLat);

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getLon() {
        return x;
    }

    public double getLat() {
        return y;
    }

    /** Returns a new BaseCoordinate which is the sum of this one and <code>other</code>. */
    public T add(T other) {
        return createInstance(x + other.getX(), y + other.getY());
    }

    /** Returns a new BaseCoordinate which is the result of this coordinate minus <code>other</code>. */
    public T subtract(T other) {
        return createInstance(x - other.getX(), y - other.getY());
    }

    /** Returns a new BaseCoordinate whose components are multiplied by <code>scaleFactor</code>. */
    public T scale(double scaleFactor) {
        return createInstance(x * scaleFactor, y * scaleFactor);
    }

    /** Returns true if both coordinates are exactly equal. */
    public boolean equals(T other) {
        return equals(other, 0.0);
    }

    /** Returns true if both coordinate x and y values are within (inclusive) of some <code>epsilon</code>. */
    public boolean equals(T other, double epsilon) {
        return equals(other, epsilon, epsilon);
    }

    /** Returns true if both coordinate x and y values are within (inclusive) of some x and y epsilon, respectively. */
    public boolean equals(T other, double epsilonX, double epsilonY) {
        if (this == other) {
            return true;
        }
        return (Math.abs(this.x - other.x) <= epsilonX) && (Math.abs(this.y - other.y) <= epsilonY);
    }

    /** Returns a short, human-readable identifier of the BaseCoordinate's type (e.g. WGS84). */
    public abstract String getCoordinateType();

    /** Returns a string representing an appropriate unit for this coordinate (e.g. m). */
    public abstract String getCoordinateUnit();

    /** Returns a String representation of this BaseCoordinate. */
    public String toString() {
        return getCoordinateType() + "(" + x  + getCoordinateUnit() + ", " + y + getCoordinateUnit() + ")";
    }
}
