package io.github.mrmaxguns.freepapermaps.projections;

public class Coordinate {
    protected double x;
    protected double y;

    // WGS84 ---projection--> InternalMeters ---user-specified scale + dimensions---> SVGUserUnits
    public enum Category {
        WGS84,
        Raw,
        InternalMeters,
        SVGUserUnits,
    }

    protected Category category;

    public Coordinate(double xOrLon, double yOrLat, Category category) {
        x = xOrLon;
        y = yOrLat;
        this.category = category;
    }

    public static Coordinate createWGS84(double lon, double lat) {
        return new Coordinate(lon, lat, Category.WGS84);
    }

    public static Coordinate createRaw(double xOrLon, double yOrLat) {
        return new Coordinate(xOrLon, yOrLat, Category.Raw);
    }

    public static Coordinate createInternal(double x, double y) {
        return new Coordinate(x, y, Category.InternalMeters);
    }

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

    public Category getCategory() {
        return category;
    }

    private void ensureCategoriesMatch(Coordinate other) {
        if (category != other.getCategory()) {
            throw new IllegalArgumentException("Coordinate categories do not match.");
        }
    }

    public Coordinate add(Coordinate other) {
        ensureCategoriesMatch(other);
        return new Coordinate(x + other.x, y + other.y, category);
    }

    public Coordinate subtract(Coordinate other) {
        ensureCategoriesMatch(other);
        return new Coordinate(x - other.x, y - other.y, category);
    }
}

