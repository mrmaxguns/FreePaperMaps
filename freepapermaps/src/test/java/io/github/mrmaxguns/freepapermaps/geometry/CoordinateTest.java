package io.github.mrmaxguns.freepapermaps.geometry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class CoordinateTest {
    @Test
    public void testConstructor() {
        double x = 9.1, y = 8.2;
        TestCoordinate coordinate = new TestCoordinate(x, y);
        assertAll(() -> assertEquals(x, coordinate.getX(), 0.0001, "Coordinate constructor should initialize x"),
                  () -> assertEquals(x, coordinate.getLon(), 0.0001,
                                     "Coordinate constructor should initialize longitude"),
                  () -> assertEquals(y, coordinate.getY(), 0.0001, "Coordinate constructor should initialize y"),
                  () -> assertEquals(y, coordinate.getLat(), 0.0001,
                                     "Coordinate constructor should initialize latitude"));
    }

    @Test
    void testAdd() {
        TestCoordinate a = new TestCoordinate(11.23, 12.42);
        TestCoordinate b = new TestCoordinate(22.30, -11.22);
        TestCoordinate result = a.add(b);
        assertAll(() -> assertEquals(33.53, result.getLon(), 0.0001, "add() should add componentwise"),
                  () -> assertEquals(1.2, result.getLat(), 0.0001, "add() should add componentwise"));
    }

    @Test
    void testSubtract() {
        TestCoordinate a = new TestCoordinate(-12.04, 66.99);
        TestCoordinate b = new TestCoordinate(-9.98, -0.01);
        TestCoordinate result = a.subtract(b);
        assertAll(() -> assertEquals(-2.06, result.getLon(), 0.0001, "subtract() should subtract componentwise"),
                  () -> assertEquals(67, result.getLat(), 0.0001, "subtract() should subtract componentwise"));
    }

    @Test
    void testScale() {
        TestCoordinate c = new TestCoordinate(-120, 15);
        TestCoordinate result = c.scale(0.5);
        assertAll(() -> assertEquals(-60, result.getX(), 0.0001, "scale() should scale each component"),
                  () -> assertEquals(7.5, result.getY(), 0.0001, "scale() should scale each component"));
    }

    @Test
    void testEqualsTrue() {
        assertTrue(new TestCoordinate(10.1, 12.4).equals(new TestCoordinate(10.1, 12.4)),
                   "equals() should check exact equality between components");
    }

    @Test
    void testEqualsFalse() {
        assertFalse(new TestCoordinate(10, 15).equals(new TestCoordinate(10, -20)),
                    "equals() should check exact equality between components");
    }

    @Test
    void testEqualsWithEpsilonTrue() {
        assertTrue(new TestCoordinate(12.573, -13.981).equals(new TestCoordinate(12.575, -13.971), 0.01),
                   "equals(other, epsilon) should use the epsilon as a bound");
    }

    @Test
    void testEqualsWithEpsilonFalse() {
        assertFalse(new TestCoordinate(8765.1, -5757.3).equals(new TestCoordinate(8765.0, -5758.4), 1),
                    "equals(other, epsilon) should use the epsilon as a bound");
    }

    @Test
    void testEqualsWithEpsilonXYTrue() {
        assertTrue(new TestCoordinate(0.5737, 14838).equals(new TestCoordinate(0.5740, 14836), 0.0005, 3),
                   "equals(other, epsilonX, epsilonY) should use the respective epsilons as bounds");
    }

    @Test
    void testEqualsWithEpsilonXYFalse() {
        assertFalse(new TestCoordinate(-9494, 11.23).equals(new TestCoordinate(-9490, 11.24), 20, 0.0001),
                    "equals(other, epsilonX, epsilonY) should use the respective epsilons as bounds");
    }

    @Test
    void testAlmostEqualsTrue() {
        assertTrue(new TestCoordinate(0, 1e30).almostEquals(new TestCoordinate(1e-12, 1e30 - (1e-10))));
    }

    @Test
    void testAlmostEqualsFalse() {
        assertFalse(new TestCoordinate(4, 5).almostEquals(new TestCoordinate(4, 5.0001)));
    }

    static class TestCoordinate extends Coordinate<TestCoordinate> {
        public TestCoordinate(double x, double y) {
            super(x, y);
        }

        @Override
        protected TestCoordinate createInstance(double x, double y) {
            return new TestCoordinate(x, y);
        }

        @Override
        public String getCoordinateType() {
            return "TestCoordinate";
        }

        @Override
        public String getCoordinateUnit() {
            return "?";
        }
    }
}
