package io.github.mrmaxguns.freepapermaps.geometry;

/** Coordinate represents an x/y or longitude/latitude position in space. Coordinates are immutable. */
public abstract class Coordinate<T extends Coordinate<T>> {
    /** Represents x or longitude. */
    private final double x;
    /** Represents y or latitude. */
    private final double y;

    public Coordinate(double xOrLon, double yOrLat) {
        x = xOrLon;
        y = yOrLat;
    }

    /**
     * Creates an instance of a Coordinate subclass. This is needed so that methods defined in the abstract
     * Coordinate class can create instances of the subclass.
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

    /** Returns a new Coordinate which is the sum of this one and <code>other</code>. */
    public T add(T other) {
        return createInstance(x + other.getX(), y + other.getY());
    }

    /** Returns a new Coordinate which is the result of this coordinate minus <code>other</code>. */
    public T subtract(T other) {
        return createInstance(x - other.getX(), y - other.getY());
    }

    /** Returns a new Coordinate whose components are multiplied by <code>scaleFactor</code>. */
    public T scale(double scaleFactor) {
        return createInstance(x * scaleFactor, y * scaleFactor);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Coordinate<?>)) { return false; }
        return equals((Coordinate<?>) other);
    }

    /** Returns true if both coordinates are exactly equal. */
    public boolean equals(Coordinate<?> other) {
        return equals(other, 0.0);
    }

    /** Returns true if both coordinate x and y values are within (inclusive) of some <code>epsilon</code>. */
    public boolean equals(Coordinate<?> other, double epsilon) {
        return equals(other, epsilon, epsilon);
    }

    /** Returns true if both coordinate x and y values are within (inclusive) of some x and y epsilon, respectively. */
    public boolean equals(Coordinate<?> other, double epsilonX, double epsilonY) {
        if (this == other) {
            return true;
        }
        return (Math.abs(x - other.getX()) <= epsilonX) && (Math.abs(y - other.getY()) <= epsilonY);
    }

    /**
     * Returns true if both coordinate x and y values are almost equal for the purposes of geography, regardless of
     * their magnitude.
     */
    public boolean almostEquals(Coordinate<?> other) {
        return valuesAlmostEqual(x, other.getX()) && valuesAlmostEqual(y, other.getY());
    }

    /** Returns true if two doubles are almost equal, given hard-coded minimum and relative tolerances. */
    private boolean valuesAlmostEqual(double a, double b) {
        final double absEpsilon = 1e-12; // minimum absolute threshold
        final double relEpsilon = 1e-9;  // relative tolerance

        double diff = Math.abs(a - b);
        double largest = Math.max(Math.abs(a), Math.abs(b));

        return diff <= Math.max(absEpsilon, largest * relEpsilon);
    }

    /** Returns a short, human-readable identifier of the Coordinate's type (e.g. WGS84). */
    public abstract String getCoordinateType();

    /** Returns a string representing an appropriate unit for this coordinate (e.g. m). */
    public abstract String getCoordinateUnit();

    /** Returns a String representation of this Coordinate. */
    @Override
    public String toString() {
        return getCoordinateType() + "(" + x + getCoordinateUnit() + ", " + y + getCoordinateUnit() + ")";
    }
}
