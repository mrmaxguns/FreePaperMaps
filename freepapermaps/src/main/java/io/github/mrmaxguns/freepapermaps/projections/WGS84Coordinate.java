package io.github.mrmaxguns.freepapermaps.projections;

/** A WGS84 lat/lon coordinate. This is the standard for coordinates in OSM. */
public class WGS84Coordinate extends Coordinate<WGS84Coordinate> {
    public WGS84Coordinate(double lon, double lat) {
        super(lon, lat);
    }

    protected WGS84Coordinate createInstance(double lon, double lat) {
        return new WGS84Coordinate(lon, lat);
    }

    public String getCoordinateType() {
        return "WGS84";
    }

    public String getCoordinateUnit() {
        return "deg";
    }
}
