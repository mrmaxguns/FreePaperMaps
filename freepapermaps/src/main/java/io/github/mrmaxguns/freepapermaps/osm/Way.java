package io.github.mrmaxguns.freepapermaps.osm;

import io.github.mrmaxguns.freepapermaps.UserInputException;
import io.github.mrmaxguns.freepapermaps.XMLTools;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/** Represents an OSM way, which is an ordered list of node ids. */
public class Way {
    /** The Way's unique identifier. */
    private long id;
    /** Whether the Way should be rendered. */
    private boolean visible;
    /** An ordered list of Node IDs associated with this Way. */
    private final ArrayList<Long> nodeIds;
    /** A collection of tags associated with this Way. */
    private final TagList tags;

    /** Constructs a Way. */
    public Way(long id, boolean visible) {
        this.id = id;
        this.visible = visible;
        this.nodeIds = new ArrayList<>();
        this.tags = new TagList();
    }

    /** Constructs a Way object from an org.w3c.dom XML Node. */
    public static Way fromXML(Element rawWay) throws UserInputException {
        return fromXML(rawWay, new XMLTools());
    }

    /**
     * Constructs a Way object from an org.w3c.dom XML Node, with an XML context provided by an <code>XMLTools</code>
     * object.
     */
    public static Way fromXML(Element rawWay, XMLTools xmlTools) throws UserInputException {
        long id = xmlTools.getAttributeValueLong(rawWay, "id");

        // Get optional visibility attribute
        boolean visible = true;
        String visibleRaw = xmlTools.getAttributeValue(rawWay, "visible", false);
        if (visibleRaw != null) {
            visible = Boolean.parseBoolean(visibleRaw);
        }

        Way newWay = new Way(id, visible);

        // A way's children are usually both node references and tags. To speed up the process, we will collect both
        // nodes and tags in one iteration rather than calling TagList's insertFromXML.
        NodeList children = rawWay.getChildNodes();
        for (int i = 0; i < children.getLength(); ++i) {
            if (children.item(i).getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element child = (Element) children.item(i);
            if (child.getTagName().equals("nd")) {
                // Parse node references
                newWay.addNodeId(xmlTools.getAttributeValueLong(child, "ref"));
            } else if (child.getTagName().equals("tag")) {
                // Parse tags
                newWay.getTags().put(
                        xmlTools.getAttributeValue(child, "k"),
                        xmlTools.getAttributeValue(child, "v"));
            }
        }

        return newWay;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /** Returns an unmodifiable list of <code>Node</code> ids. */
    public List<Long> getNodeIds() {
        return Collections.unmodifiableList(nodeIds);
    }

    /** Adds a <code>Node</code> id to the end of the list. */
    public void addNodeId(long id) {
        nodeIds.add(id);
    }

    /** Clears the list of <code>Node</code> ids. */
    public void clearNodeIds() {
        nodeIds.clear();
    }

    public TagList getTags() {
        return tags;
    }
}
