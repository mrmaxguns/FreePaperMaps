package io.github.mrmaxguns.freepapermaps.osm;

import io.github.mrmaxguns.freepapermaps.UserInputException;
import io.github.mrmaxguns.freepapermaps.projections.WGS84Coordinate;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

import static io.github.mrmaxguns.freepapermaps.TestingUtility.loadXMLElementFromString;
import static org.junit.jupiter.api.Assertions.*;

public class NodeTest {
    public final static String VALID_XML = """
            <node id="8859934316" visible="true" version="1" changeset="106835416" timestamp="2021-06-23T11:41:13Z"
                    user="G_P_J" uid="6751278" lat="54.5382081" lon="18.4608362">
                <tag k="addr:city" v="Gdynia"/>
                <tag k="addr:city:simc" v="0934100"/>
                <tag k="addr:housenumber" v="19"/>
                <tag k="addr:postcode" v="81-057"/>
                <tag k="addr:street" v="Raduńska"/>
                <tag k="source:addr" v="gugik.gov.pl"/>
            </node>
            """;

    private final Node validNode;

    public NodeTest() throws Exception {
        Element validXMLNode = loadXMLElementFromString(VALID_XML);
        validNode = Node.fromXML(validXMLNode);
    }

    @Test
    public void testConstructor() {
        long id = 68745432L;
        WGS84Coordinate position = new WGS84Coordinate(-74.9377, 43.9216);
        boolean visible = true;
        Node n = new Node(id, position, visible);

        assertAll(
                () -> assertEquals(id, n.getId(), "constructor should initialize id"),
                () -> assertEquals(position, n.getPosition(), "constructor should initialize position"),
                () -> assertEquals(visible, n.isVisible(), "constructor should initialize visible"),
                () -> assertTrue(n.getTags().isEmpty(), "constructor should initialize tags to ge an empty TagList")
        );
    }

    @Test
    public void testFromXMLValidProperties() {
        assertAll(
                () -> assertEquals(8859934316L, validNode.getId(), "id should be parsed correctly"),
                () -> assertEquals(18.4608362, validNode.getPosition().getLon(), 0.0001,
                        "longitude should be parsed correctly"),
                () -> assertEquals(54.5382081, validNode.getPosition().getLat(), 0.0001,
                        "latitude should be parsed correctly"),
                () -> assertTrue(validNode.isVisible(), "visibility should be parsed correctly")
        );
    }

    @Test
    public void testFromXMLValidTags() {
        TagList tags = validNode.getTags();
        assertAll(
                () -> assertEquals(6, tags.size(), "all tags should be parsed"),
                () -> assertEquals("19", validNode.getTags().get("addr:housenumber"), "tags should be parsed correctly")
        );
    }

    @Test
    public void testFromXMLMissingRequiredAttribute() throws Exception {
        String[] attrs = { "id", "lon", "lat" };
        for (String attr : attrs) {
            Element node = loadXMLElementFromString(VALID_XML);
            node.getAttributes().removeNamedItem(attr);
            assertThrows(UserInputException.class, () -> Node.fromXML(node),
                    "missing required attribute " + attr + " should cause an error");
        }
    }

    @Test
    public void testFromXMLMissingOptionalAttribute() throws Exception {
        Element node = loadXMLElementFromString(VALID_XML);
        node.getAttributes().removeNamedItem("visible");
        Node result = Node.fromXML(node);
        assertTrue(result.isVisible(), "a node without optional visibility attribute should default to visible");
    }
}
