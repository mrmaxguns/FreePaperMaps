package io.github.mrmaxguns.freepapermaps.projections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PseudoMercatorProjectionTest {
    private final PseudoMercatorProjection defaultProjection = new PseudoMercatorProjection(new WGS84Coordinate(0, 0));

    @Test
    public void testConstructor() {
        WGS84Coordinate c = new WGS84Coordinate(10, 20);
        PseudoMercatorProjection p = new PseudoMercatorProjection(c);
        assertTrue(p.getOrigin().equals(c));
    }

    @Test
    public void testProjectRaw1() {
        WGS84Coordinate c = new WGS84Coordinate(48.307, 54.310);
        RawCoordinate result = defaultProjection.projectRaw(c);
        assertAll(
                () -> assertEquals(5377511, result.getX(), 1),
                () -> assertEquals(7229087, result.getY(), 1)
        );
    }

    @Test
    public void testProjectRaw2() {
        WGS84Coordinate c = new WGS84Coordinate(0, 0);
        RawCoordinate result = defaultProjection.projectRaw(c);
        assertAll(
                () -> assertEquals(0, result.getX(), 1),
                () -> assertEquals(0, result.getY(), 1)
        );
    }

    @Test
    public void testProjectRaw3() {
        WGS84Coordinate c = new WGS84Coordinate(-66.18430, -47.80841);
        RawCoordinate result = defaultProjection.projectRaw(c);
        assertAll(
                () -> assertEquals(-7367603, result.getX(), 1),
                () -> assertEquals(-6075040, result.getY(), 1)
        );
    }

    @Test
    public void testProjectRawOutOfBoundsLon() {
        WGS84Coordinate a = new WGS84Coordinate(-200, 30.4);
        WGS84Coordinate b = new WGS84Coordinate(181.22, -14.8);
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> defaultProjection.projectRaw(a)),
                () -> assertThrows(IllegalArgumentException.class, () -> defaultProjection.projectRaw(b))
        );
    }

    @Test
    public void testProjectRawOutOfBoundsLat() {
        WGS84Coordinate a = new WGS84Coordinate(30, -90);
        WGS84Coordinate b = new WGS84Coordinate(40.5, 108.4);
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> defaultProjection.projectRaw(a)),
                () -> assertThrows(IllegalArgumentException.class, () -> defaultProjection.projectRaw(b))
        );
    }

    @Test
    public void testProjectWithOrigin() {
        WGS84Coordinate origin = new WGS84Coordinate(137.71654, -32.50831);
        PseudoMercatorProjection p = new PseudoMercatorProjection(origin);
        ProjectedCoordinate result = p.project(origin);

        assertAll(
                () -> assertEquals(0, result.getX(), 0.0001),
                () -> assertEquals(0, result.getY(), 0.0001)
        );
    }

    @Test
    public void testProjectWithNoWrapAround() {
        WGS84Coordinate origin = new WGS84Coordinate(-28.32649, 38.52594);
        PseudoMercatorProjection p = new PseudoMercatorProjection(origin);
        WGS84Coordinate test = new WGS84Coordinate(-28.30353, 38.51352);
        ProjectedCoordinate result = p.project(test);

        assertAll(
                () -> assertEquals(2555, result.getX(), 1),
                () -> assertEquals(1767, result.getY(), 1)
        );
    }

    @Test
    public void testProjectWithXWrapAround() {
        WGS84Coordinate origin = new WGS84Coordinate(179.8, 51.516);
        PseudoMercatorProjection p = new PseudoMercatorProjection(origin);
        WGS84Coordinate test = new WGS84Coordinate(-179.9, 51.50);
        ProjectedCoordinate result = p.project(test);

        assertAll(
                () -> assertEquals(33396, result.getX(), 1),
                () -> assertEquals(2862, result.getY(), 1)
        );
    }
}
