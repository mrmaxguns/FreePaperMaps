package io.github.mrmaxguns.freepapermaps.osm;

import io.github.mrmaxguns.freepapermaps.UserInputException;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

import java.util.List;

import static io.github.mrmaxguns.freepapermaps.TestingUtility.loadXMLElementFromString;
import static org.junit.jupiter.api.Assertions.*;


public class WayTest {
    public static final String VALID_XML = """
            <way id="1347802008" visible="true" version="1" changeset="160878631" timestamp="2025-01-01T17:48:27Z" user="Marvin de Hont" uid="6616734">
                <nd ref="12467240751"/>
                <nd ref="12467240749"/>
                <nd ref="12467240759"/>
                <nd ref="12467240757"/>
                <nd ref="12467240756"/>
                <nd ref="12467240758"/>
                <nd ref="12467240761"/>
                <nd ref="12467240760"/>
                <nd ref="12467240750"/>
                <nd ref="12467240751"/>
                <tag k="building" v="yes"/>
            </way>
            """;

    private final Way validWay;

    public WayTest() throws Exception {
        Element validXMLNode = loadXMLElementFromString(VALID_XML);
        validWay = Way.fromXML(validXMLNode);
    }


    @Test
    public void testConstructor() {
        long id = 193831942L;
        boolean visible = true;
        Way w = new Way(id, visible);

        assertAll(() -> assertEquals(id, w.getId(), "constructor should initialize id"),
                  () -> assertEquals(visible, w.isVisible(), "constructor should initialize visible"),
                  () -> assertTrue(w.getNodeIds().isEmpty(),
                                   "constructor should initialize nodeIds to be an empty list"),
                  () -> assertTrue(w.getTags().isEmpty(), "constructor should initialize tags to ge an empty TagList"));
    }

    @Test
    public void testFromXMLValidProperties() {
        assertAll(() -> assertEquals(1347802008L, validWay.getId(), "id should be parsed correctly"),
                  () -> assertTrue(validWay.isVisible(), "visibility should be parsed correctly"));
    }

    @Test
    public void testFromXMLValidNodeIds() {
        List<Long> nodeIds = validWay.getNodeIds();
        assertAll(() -> assertEquals(10, nodeIds.size(), "all nodes should be parsed, including duplicates"),
                  () -> assertEquals(12467240751L, nodeIds.get(0), "nodeIds should be ordered based on the input"),
                  () -> assertEquals(12467240757L, nodeIds.get(3), "nodeIds should be ordered based on the input"),
                  () -> assertEquals(12467240751L, nodeIds.get(9), "nodeIds should be ordered based on the input"));
    }

    @Test
    public void testFromXMLValidTags() {
        TagList tags = validWay.getTags();
        assertAll(() -> assertEquals(1, tags.size(), "all tags should be parsed"),
                  () -> assertEquals("yes", validWay.getTags().get("building"), "tags should be parsed correctly"));
    }

    @Test
    public void testFromXMLMissingRequiredAttribute() throws Exception {
        Element node = loadXMLElementFromString(VALID_XML);
        node.getAttributes().removeNamedItem("id");
        assertThrows(UserInputException.class, () -> Node.fromXML(node),
                     "missing required attribute id should cause an error");
    }

    @Test
    public void testFromXMLMissingOptionalAttribute() throws Exception {
        Element node = loadXMLElementFromString(VALID_XML);
        node.getAttributes().removeNamedItem("visible");
        Way result = Way.fromXML(node);
        assertTrue(result.isVisible(), "a node without optional visibility attribute should default to visible");
    }
}
