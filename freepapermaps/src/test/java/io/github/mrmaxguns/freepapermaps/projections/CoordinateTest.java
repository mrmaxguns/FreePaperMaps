package io.github.mrmaxguns.freepapermaps.projections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CoordinateTest {
    @Test
    void testConstructor() {
        Coordinate c = new Coordinate(50.3, 11.2, Coordinate.Category.Raw);
        assertAll(
                () -> assertEquals(50.3, c.getX(), 0.0001),
                () -> assertEquals(11.2, c.getY(), 0.0001),
                () -> assertEquals(Coordinate.Category.Raw, c.getCategory())
        );
    }

    @Test
    void testAdd() {
        Coordinate a = Coordinate.newWGS84Coordinate(11.23, 12.42);
        Coordinate b = Coordinate.newWGS84Coordinate(22.30, -11.22);
        Coordinate result = a.add(b);
        assertAll(
                () -> assertEquals(33.53, result.getLon(), 0.0001),
                () -> assertEquals(1.2, result.getLat(), 0.0001)
        );
    }

    @Test
    void testAddFailsWithDifferingCategories() {
        Coordinate a = Coordinate.newRawCoordinate(11, 12);
        Coordinate b = Coordinate.newSVGCoordinate(15, -0.06);
        assertThrows(IllegalArgumentException.class, () -> a.add(b));
    }

    @Test
    void testSubtract() {
        Coordinate a = Coordinate.newWGS84Coordinate(-12.04, 66.99);
        Coordinate b = Coordinate.newWGS84Coordinate(-9.98, -0.01);
        Coordinate result = a.subtract(b);
        assertAll(
                () -> assertEquals(-2.06, result.getLon(), 0.0001),
                () -> assertEquals(67, result.getLat(), 0.0001)
        );
    }

    @Test
    void testSubtractFailsWithDifferingCategories() {
        Coordinate a = Coordinate.newWGS84Coordinate(69.3, 12.12);
        Coordinate b = Coordinate.newInternalCoordinate(-915, 19.6);
        assertThrows(IllegalArgumentException.class, () -> a.subtract(b));
    }

    @Test
    void testScale() {
        Coordinate c = Coordinate.newSVGCoordinate(-120, 15);
        Coordinate result = c.scale(0.5);
        assertAll(
                () -> assertEquals(-60, result.getX(), 0.0001),
                () -> assertEquals(7.5, result.getY(), 0.0001)
        );
    }

    @Test
    void testEqualsTrue() {
        //noinspection EqualsWithItself
        assertTrue(Coordinate.newInternalCoordinate(10.1, 12.4).equals(Coordinate.newInternalCoordinate(10.1, 12.4)));
    }

    @Test
    void testEqualsDifferentCategories() {
        assertFalse(Coordinate.newSVGCoordinate(5, -6).equals(Coordinate.newRawCoordinate(5, -6)));
    }

    @Test
    void testEqualsDifferentValues() {
        assertFalse(Coordinate.newWGS84Coordinate(10, 15).equals(Coordinate.newWGS84Coordinate(10, -20)));
    }
}
