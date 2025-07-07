package io.github.mrmaxguns.freepapermaps.osm;

import io.github.mrmaxguns.freepapermaps.UserInputException;
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
        long id;
        try {
            id = Long.parseLong(rawNode.getAttributes().getNamedItem("id").getNodeValue());
        } catch (NullPointerException e) {
            throw new UserInputException("Encountered a way without an id.");
        }

        boolean visible = true;
        try {
            visible = Boolean.parseBoolean(rawNode.getAttributes().getNamedItem("visible").getNodeValue());
        } catch (NullPointerException ignored) {}

        Way newWay = new Way(id, visible);

        // A way's children are usually both node references and tags. To speed up the process, we will collect both
        // nodes and tags in one iteration rather than calling TagList's insertFromXML.
        NodeList children = rawNode.getChildNodes();
        for (int i = 0; i < children.getLength(); ++i) {
            org.w3c.dom.Node child = children.item(i);
            if (child.getNodeName().equals("nd")) {
                // Parse node references
                try {
                    newWay.getNodeIds().add(Long.parseLong(child.getAttributes().getNamedItem("ref").getNodeValue()));
                } catch (NullPointerException e) {
                    throw new UserInputException("Found a 'nd' in way " + id + " without a 'ref' value.");
                }
            } else if (child.getNodeName().equals("tag")) {
                // Parse tags
                try {
                    newWay.getTags().put(
                            child.getAttributes().getNamedItem("k").getNodeValue(),
                            child.getAttributes().getNamedItem("v").getNodeValue()
                    );
                } catch (NullPointerException e) {
                    throw new UserInputException("Found a tag in way " + id + " missing either 'k' or 'v' (or both).");
                }
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
