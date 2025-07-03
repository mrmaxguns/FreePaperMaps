package io.github.mrmaxguns.freepapermaps.projections;

/**
 * Coordinate represents an x/y or longitude/latitude position in space. Coordinates are designed to be immutable.
 */
public class Coordinate {
    /**
     * Either an x position or a longitude
     */
    final private double x;
    /**
     * Either a y position or a latitude
     */
    final private double y;

    /**
     * Category defines the units for a coordinate. It is imperative to specify the following:
     * <ul>
     *     <li>Whether the coordinate is in degrees (lat/lon) or an x/y distance</li>
     *     <li>What the datum is (for lat/lon coordinates)</li>
     *     <li>Whether the coordinates are meant to represent real distances or on-screen/SVG distances</li>
     * </ul>
     * The essence of what FreePaperMaps does is as follows:
     * <ul>
     *     <li>
     *         A WGS84 coordinate is <em>projected</em> to an InternalMeters coordinate value. (A projection may need
     *         to use intermediate values, which are stored in Raw coordinate values.)
     *     </li>
     *     <li>
     *         The InternalMeters coordinate is scaled based on the user's needs and converted into SVGUserUnits to
     *         be displayed in the final map.
     *     </li>
     * </ul>
     */
    public enum Category {
        /**
         * A WGS84 lat/lon coordinate. This is the standard for coordinates in OSM.
         */
        WGS84,
        /**
         * A generic value meaning some internal unit that is neither an input nor output of the application.
         */
        Raw,
        /**
         * An internally used coordinate system similar to the one used in computers:
         * <ul>
         *     <li>The origin is located at (0, 0) and is in the top left corner of the "map"</li>
         *     <li>
         *         Point values are x and y distances in meters. Distances are only positive and represent distance to
         *         the right (x) or down (y)
         *     </li>
         * </ul>
         */
        InternalMeters,
        /**
         * A value in SVG user units. This is based on the SVG coordinate system.
         */
        SVGUserUnits,
    }

    /**
     * The category of this coordinate.
     */
    final private Category category;

    /**
     * Constructs a coordinate value from an x/y or lat/lon pair.
     *
     * @param xOrLon   either an x-value or longitude
     * @param yOrLat   either a y-value or latitude
     * @param category the coordinate's category, which contextualizes the values stored in the coordinate
     */
    public Coordinate(double xOrLon, double yOrLat, Category category) {
        x = xOrLon;
        y = yOrLat;
        this.category = category;
    }

    /**
     * Static constructor to create a WGS84 coordinate.
     */
    public static Coordinate newWGS84Coordinate(double lon, double lat) {
        return new Coordinate(lon, lat, Category.WGS84);
    }

    /**
     * Static constructor to create a raw coordinate.
     */
    public static Coordinate newRawCoordinate(double xOrLon, double yOrLat) {
        return new Coordinate(xOrLon, yOrLat, Category.Raw);
    }

    /**
     * Static constructor to create an internal (InternalMeters) coordinate.
     */
    public static Coordinate newInternalCoordinate(double x, double y) {
        return new Coordinate(x, y, Category.InternalMeters);
    }

    /**
     * Static constructor to create an SVG (SVG user unit) coordinate.
     */
    public static Coordinate newSVGCoordinate(double x, double y) {
        return new Coordinate(x, y, Category.SVGUserUnits);
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

    /**
     * Returns a new coordinate that is the sum of this one and other.
     * @throws IllegalArgumentException when the coordinates don't match
     */
    public Coordinate add(Coordinate other) {
        ensureCategoriesMatch(other);
        return new Coordinate(x + other.x, y + other.y, category);
    }

    /**
     * Returns a new coordinate that is the this coordinate minus other.
     * @throws IllegalArgumentException when the coordinates don't match
     */
    public Coordinate subtract(Coordinate other) {
        ensureCategoriesMatch(other);
        return new Coordinate(x - other.x, y - other.y, category);
    }

    /** Returns a string representation of this coordinate. */
    public String toString() {
        String prefix;
        switch (category) {
            case WGS84 -> prefix = "WGS84";
            case Raw -> prefix = "Raw";
            case InternalMeters -> prefix = "Internal";
            case SVGUserUnits -> prefix = "UserUnits";
            default -> prefix = "Coordinate";
        }
        return prefix + "(" + x + ", " + y + ")";
    }
}
