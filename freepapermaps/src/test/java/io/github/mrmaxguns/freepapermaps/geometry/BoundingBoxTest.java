package io.github.mrmaxguns.freepapermaps.geometry;

import io.github.mrmaxguns.freepapermaps.projections.RawCoordinate;
import io.github.mrmaxguns.freepapermaps.projections.WGS84Coordinate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class BoundingBoxTest {
    private static final BoundingBox<WGS84Coordinate> bbox1 = new BoundingBox<>(new WGS84Coordinate(55.497, 19.438),
                                                                                new WGS84Coordinate(56.132, 18.841));
    private static final BoundingBox<RawCoordinate> bbox2 = new BoundingBox<>(new RawCoordinate(0, 0),
                                                                              new RawCoordinate(150, 345));

    @Test
    public void testConstructor() {
        assertAll(
                () -> assertEquals(55.497, bbox1.getTopLeftCorner().getLon(), "bounding box should initialize bounds"),
                () -> assertEquals(19.438, bbox1.getTopLeftCorner().getLat(), "bounding box should initialize bounds"),
                () -> assertEquals(56.132, bbox1.getBottomRightCorner().getLon(),
                                   "bounding box should initialize bounds"),
                () -> assertEquals(18.841, bbox1.getBottomRightCorner().getLat(),
                                   "bounding box should initialize bounds"),
                () -> assertEquals(0.635, bbox1.getWidth(), 0.0001, "bounding box should initialize width"),
                () -> assertEquals(0.597, bbox1.getHeight(), 0.0001, "bounding box should initialize height"),
                () -> assertEquals(55.497, bbox1.getMinLon(), "bounding box should determine the min longitude"),
                () -> assertEquals(bbox1.getMinLon(), bbox1.getMinX(),
                                   "getMinLon() and getMinX() should always return the same thing"),
                () -> assertEquals(56.132, bbox1.getMaxLon(), "bounding box should determine the max longitude"),
                () -> assertEquals(bbox1.getMaxLon(), bbox1.getMaxX(),
                                   "getMaxLon() and getMaxX() should always return the same thing"),
                () -> assertEquals(18.841, bbox1.getMinLat(), "bounding box should determine the min latitude"),
                () -> assertEquals(bbox1.getMinLat(), bbox1.getMinY(),
                                   "getMinLat() and getMinY() should always return the same thing"),
                () -> assertEquals(19.438, bbox1.getMaxLat(), "bounding box should determine the max latitude"),
                () -> assertEquals(bbox1.getMaxLat(), bbox1.getMaxY(),
                                   "getMaxLat() and getMaxY() should always return the same thing"));
    }

    @Test
    public void testWidth() {
        assertAll(() -> assertEquals(150, bbox2.getWidth(),
                                     "bounding box should return a positive width, even for non-cartesian coordinates"),
                  () -> assertEquals(345, bbox2.getHeight(),
                                     "bounding box should return a positive height, even for non-cartesian " +
                                     "coordinates"));
    }

    @Test
    public void testEqualsTrue() {
        //noinspection EqualsWithItself
        assertAll(() -> assertTrue(bbox1.equals(bbox1), "the same bounding box object should equal itself"),
                  () -> assertTrue(
                          bbox1.equals(new BoundingBox<>(bbox1.getTopLeftCorner(), bbox1.getBottomRightCorner())),
                          "two identical bounding boxes that are different instances should be equal"));
    }

    @Test
    public void testEqualsFalse() {
        assertNotEquals(bbox1, new BoundingBox<>(new WGS84Coordinate(55, 19), new WGS84Coordinate(56, 18)),
                        "two bounding boxes whose components are not exactly equal are not equal");
    }

    @Test
    public void testEqualsEpsilonTrue() {
        assertTrue(
                bbox1.equals(new BoundingBox<>(new WGS84Coordinate(55.5, 19.4), new WGS84Coordinate(56.1, 18.8)), 0.1),
                "when an epsilon is specified, bounding boxes equal within the epsilon are considered equal");
    }

    @Test
    public void testEqualsEpsilonFalse() {
        assertFalse(bbox1.equals(
                            new BoundingBox<>(new WGS84Coordinate(55.497, 19.438), new WGS84Coordinate(56.133,
                                                                                                       18.841)),
                            0.0001),
                    "when an epsilon is specified, bounding boxes not equal within the epsilon are not considered " +
                    "equal");
    }

    @Test
    public void testEqualsEpsilonXYTrue() {
        assertTrue(bbox2.equals(new BoundingBox<>(new RawCoordinate(5, 0.1), new RawCoordinate(143, 344.5)), 8, 1),
                   "when x, y epsilons are specified, bounding boxes equal within the parameters are considered equal");
    }

    @Test
    public void testEqualsEpsilonXYFalse() {
        assertFalse(bbox2.equals(new BoundingBox<>(new RawCoordinate(1, 1), new RawCoordinate(150, 345)), 2, 0.1),
                    "when x, y epsilons are specified, bounding boxes not equal within the parameters are not " +
                    "considered equal");
    }

    @Test
    public void testAlmostEquals() {
        WGS84Coordinate a = new WGS84Coordinate(34.4, -11.5);
        WGS84Coordinate b = new WGS84Coordinate(35.6, -12.0);
        WGS84Coordinate c = new WGS84Coordinate(1e-13, -1e-15);
        assertTrue(new BoundingBox<>(a, b).almostEquals(new BoundingBox<>(a.add(c), b.add(c))));
    }
}
