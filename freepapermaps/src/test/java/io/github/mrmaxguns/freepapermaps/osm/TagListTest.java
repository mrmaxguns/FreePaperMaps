package io.github.mrmaxguns.freepapermaps.osm;

import io.github.mrmaxguns.freepapermaps.UserInputException;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

import static io.github.mrmaxguns.freepapermaps.TestingUtility.loadXMLElementFromString;
import static org.junit.jupiter.api.Assertions.*;


public class TagListTest {
    public final static String VALID_XML = """
            <way id="168831944" visible="true" version="15" changeset="169250186" timestamp="2025-07-21T15:18:09Z" user="NFZANMNIM" uid="1980310">
                <nd ref="8074057436"/>
                <nd ref="1800270154"/>
                <tag k="aeroway" v="aerodrome"/>
                <tag k="barrier" v="fence"/>
                <tag k="landuse" v="military"/>
                <tag k="military" v="airfield"/>
                <tag k="name" v="Налайх цэргийн нисэх буудал ᠨᠠᠯᠠᠶᠢᠬᠤ ᠴᠡᠷᠢᠭ ᠦᠨ ᠨᠢᠰᠬᠦ ᠪᠠᠭᠤᠳᠠᠯ"/>
                <nd ref="8639236675"/>
                <tag k="name:en" v="Nalaikh Military Airport"/>
                <tag k="name:mn" v="Налайх цэргийн нисэх буудал"/>
                <tag k="name:mn-Cyrl" v="Налайх цэргийн нисэх буудал"/>
                <tag k="name:mn-Mong" v="ᠨᠠᠯᠠᠶᠢᠬᠤ ᠴᠡᠷᠢᠭ ᠦᠨ ᠨᠢᠰᠬᠦ ᠪᠠᠭᠤᠳᠠᠯ"/>
                <tag k="name:zh" v="纳来哈机场"/>
                <tag k="wikidata" v="Q4312581"/>
                <tag k="wikipedia" v="ru:Налайх (аэродром)"/>
                <nd ref="8074057439"/>
            </way>
            """;

    private final TagList validTagList;

    public TagListTest() throws Exception {
        Element validXMLNode = loadXMLElementFromString(VALID_XML);
        validTagList = new TagList();
        validTagList.insertFromXML(validXMLNode.getChildNodes());
    }

    @Test
    public void testInsertFromXML() {
        assertAll(() -> assertEquals(12, validTagList.size(),
                                     "all tags are inserted, while non-tag elements are ignored"),
                  () -> assertEquals("airfield", validTagList.get("military"),
                                     "tags should be inserted maintaining correctness"),
                  () -> assertEquals("纳来哈机场", validTagList.get("name:zh"),
                                     "tags should be inserted maintaining correctness"));
    }

    @Test
    public void testInsertFromXMLMissingRequiredAttribute() throws Exception {
        String[] attrs = { "k", "v" };
        for (String attr : attrs) {
            Element node = loadXMLElementFromString(VALID_XML);
            node.getElementsByTagName("tag").item(2).getAttributes().removeNamedItem(attr);
            assertThrows(UserInputException.class, () -> validTagList.insertFromXML(node.getChildNodes()),
                         "missing required attribute " + attr + " should cause an error");
        }
    }
}
