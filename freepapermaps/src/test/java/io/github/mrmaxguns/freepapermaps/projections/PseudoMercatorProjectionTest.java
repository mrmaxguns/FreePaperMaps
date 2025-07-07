package io.github.mrmaxguns.freepapermaps.projections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PseudoMercatorProjectionTest {
    private PseudoMercatorProjection defaultProjection = new PseudoMercatorProjection(Coordinate.newWGS84Coordinate(0, 0));

    @Test
    public void testConstructor() {
        Coordinate c = Coordinate.newWGS84Coordinate(10, 20);
        PseudoMercatorProjection p = new PseudoMercatorProjection(c);
        assertTrue(p.getOrigin().equals(c));
    }

    @Test
    public void testConstructorWrongCategory() {
        Coordinate c = Coordinate.newSVGCoordinate(500, 200);
        assertThrows(IllegalArgumentException.class, () -> new PseudoMercatorProjection((c)));
    }

    @Test
    public void testProjectRaw1() {
        Coordinate c = Coordinate.newWGS84Coordinate(48.307, 54.310);
        Coordinate result = defaultProjection.projectRaw(c);
        assertAll(
                () -> assertEquals(5377511, result.getX(), 1),
                () -> assertEquals(7229087, result.getY(), 1)
        );
    }

    @Test
    public void testProjectRaw2() {
        Coordinate c = Coordinate.newWGS84Coordinate(0, 0);
        Coordinate result = defaultProjection.projectRaw(c);
        assertAll(
                () -> assertEquals(0, result.getX(), 1),
                () -> assertEquals(0, result.getY(), 1)
        );
    }

    @Test
    public void testProjectRaw3() {
        Coordinate c = Coordinate.newWGS84Coordinate(-66.18430, -47.80841);
        Coordinate result = defaultProjection.projectRaw(c);
        assertAll(
                () -> assertEquals(-7367603, result.getX(), 1),
                () -> assertEquals(-6075040, result.getY(), 1)
        );
    }

    @Test
    public void testProjectRawWrongCategory() {
        Coordinate c = Coordinate.newRawCoordinate(5.5, -5.3);
        assertThrows(IllegalArgumentException.class, () -> defaultProjection.projectRaw(c));
    }

    @Test
    public void testProjectRawOutOfBoundsLon() {
        Coordinate a = Coordinate.newWGS84Coordinate(-200, 30.4);
        Coordinate b = Coordinate.newWGS84Coordinate(181.22, -14.8);
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> defaultProjection.projectRaw(a)),
                () -> assertThrows(IllegalArgumentException.class, () -> defaultProjection.projectRaw(b))
        );
    }

    @Test
    public void testProjectRawOutOfBoundsLat() {
        Coordinate a = Coordinate.newWGS84Coordinate(30, -90);
        Coordinate b = Coordinate.newWGS84Coordinate(40.5, 108.4);
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> defaultProjection.projectRaw(a)),
                () -> assertThrows(IllegalArgumentException.class, () -> defaultProjection.projectRaw(b))
        );
    }

    @Test
    public void testProjectWithOrigin() {
        Coordinate origin = Coordinate.newWGS84Coordinate(137.71654, -32.50831);
        PseudoMercatorProjection p = new PseudoMercatorProjection(origin);
        Coordinate result = p.project(origin);

        assertAll(
                () -> assertEquals(0, result.getX(), 0.0001),
                () -> assertEquals(0, result.getY(), 0.0001)
        );
    }

    @Test
    public void testProjectWithNoWrapAround() {
        Coordinate origin = Coordinate.newWGS84Coordinate(-28.32649, 38.52594);
        PseudoMercatorProjection p = new PseudoMercatorProjection(origin);
        Coordinate test = Coordinate.newWGS84Coordinate(-28.30353, 38.51352);
        Coordinate result = p.project(test);

        assertAll(
                () -> assertEquals(2555, result.getX(), 1),
                () -> assertEquals(1767, result.getY(), 1)
        );
    }

    @Test
    public void testProjectWithXWrapAround() {
        Coordinate origin = Coordinate.newWGS84Coordinate(179.8, 51.516);
        PseudoMercatorProjection p = new PseudoMercatorProjection(origin);
        Coordinate test = Coordinate.newWGS84Coordinate(-179.9, 51.50);
        Coordinate result = p.project(test);

        assertAll(
                () -> assertEquals(33396, result.getX(), 1),
                () -> assertEquals(2862, result.getY(), 1)
        );
    }
}
