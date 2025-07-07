package io.github.mrmaxguns.freepapermaps.projections;

/**
 * Coordinate represents an x/y or longitude/latitude position in space. Coordinates are designed to be immutable.
 */
public abstract class Coordinate<T extends Coordinate<T>> {
    protected final double x;
    protected final double y;

    public Coordinate(double xOrLon, double yOrLat) {
        x = xOrLon;
        y = yOrLat;
    }

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


    public T add(T other) {
        return createInstance(x + other.getX(), y + other.getY());
    }

    public T subtract(T other) {
        return createInstance(x - other.getX(), y - other.getY());
    }

    public T scale(double scaleFactor) {
        return createInstance(x * scaleFactor, y * scaleFactor);
    }

    public boolean equals(T other) {
        return x == other.getX() && y == other.getY();
    }

    public abstract String getCoordinateType();
    public abstract String getCoordinateUnit();

    public String toString() {
        return getCoordinateType() + "(" + x  + getCoordinateUnit() + ", " + y + getCoordinateUnit() + ")";
    }
}
