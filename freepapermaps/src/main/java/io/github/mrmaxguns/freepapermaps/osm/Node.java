package io.github.mrmaxguns.freepapermaps.osm;

import io.github.mrmaxguns.freepapermaps.UserInputException;
import io.github.mrmaxguns.freepapermaps.XMLTools;
import io.github.mrmaxguns.freepapermaps.projections.WGS84Coordinate;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Objects;


/** Represents an OSM node, which is a point in space that optionally has tags. */
public class Node {
    /** A list of tags associated with this Node. */
    private final TagList tags;
    /** The <code>Node</code>'s unique identifier in the database. */
    private long id;
    /** The <code>Node</code>'s position in the world. Never <code>null</code>. */
    private WGS84Coordinate position;
    /** Whether the <code>Node</code> should be rendered. */
    private boolean visible;

    /** Constructs a new Node. */
    public Node(long id, WGS84Coordinate position, boolean visible) {
        this.id = id;
        this.position = Objects.requireNonNull(position);
        this.visible = visible;
        this.tags = new TagList();
    }

    /** Constructs a Node from an org.w3c.dom XML Element. */
    public static Node fromXML(Element rawNode) throws UserInputException {
        return fromXML(rawNode, new XMLTools());
    }

    /**
     * Constructs a Node from an org.w3c.dom XML Element, with an XML context provided by an <code>XMLTools</code>
     * object.
     */
    public static Node fromXML(Element rawNode, XMLTools xmlTools) throws UserInputException {
        long id = xmlTools.getAttributeValueLong(rawNode, "id");

        // Get position
        double lon = xmlTools.getAttributeValueDouble(rawNode, "lon");
        double lat = xmlTools.getAttributeValueDouble(rawNode, "lat");
        WGS84Coordinate position = new WGS84Coordinate(lon, lat);

        // Get optional attribute visible
        boolean visible = true;
        String visibleRaw = xmlTools.getAttributeValue(rawNode, "visible", false);
        if (visibleRaw != null) {
            visible = Boolean.parseBoolean(visibleRaw);
        }

        Node newNode = new Node(id, position, visible);

        NodeList tags = rawNode.getChildNodes();
        newNode.getTags().insertFromXML(tags, xmlTools);

        return newNode;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public WGS84Coordinate getPosition() {
        return position;
    }

    public void setPosition(WGS84Coordinate position) {
        this.position = Objects.requireNonNull(position);
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public TagList getTags() {
        return tags;
    }
}
