package io.github.mrmaxguns.freepapermaps.osm;

import io.github.mrmaxguns.freepapermaps.UserInputException;
import io.github.mrmaxguns.freepapermaps.XMLTools;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;


/**
 * Represents a collection of tags associated with a Node, Way, or Relation. Permits <code>null</code> keys and
 * values.
 */
public class TagList extends HashMap<String, String> {
    public TagList() {
        super();
    }

    /** Constructs a <code>TagList</code> with an initial capacity. */
    public TagList(int initialCapacity) {
        super(initialCapacity);
    }

    /** Constructs a <code>TagList</code> with an initial capacity and load factor. */
    public TagList(int initialCapacity, int loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /** Constructs a <code>TagList</code> from a shallow copy of another. */
    public TagList(TagList t) {
        super(t);
    }

    /** Constructs a <code>TagList</code> from an XML NodeList of tags. Non-tag elements are ignored. */
    public void insertFromXML(NodeList tags) throws UserInputException {
        insertFromXML(tags, new XMLTools());
    }

    /**
     * Constructs a <code>TagList</code> from an XML <code>NodeList</code> of tags and with a context from an
     * <code>XMLTools</code> object. Non-tag elements are ignored.
     */
    public void insertFromXML(NodeList tags, XMLTools xmlTools) throws UserInputException {
        for (int i = 0; i < tags.getLength(); ++i) {
            if (tags.item(i).getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element tag = (Element) tags.item(i);
            if (tag.getTagName().equals("tag")) {
                put(xmlTools.getAttributeValue(tag, "k"), xmlTools.getAttributeValue(tag, "v"));
            }
        }
    }
}
