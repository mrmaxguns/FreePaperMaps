package io.github.mrmaxguns.freepapermaps.geometry;

import io.github.mrmaxguns.freepapermaps.UserInputException;
import io.github.mrmaxguns.freepapermaps.osm.Node;
import io.github.mrmaxguns.freepapermaps.osm.OSM;
import io.github.mrmaxguns.freepapermaps.osm.Way;
import io.github.mrmaxguns.freepapermaps.projections.PseudoMercatorProjection;
import io.github.mrmaxguns.freepapermaps.projections.RawCoordinate;
import io.github.mrmaxguns.freepapermaps.projections.WGS84Coordinate;
import io.github.mrmaxguns.freepapermaps.rendering.ScaledCoordinate;
import io.github.mrmaxguns.freepapermaps.rendering.Scaler;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class WayGeometryTest {
    private final WayGeometry w1;
    private final WayGeometry w2;
    private final WayGeometry w3;
    private final WayGeometry w4;
    private final WayGeometry w5;
    private final WayGeometry w6;
    private final WayGeometry w7;
    private final WayGeometry w8;

    public WayGeometryTest() {
        // Visualize the way geometry used in these tests:

        List<NodeGeometry> n1 = new ArrayList<>();
        n1.add(new NodeGeometry(new RawCoordinate(0, 0)));
        n1.add(new NodeGeometry(new RawCoordinate(1, 5)));
        n1.add(new NodeGeometry(new RawCoordinate(3, 4)));
        n1.add(new NodeGeometry(new RawCoordinate(1, -2)));
        w1 = new WayGeometry(n1);

        List<NodeGeometry> n2 = new ArrayList<>();
        n2.add(new NodeGeometry(new RawCoordinate(-2, 2)));
        n2.add(new NodeGeometry(new RawCoordinate(1, 6)));
        n2.add(new NodeGeometry(new RawCoordinate(4, 1)));
        n2.add(new NodeGeometry(new RawCoordinate(-1, -2)));
        n2.add(new NodeGeometry(new RawCoordinate(-2, 2)));
        w2 = new WayGeometry(n2);

        List<NodeGeometry> n3 = new ArrayList<>();
        n3.add(new NodeGeometry(new RawCoordinate(1, 1)));
        n3.add(new NodeGeometry(new RawCoordinate(1, 2)));
        n3.add(new NodeGeometry(new RawCoordinate(1, 2)));
        n3.add(new NodeGeometry(new RawCoordinate(3, 3)));
        w3 = new WayGeometry(n3);

        List<NodeGeometry> n4 = new ArrayList<>();
        n4.add(new NodeGeometry(new RawCoordinate(-1, -1)));
        n4.add(new NodeGeometry(new RawCoordinate(1, 0)));
        n4.add(new NodeGeometry(new RawCoordinate(-1, -1)));
        n4.add(new NodeGeometry(new RawCoordinate(1, 2)));
        w4 = new WayGeometry(n4);

        List<NodeGeometry> n5 = new ArrayList<>();
        n5.add(new NodeGeometry(new RawCoordinate(-4, 0)));
        n5.add(new NodeGeometry(new RawCoordinate(0, -4)));
        n5.add(new NodeGeometry(new RawCoordinate(-4, -4)));
        n5.add(new NodeGeometry(new RawCoordinate(-1, -1)));
        n5.add(new NodeGeometry(new RawCoordinate(-4, 0)));
        w5 = new WayGeometry(n5);

        List<NodeGeometry> n6 = new ArrayList<>();
        n6.add(new NodeGeometry(new RawCoordinate(1, -2)));
        n6.add(new NodeGeometry(new RawCoordinate(2, -3)));
        n6.add(new NodeGeometry(new RawCoordinate(5, -3)));
        w6 = new WayGeometry(n6);

        List<NodeGeometry> n7 = new ArrayList<>();
        n7.add(new NodeGeometry(new RawCoordinate(5, -3)));
        n7.add(new NodeGeometry(new RawCoordinate(6, 0)));
        n7.add(new NodeGeometry(new RawCoordinate(2, -1)));
        n7.add(new NodeGeometry(new RawCoordinate(1, -2)));
        w7 = new WayGeometry(n7);

        List<NodeGeometry> n8 = new ArrayList<>();
        n8.add(new NodeGeometry(new RawCoordinate(-2, 2)));
        n8.add(new NodeGeometry(new RawCoordinate(-4, 0)));
        w8 = new WayGeometry(n8);
    }

    @Test
    public void testConstructor() {
        List<NodeGeometry> nodes = new ArrayList<>();

        NodeGeometry n1 = new NodeGeometry(new WGS84Coordinate(0, 1));
        nodes.add(n1);
        nodes.add(new NodeGeometry(new WGS84Coordinate(1, 1)));
        NodeGeometry n3 = new NodeGeometry(new WGS84Coordinate(3, 4));
        nodes.add(n3);

        WayGeometry g = new WayGeometry(nodes);

        assertAll(() -> assertEquals(nodes, g.getNodes(), "nodes should remain in order"),
                  () -> assertThrows(UnsupportedOperationException.class, () -> g.getNodes().add(null),
                                     "the WayGeometry node list should be immutable"),
                  () -> assertEquals(n1, g.getFirstNode(), "getFirstNode() should return the first node"),
                  () -> assertEquals(n3, g.getLastNode(), "getLastNode() should return the last node"));
    }

    @Test
    public void testConstructorInvalid() {
        List<NodeGeometry> nodes = new ArrayList<>();
        nodes.add(new NodeGeometry(new RawCoordinate(1, 1)));
        assertThrows(IllegalArgumentException.class, () -> new WayGeometry(nodes),
                     "way geometry construction should fail if there are less than two nodes");
    }

    @Test
    public void testFromOSM() throws UserInputException {
        OSM osm = new OSM();
        Way way = new Way(3939, true);
        way.getTags().put("highway", "footway");
        WGS84Coordinate p1 = new WGS84Coordinate(66.19, -19.11);
        Node n1 = new Node(94949, p1, true);
        Node n2 = new Node(93933, new WGS84Coordinate(67.11, -19.22), true);
        n2.getTags().put("ford", "yes");
        Node n3 = new Node(93490, new WGS84Coordinate(69.01, -19.33), true);
        way.addNodeId(94949);
        way.addNodeId(93933);
        way.addNodeId(93490);
        osm.addWay(way);
        osm.addNode(n1);
        osm.addNode(n2);
        osm.addNode(n3);

        WayGeometry g1 = WayGeometry.fromOSM(osm, way);
        WayGeometry g2 = WayGeometry.fromOSM(osm, way, new PseudoMercatorProjection(p1), new Scaler(1));

        assertAll(() -> assertEquals(3, g1.getNodes().size(), "all nodes in the way should be added"),
                  () -> assertEquals(3, g2.getNodes().size(), "all nodes in the way should be added"),

                  () -> assertEquals("yes", g1.getNodes().get(1).getTags().get("ford"),
                                     "child node tags should be added; child nodes should be in order"),
                  () -> assertEquals("yes", g2.getNodes().get(1).getTags().get("ford"),
                                     "child node tags should be added; child nodes should be in order"),

                  () -> assertEquals("footway", g1.getTags().get("highway"), "tags should be added"),
                  () -> assertEquals("footway", g2.getTags().get("highway"), "tags should be added"),

                  () -> assertTrue(p1.equals(g1.getFirstNode().getPosition()),
                                   "no projecting/scaling should be done if it isn't requested"),
                  () -> assertTrue(g2.getFirstNode().getPosition().equals(new ScaledCoordinate(0, 0)),
                                   "projecting and scaling should be done if specified"));
    }

    @Test
    public void testIsClosed() {
        assertAll(() -> assertFalse(w1.isClosed(), "unclosed ways should be identified"),
                  () -> assertTrue(w2.isClosed(), "closed ways should be identified"),
                  () -> assertFalse(w3.isClosed(), "unclosed ways should be identified"),
                  () -> assertFalse(w4.isClosed(), "unclosed ways should be identified"),
                  () -> assertTrue(w5.isClosed(), "closed ways should be identified"));
    }

    @Test
    public void testEquals() {
        assertAll(() -> assertTrue(w1.equals(new WayGeometry(w1.getNodes())),
                                   "ways with exact nodes in the exact order should be equal"),
                  () -> assertFalse(w1.equals(w2), "unequal ways should be identified"));
    }

    @Test
    public void testIsValid() {
        List<NodeGeometry> badGeometryList = new ArrayList<>();
        badGeometryList.add(new NodeGeometry(new RawCoordinate(Double.NaN, 3)));
        badGeometryList.add(new NodeGeometry(new RawCoordinate(6.5, 1.2)));
        WayGeometry badGeometry = new WayGeometry(badGeometryList);

        assertAll(() -> assertFalse(badGeometry.isValid(),
                                    "ways with points with invalid double values should be considered invalid"),
                  () -> assertTrue(w1.isValid(), "valid ways should be identified"),
                  () -> assertTrue(w2.isValid(), "valid ways should be identified"),
                  () -> assertTrue(w3.isValid(), "valid ways should be identified"),
                  () -> assertTrue(w4.isValid(), "valid ways should be identified"),
                  () -> assertTrue(w5.isValid(), "valid ways should be identified"));
    }

    @Test
    public void testIsCompletelyValid() {
        assertAll(() -> assertTrue(w1.isCompletelyValid(), "completely valid ways should be identified"),
                  () -> assertTrue(w2.isCompletelyValid(), "completely valid ways should be identified"),
                  () -> assertFalse(w3.isCompletelyValid(), "ways with degenerate segments should be marked invalid"),
                  () -> assertFalse(w4.isCompletelyValid(), "ways with spikes should be marked invalid"),
                  () -> assertTrue(w5.isCompletelyValid(), "completely valid ways should be identified"));
    }

    @Test
    public void testIsValidRing() {
        assertAll(() -> assertFalse(w1.isValidRing(), "unclosed ways should not be identified as rings"),
                  () -> assertTrue(w2.isValidRing(), "valid rings should be identified"),
                  () -> assertFalse(w5.isValidRing(), "self-intersecting ways should not be counted as rings"));
    }

    @Test
    public void testIntersects() {
        assertAll(() -> assertTrue(w2.intersects(w1), "intersecting ways should be identified"),
                  () -> assertTrue(w5.intersects(w2), "intersecting ways should be identified"),
                  () -> assertFalse(w5.intersects(w4), "ways that simply touch should not be considered intersecting"),
                  () -> assertFalse(w3.intersects(w5), "ways that don't intersect should be identified"),
                  () -> assertTrue(w5.intersects(null), "ways that self-intersect should be identified"),
                  () -> assertTrue(w4.intersects(null),
                                   "ways that have overlapping segments should be considered self-intersecting"),
                  () -> assertFalse(w2.intersects(null), "closed ways should not be considered self-intersecting"),
                  () -> assertFalse(w1.intersects(null), "ways that don't self-intersect should be identified"));
    }

    @Test
    public void testCanBeCombined() {
        assertAll(() -> assertTrue(w1.canBeCombined(w6), "ways that can be combined should be identified"),
                  () -> assertTrue(w6.canBeCombined(w1), "ways that can be combined should be identified"),
                  () -> assertTrue(w6.canBeCombined(w7), "ways that can be combined into a ring should be identified"),
                  () -> assertFalse(w4.canBeCombined(w5), "ways that can't be combined should be identified"),
                  () -> assertFalse(w8.canBeCombined(w2), "ways that can be combined should be identified"));
    }

    @Test
    public void testCombine() {
        NodeGeometry[] pointsArray = new NodeGeometry[]{ new NodeGeometry(new RawCoordinate(0, 0)),
                new NodeGeometry(new RawCoordinate(1, 5)), new NodeGeometry(new RawCoordinate(3, 4)),
                new NodeGeometry(new RawCoordinate(1, -2)), new NodeGeometry(new RawCoordinate(2, -3)),
                new NodeGeometry(new RawCoordinate(5, -3)) };
        ArrayList<NodeGeometry> r1 = new ArrayList<>(Arrays.asList(pointsArray));


        NodeGeometry[] pointsArray2 = new NodeGeometry[]{ new NodeGeometry(new RawCoordinate(0, 0)),
                new NodeGeometry(new RawCoordinate(1, 5)), new NodeGeometry(new RawCoordinate(3, 4)),
                new NodeGeometry(new RawCoordinate(1, -2)), new NodeGeometry(new RawCoordinate(2, -1)),
                new NodeGeometry(new RawCoordinate(6, 0)), new NodeGeometry(new RawCoordinate(5, -3)) };
        ArrayList<NodeGeometry> r2 = new ArrayList<>(Arrays.asList(pointsArray2));

        assertAll(() -> assertEquals(r1, w1.combine(w6).getNodes(),
                                     "combine() should handle two ways in the same direction"),
                  () -> assertEquals(r1, w6.combine(w1).getNodes(),
                                     "combine() should handle two ways in the same direction"),
                  () -> assertEquals(r2, w1.combine(w7).getNodes(),
                                     "combine() should handle two ways going in opposite directions"));
    }
}
