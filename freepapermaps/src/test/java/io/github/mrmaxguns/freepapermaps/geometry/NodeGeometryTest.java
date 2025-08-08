package io.github.mrmaxguns.freepapermaps.geometry;

import io.github.mrmaxguns.freepapermaps.osm.Node;
import io.github.mrmaxguns.freepapermaps.projections.Projection;
import io.github.mrmaxguns.freepapermaps.projections.PseudoMercatorProjection;
import io.github.mrmaxguns.freepapermaps.projections.WGS84Coordinate;
import io.github.mrmaxguns.freepapermaps.rendering.ScaledCoordinate;
import io.github.mrmaxguns.freepapermaps.rendering.Scaler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class NodeGeometryTest {
    @Test
    public void testConstructor() {
        WGS84Coordinate pos = new WGS84Coordinate(10, -20);
        NodeGeometry n = new NodeGeometry(pos);
        assertTrue(pos.equals(n.getPosition()), "node geometry should initialize its position");
    }

    @Test
    public void testConstructorNull() {
        assertThrows(NullPointerException.class, () -> new NodeGeometry(null),
                     "node geometry shouldn't permit null values");
    }

    @Test
    public void testFromOSMNoProjection() {
        WGS84Coordinate pos = new WGS84Coordinate(-24.4, 35.9);
        Node n = new Node(4, pos, true);
        NodeGeometry g = NodeGeometry.fromOSM(n);
        assertTrue(pos.equals(g.getPosition()),
                   "fromOSM with just an OSM node should initialize position without projecting");
    }

    @Test
    public void testFromOSMWithProjection() {
        WGS84Coordinate pos = new WGS84Coordinate(-50.5, 34.44);
        Node n = new Node(1484, pos, true);
        Projection p = new PseudoMercatorProjection(pos);
        Scaler s = new Scaler(1);
        NodeGeometry g = NodeGeometry.fromOSM(n, p, s);
        assertTrue(new ScaledCoordinate(0, 0).almostEquals(g.getPosition()),
                   "fromOSM with a projection and scaler should correctly project and scale its input position");
    }

    @Test
    public void testFromOSMMaintainsTags() {
        Node n = new Node(50595, new WGS84Coordinate(11.1, -40.1), true);
        n.getTags().put("building", "hospital");
        n.getTags().put("amenity", "hospital");
        n.getTags().put("name", "Dell Medical Center");

        NodeGeometry g1 = NodeGeometry.fromOSM(n);
        NodeGeometry g2 = NodeGeometry.fromOSM(n, new PseudoMercatorProjection(new WGS84Coordinate(-1, 1)),
                                               new Scaler(11.1));

        assertAll(() -> assertEquals(n.getTags(), g1.getTags(), "tags should be copied from the OSM node"),
                  () -> assertEquals(n.getTags(), g2.getTags(), "tags should be copied from the OSM node"));
    }

    @Test
    public void testIsValidAndIsCompletelyValid() {
        NodeGeometry g = new NodeGeometry(new WGS84Coordinate(44.4, -89.9));

        assertAll(() -> assertTrue(g.isValid(), "all NodeGeometry is guaranteed to be valid"),
                  () -> assertTrue(g.isCompletelyValid(), "all NodeGeometry is guaranteed to be completely valid"));
    }

    @Test
    public void testEquals() {
        WGS84Coordinate c1 = new WGS84Coordinate(11.1, 12.2);
        WGS84Coordinate c2 = new WGS84Coordinate(11.1 - 1e-14, 12.2 + 3e-16);
        NodeGeometry g1 = new NodeGeometry(c1);
        NodeGeometry g2 = new NodeGeometry(c2);

        assertTrue(g1.equals(g2), "very close positions in space should be treated as the same position by equals()");
    }
}
