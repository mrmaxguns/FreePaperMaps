package io.github.mrmaxguns.freepapermaps.osm;

import io.github.mrmaxguns.freepapermaps.UserInputException;
import io.github.mrmaxguns.freepapermaps.XMLTools;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

/** Represents an OSM way: an ordered list of Node IDs. */
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
    public static Way fromXML(org.w3c.dom.Node rawNode) throws UserInputException {
        XMLTools xmlTools = new XMLTools();

        long id = xmlTools.getAttributeValueLong(rawNode, "id");

        boolean visible = true;
        String visibleRaw = xmlTools.getOptionalAttributeValue(rawNode, "visible");
        if (visibleRaw != null) {
            visible = Boolean.parseBoolean(visibleRaw);
        }

        Way newWay = new Way(id, visible);

        // A way's children are usually both node references and tags. To speed up the process, we will collect both
        // nodes and tags in one iteration rather than calling TagList's insertFromXML.
        NodeList children = rawNode.getChildNodes();
        for (int i = 0; i < children.getLength(); ++i) {
            org.w3c.dom.Node child = children.item(i);
            if (child.getNodeName().equals("nd")) {
                // Parse node references
                newWay.getNodeIds().add(xmlTools.getAttributeValueLong(child, "ref"));
            } else if (child.getNodeName().equals("tag")) {
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

    public ArrayList<Long> getNodeIds() {
        return nodeIds;
    }

    public TagList getTags() {
        return tags;
    }
}
