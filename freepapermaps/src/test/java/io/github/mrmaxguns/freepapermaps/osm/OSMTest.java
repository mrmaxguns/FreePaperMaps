package io.github.mrmaxguns.freepapermaps.osm;

import io.github.mrmaxguns.freepapermaps.UserInputException;
import io.github.mrmaxguns.freepapermaps.projections.BoundingBox;
import io.github.mrmaxguns.freepapermaps.projections.WGS84Coordinate;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OSMTest {
    private final static String VALID_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <osm version="0.6" generator="openstreetmap-cgimap 2.0.1 (1384516 spike-08.openstreetmap.org)" copyright="OpenStreetMap and contributors" attribution="http://www.openstreetmap.org/copyright" license="http://opendatacommons.org/licenses/odbl/1-0/">
             <bounds minlat="30.2727120" minlon="-97.7001980" maxlat="30.2731710" maxlon="-97.6992270"/>
             <node id="2933410027" visible="true" version="2" changeset="98040654" timestamp="2021-01-24T00:40:07Z" user="raspberrycoffee" uid="11015901" lat="30.2725370" lon="-97.7000895"/>
             <node id="2933410028" visible="true" version="1" changeset="23165723" timestamp="2014-06-25T20:40:24Z" user="azerial" uid="1933664" lat="30.2725910" lon="-97.7001599"/>
             <node id="2933410029" visible="true" version="2" changeset="98040654" timestamp="2021-01-24T00:40:07Z" user="raspberrycoffee" uid="11015901" lat="30.2726688" lon="-97.6993286"/>
             <node id="2933410030" visible="true" version="2" changeset="98040654" timestamp="2021-01-24T00:40:07Z" user="raspberrycoffee" uid="11015901" lat="30.2726967" lon="-97.7001258"/>
             <node id="2933410031" visible="true" version="1" changeset="23165723" timestamp="2014-06-25T20:40:24Z" user="azerial" uid="1933664" lat="30.2727326" lon="-97.6993442"/>
             <node id="2933410032" visible="true" version="1" changeset="23165723" timestamp="2014-06-25T20:40:24Z" user="azerial" uid="1933664" lat="30.2727345" lon="-97.7001933"/>
             <node id="2933410033" visible="true" version="2" changeset="98040654" timestamp="2021-01-24T00:40:07Z" user="raspberrycoffee" uid="11015901" lat="30.2728227" lon="-97.6993604"/>
             <node id="2933410034" visible="true" version="1" changeset="23165723" timestamp="2014-06-25T20:40:24Z" user="azerial" uid="1933664" lat="30.2728833" lon="-97.6993126"/>
             <node id="2933410035" visible="true" version="1" changeset="23165723" timestamp="2014-06-25T20:40:24Z" user="azerial" uid="1933664" lat="30.2728908" lon="-97.6990835"/>
             <node id="2933410036" visible="true" version="4" changeset="27237341" timestamp="2014-12-04T15:29:22Z" user="nubs" uid="1916849" lat="30.2732045" lon="-97.7002980"/>
             <node id="2933427972" visible="true" version="2" changeset="98040654" timestamp="2021-01-24T00:40:07Z" user="raspberrycoffee" uid="11015901" lat="30.2728308" lon="-97.6999176"/>
             <node id="2933427973" visible="true" version="2" changeset="98040654" timestamp="2021-01-24T00:40:07Z" user="raspberrycoffee" uid="11015901" lat="30.2728902" lon="-97.6995964"/>
             <node id="2933427974" visible="true" version="2" changeset="98040654" timestamp="2021-01-24T00:40:07Z" user="raspberrycoffee" uid="11015901" lat="30.2729138" lon="-97.7000064"/>
             <node id="2933427975" visible="true" version="2" changeset="98040654" timestamp="2021-01-24T00:40:07Z" user="raspberrycoffee" uid="11015901" lat="30.2729258" lon="-97.6999411"/>
             <node id="2933427976" visible="true" version="2" changeset="98040654" timestamp="2021-01-24T00:40:07Z" user="raspberrycoffee" uid="11015901" lat="30.2729507" lon="-97.6996114"/>
             <node id="2933427977" visible="true" version="2" changeset="98040654" timestamp="2021-01-24T00:40:07Z" user="raspberrycoffee" uid="11015901" lat="30.2729731" lon="-97.6994906"/>
             <node id="2933427978" visible="true" version="2" changeset="98040654" timestamp="2021-01-24T00:40:07Z" user="raspberrycoffee" uid="11015901" lat="30.2730689" lon="-97.7000448"/>
             <node id="2933427979" visible="true" version="2" changeset="98040654" timestamp="2021-01-24T00:40:07Z" user="raspberrycoffee" uid="11015901" lat="30.2730989" lon="-97.6996150"/>
             <node id="2933427980" visible="true" version="2" changeset="98040654" timestamp="2021-01-24T00:40:07Z" user="raspberrycoffee" uid="11015901" lat="30.2731153" lon="-97.6995259"/>
             <node id="2933427981" visible="true" version="2" changeset="98040654" timestamp="2021-01-24T00:40:07Z" user="raspberrycoffee" uid="11015901" lat="30.2731461" lon="-97.6996267"/>
             <node id="3825417633" visible="true" version="1" changeset="35179588" timestamp="2015-11-08T21:33:05Z" user="johnclary_axtbuildings" uid="3371348" lat="30.2727350" lon="-97.6998780">
              <tag k="addr:housenumber" v="3101"/>
              <tag k="addr:street" v="Oak Springs Drive"/>
              <tag k="coa:place_id" v="860234"/>
             </node>
             <node id="11711527457" visible="true" version="1" changeset="148470942" timestamp="2024-03-10T16:43:35Z" user="kaleidoscopica" uid="8704810" lat="30.2732855" lon="-97.6992140"/>
             <node id="11711578081" visible="true" version="1" changeset="148470942" timestamp="2024-03-10T16:43:35Z" user="kaleidoscopica" uid="8704810" lat="30.2728879" lon="-97.6991708">
              <tag k="highway" v="crossing"/>
             </node>
             <node id="11711578108" visible="true" version="1" changeset="148470942" timestamp="2024-03-10T16:43:35Z" user="kaleidoscopica" uid="8704810" lat="30.2730898" lon="-97.7005559"/>
             <node id="11711578117" visible="true" version="1" changeset="148470942" timestamp="2024-03-10T16:43:35Z" user="kaleidoscopica" uid="8704810" lat="30.2731322" lon="-97.7000780"/>
             <node id="11711578118" visible="true" version="1" changeset="148470942" timestamp="2024-03-10T16:43:35Z" user="kaleidoscopica" uid="8704810" lat="30.2730974" lon="-97.7003222"/>
             <node id="11711578119" visible="true" version="1" changeset="148470942" timestamp="2024-03-10T16:43:35Z" user="kaleidoscopica" uid="8704810" lat="30.2730991" lon="-97.7003970"/>
             <node id="11711578120" visible="true" version="1" changeset="148470942" timestamp="2024-03-10T16:43:35Z" user="kaleidoscopica" uid="8704810" lat="30.2730991" lon="-97.7004353"/>
             <node id="11711578121" visible="true" version="1" changeset="148470942" timestamp="2024-03-10T16:43:35Z" user="kaleidoscopica" uid="8704810" lat="30.2730855" lon="-97.7005131"/>
             <node id="11711578122" visible="true" version="1" changeset="148470942" timestamp="2024-03-10T16:43:35Z" user="kaleidoscopica" uid="8704810" lat="30.2731040" lon="-97.7002756">
              <tag k="highway" v="crossing"/>
             </node>
             <node id="11711578170" visible="true" version="1" changeset="148470942" timestamp="2024-03-10T16:43:35Z" user="kaleidoscopica" uid="8704810" lat="30.2731993" lon="-97.6996998"/>
             <way id="289801716" visible="true" version="2" changeset="111553539" timestamp="2021-09-22T17:11:17Z" user="jkers" uid="13596383">
              <nd ref="2933410034"/>
              <nd ref="2933410032"/>
              <tag k="highway" v="service"/>
              <tag k="service" v="parking_aisle"/>
              <tag k="surface" v="asphalt"/>
             </way>
             <way id="289801717" visible="true" version="3" changeset="148470942" timestamp="2024-03-10T16:43:35Z" user="kaleidoscopica" uid="8704810">
              <nd ref="2933410036"/>
              <nd ref="11711578122"/>
              <nd ref="2933410032"/>
              <nd ref="2933410028"/>
              <nd ref="2933410031"/>
              <nd ref="2933410034"/>
              <nd ref="11711578081"/>
              <nd ref="2933410035"/>
              <tag k="highway" v="service"/>
              <tag k="service" v="parking_aisle"/>
              <tag k="surface" v="asphalt"/>
             </way>
             <way id="289801718" visible="true" version="1" changeset="23165723" timestamp="2014-06-25T20:40:24Z" user="azerial" uid="1933664">
              <nd ref="2933410030"/>
              <nd ref="2933410033"/>
              <nd ref="2933410029"/>
              <nd ref="2933410027"/>
              <nd ref="2933410030"/>
              <tag k="access" v="yes"/>
              <tag k="amenity" v="parking"/>
              <tag k="capacity:disabled" v="2"/>
              <tag k="fee" v="no"/>
              <tag k="lit" v="yes"/>
              <tag k="parking" v="surface"/>
              <tag k="supervised" v="no"/>
              <tag k="surface" v="asphalt"/>
             </way>
             <way id="289804782" visible="true" version="7" changeset="151062610" timestamp="2024-05-08T15:32:42Z" user="Mateusz Konieczny - bot account" uid="3199858">
              <nd ref="2933427978"/>
              <nd ref="2933427974"/>
              <nd ref="2933427975"/>
              <nd ref="2933427972"/>
              <nd ref="2933427973"/>
              <nd ref="2933427976"/>
              <nd ref="2933427977"/>
              <nd ref="2933427980"/>
              <nd ref="2933427979"/>
              <nd ref="2933427981"/>
              <nd ref="2933427978"/>
              <tag k="addr:city" v="Austin"/>
              <tag k="addr:housenumber" v="3101"/>
              <tag k="addr:postcode" v="78702"/>
              <tag k="addr:state" v="TX"/>
              <tag k="addr:street" v="Oak Springs Drive"/>
              <tag k="amenity" v="library"/>
              <tag k="building" v="yes"/>
              <tag k="ele" v="144"/>
              <tag k="name" v="Austin Public Library - Willie Mae Kirk Branch"/>
              <tag k="opening_hours" v="Mo,Tu 12:00-20:00; We-Fr 10:00-18:00"/>
              <tag k="operator" v="Austin Public Libarary"/>
              <tag k="operator:type" v="public"/>
              <tag k="phone" v="+1 (512) 974-9920"/>
              <tag k="wheelchair" v="yes"/>
              <tag k="wikidata" v="Q69969533"/>
             </way>
             <way id="1259737750" visible="true" version="1" changeset="148470942" timestamp="2024-03-10T16:43:35Z" user="kaleidoscopica" uid="8704810">
              <nd ref="11711527457"/>
              <nd ref="11711578170"/>
              <nd ref="11711578117"/>
              <nd ref="11711578122"/>
              <nd ref="11711578118"/>
              <nd ref="11711578119"/>
              <nd ref="11711578120"/>
              <nd ref="11711578121"/>
              <nd ref="11711578108"/>
              <tag k="bicycle" v="designated"/>
              <tag k="foot" v="designated"/>
              <tag k="highway" v="cycleway"/>
             </way>
            </osm>
            """;

    Document validDoc;
    OSM validOSM;

    public OSMTest() throws Exception {
        validDoc = loadXMLFromString(VALID_XML);
        validOSM = OSM.fromXML(validDoc);
    }

    private static Document loadXMLFromString(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }

    @Test
    public void testConstructor() {
        BoundingBox<WGS84Coordinate> boundingBox = new BoundingBox<>(new WGS84Coordinate(56.6, 11.1), new WGS84Coordinate(11.5, -6.6));
        OSM osm = new OSM(boundingBox);
        assertAll(
                () -> assertEquals(boundingBox, osm.getBoundingBox(), "boundingBox should be initialized"),
                () -> assertInstanceOf(List.class, osm.getNodes(), "nodes should be initialized"),
                () -> assertInstanceOf(List.class, osm.getWays(), "ways should be initialized"),
                () -> assertNull(osm.getNodeBoundingBox(), "nodeBoundingBox should be initialized to null")
        );
    }

    @Test
    public void testFromXMLValidBoundingBox() {
        BoundingBox<WGS84Coordinate> boundingBox = validOSM.getBoundingBox();

        assertAll(
                () -> assertEquals(30.2727120, boundingBox.getMinLat(), 0.0001, "minimum latitude should be parsed correctly"),
                () -> assertEquals(-97.7001980, boundingBox.getMinLon(), 0.0001, "minimum longitude should be parsed correctly"),
                () -> assertEquals(30.2731710, boundingBox.getMaxLat(), 0.0001, "maximum latitude should be parsed correctly"),
                () -> assertEquals(-97.6992270, boundingBox.getMaxLon(), 0.0001, "maximum longitude should be parsed correctly")
        );
    }

    @Test
    public void testFromXMLValidNodes() {
        assertAll(
                () -> assertEquals(31, validOSM.getNodes().size(), "all nodes should be added to `nodes`"),
                () -> assertEquals(2933410027L, validOSM.getNodes().get(0).getId(), "nodes should remain in order"),
                () -> assertEquals(2933410032L, validOSM.getNodes().get(5).getId(), "nodes should remain in order"),
                () -> assertEquals(11711578170L, validOSM.getNodes().get(30).getId(), "nodes should remain in order")
        );
    }

    @Test
    public void testFromXMLValidWays() {
        assertAll(
                () -> assertEquals(5, validOSM.getWays().size(), "all ways should be added to `ways`"),
                () -> assertEquals(289801716L, validOSM.getWays().get(0).getId(), "ways should remain in order"),
                () -> assertEquals(289804782L, validOSM.getWays().get(3).getId(), "ways should remain in order"),
                () -> assertEquals(1259737750L, validOSM.getWays().get(4).getId(), "ways should remain in order")
        );
    }

    @Test
    public void testFromXMLValidNodeBoundingBox() {
        BoundingBox<WGS84Coordinate> bb = validOSM.getNodeBoundingBox();
        assertAll(
                () -> assertEquals(-97.7005559, bb.getMinLon(), 0.0001, "nodeBoundingBox minimum lon should be adjusted"),
                () -> assertEquals(-97.6990835, bb.getMaxLon(), 0.0001, "nodeBoundingBox maximum lon should be adjusted"),
                () -> assertEquals(30.272537, bb.getMinLat(), 0.0001, "nodeBoundingBox minimum lat should be adjusted"),
                () -> assertEquals(30.2732855, bb.getMaxLat(), 0.0001, "nodeBoundingBox maximum lat should be adjusted")
        );
    }

    @Test
    public void testFromXMLMissingBound() throws Exception {
        String[] attrs = {"minlat", "minlon", "maxlat", "maxlon"};
        for (String attr : attrs) {
            Document doc = loadXMLFromString(VALID_XML);
            doc.getElementsByTagName("bounds").item(0).getAttributes().removeNamedItem("minlon");
            assertThrows(UserInputException.class, () -> OSM.fromXML(doc), "missing required attribute " + attr + " should cause an error");
        }
    }

    @Test
    public void testGetNodesReturnsUnmodifiableList() {
        assertThrows(UnsupportedOperationException.class, () -> validOSM.getNodes().add(null), "getNodes should return an unmodifiable list");
    }

    @Test
    public void testGetNodeByIdValid() {
        long id = 2933427979L;
        assertEquals(id, validOSM.getNodeById(id).getId(), "getNodeById should find a node by id when it exists");
    }

    @Test
    public void testGetNodeByIdInvalid() {
        long id = 12345678L;
        assertNull(validOSM.getNodeById(id));
    }

    @Test
    public void testAddNode() throws Exception {
        OSM osm = OSM.fromXML(loadXMLFromString(VALID_XML));
        Node newNode = new Node(12345L, new WGS84Coordinate(-98, -33.2), true);
        osm.addNode(newNode);
        assertAll(
                () -> assertEquals(newNode, osm.getNodeById(newNode.getId()), "addNode should add the node itself, rather than a copy"),
                () -> assertEquals(-98, osm.getNodeBoundingBox().getMinLon(), "addNode should expand the node bounding box as necessary")
        );
    }

    @Test
    public void testRemoveNodeByIdNoBoundAdjustment() throws Exception {
        OSM osm = OSM.fromXML(loadXMLFromString(VALID_XML));
        long id = 2933427973L;
        osm.removeNodeById(id);
        assertAll(
                () -> assertNull(osm.getNodeById(id), "removeNodeById should remove the node from `nodes`"),
                () -> assertTrue(osm.getNodeBoundingBox().equals(validOSM.getNodeBoundingBox()),
                        "removeNodeById should not affect nodeBoundingBox for an internal node")
        );
    }

    @Test
    public void testRemoveNodeByIdWithBoundAdjustment() throws Exception {
        OSM osm = OSM.fromXML(loadXMLFromString(VALID_XML));
        long id = 2933410035L;
        osm.removeNodeById(id);
        assertAll(
                () -> assertNull(osm.getNodeById(id), "removeNodeById should remove the node from `nodes`"),
                () -> assertEquals(-97.699214, osm.getNodeBoundingBox().getMaxLon(), 0.0001,
                        "removeNodeById should automatically adjust the nodeBoundingBox for an edge node")
        );
    }

    @Test
    public void testRemoveNodeByIdWithBoundAdjustmentExplicitlyDisabled() throws Exception {
        OSM osm = OSM.fromXML(loadXMLFromString(VALID_XML));
        long id = 2933410027L;
        osm.removeNodeById(id, false);
        assertAll(
                () -> assertNull(osm.getNodeById(id), "removeNodeById should remove the node from `nodes`"),
                () -> assertTrue(validOSM.getNodeBoundingBox().equals(osm.getNodeBoundingBox()),
                        "removeNodeById should not shrink the nodeBoundingBox if the behavior is explicitly disabled")
        );
    }

    @Test
    public void testRemoveNodeByIdWithInvalidId() {
        int expectedNodeCount = validOSM.getNodes().size();
        validOSM.removeNodeById(99999L);
        assertEquals(expectedNodeCount, validOSM.getNodes().size(), "removeNodeById should do nothing if the node to remove doesn't exist");
    }

    @Test
    public void testClearNodes() throws Exception {
        OSM osm = OSM.fromXML(loadXMLFromString(VALID_XML));
        osm.clearNodes();
        assertAll(
                () -> assertTrue(osm.getNodes().isEmpty(), "clearNodes should clear the node list"),
                () -> assertNull(osm.getNodeBoundingBox(), "clearNodes should delete the nodeBoundingBox")
        );
    }

    @Test
    public void testGetWaysReturnsUnmodifiableList() {
        assertThrows(UnsupportedOperationException.class, () -> validOSM.getWays().add(null), "getWays should return an unmodifiable list");
    }

    @Test
    public void testGetWayByIdValid() {
        long id = 289801718L;
        assertEquals(id, validOSM.getWayById(id).getId(), "getWayById should find a way by id when it exists");
    }

    @Test
    public void testGetWayByIdInvalid() {
        long id = 12345678L;
        assertNull(validOSM.getWayById(id), "getWayById should return null with an invalid id");
    }

    @Test
    public void testAddWay() throws Exception {
        OSM osm = OSM.fromXML(loadXMLFromString(VALID_XML));
        Way newWay = new Way(12345, true);
        osm.addWay(newWay);
        assertEquals(newWay, osm.getWayById(newWay.getId()), "addWay should add the way itself, rather than a copy");
    }

    @Test
    public void testRemoveWayById() throws Exception {
        OSM osm = OSM.fromXML(loadXMLFromString(VALID_XML));
        long id = 1259737750L;
        osm.removeWayById(id);
        assertNull(osm.getWayById(id), "removeWayById should remove the requested way");
    }

    @Test
    public void testRemoveWayByIdWithInvalidId() {
        int expectedWayCount = validOSM.getWays().size();
        validOSM.removeWayById(99999L);
        assertEquals(expectedWayCount, validOSM.getWays().size(), "removeWayById should do nothing if the way to remove doesn't exist");
    }

    @Test
    public void testClearWays() throws Exception {
        OSM osm = OSM.fromXML(loadXMLFromString(VALID_XML));
        osm.clearWays();
        assertTrue(osm.getWays().isEmpty(), "clearWays should clear the way list");
    }
}
