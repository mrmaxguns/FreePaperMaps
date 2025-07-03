package io.github.mrmaxguns.freepapermaps.projections;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit test for simple App.
 */
public class PseudoMercatorProjectionTest {

    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() {
        Coordinate c = Coordinate.newWGS84Coordinate(24.3758030, 56.8541140);
        Projection x = new PseudoMercatorProjection(c.getX(), c.getY());
        System.out.println(x.project(Coordinate.newWGS84Coordinate(24.3758592, 56.8536411)).getX());
        System.out.println(x.project(Coordinate.newWGS84Coordinate(24.3758592, 56.8536411)).getX());
        assertTrue(true);
    }
}
