package io.github.mrmaxguns.freepapermaps.osm;

import io.github.mrmaxguns.freepapermaps.UserInputException;
import io.github.mrmaxguns.freepapermaps.XMLTools;
import io.github.mrmaxguns.freepapermaps.projections.WGS84Coordinate;
import org.w3c.dom.NodeList;

import java.util.Objects;


/** Represents an OSM node, which is a point in space that optionally has tags. */
public class Node {
    /** The Node's unique identifier in the database. */
    private long id;
    /** The Node's position in the world. Never null. */
    private WGS84Coordinate position;
    /** Whether the Node should be rendered. */
    private boolean visible;
    /** A list of tags associated with this Node. */
    private final TagList tags;

    /** Constructs a new Node. */
    public Node(long id, WGS84Coordinate position, boolean visible) {
        this.id = id;
        this.position = Objects.requireNonNull(position);
        this.visible = visible;
        this.tags = new TagList();
    }

    /** Constructs a Node from an org.w3c.dom XML Node. */
    public static Node fromXML(org.w3c.dom.Node rawNode) throws UserInputException {
        XMLTools xmlTools = new XMLTools();

        long id = xmlTools.getAttributeValueLong(rawNode, "id");

        double lon = xmlTools.getAttributeValueDouble(rawNode, "lon");
        double lat = xmlTools.getAttributeValueDouble(rawNode, "lat");
        WGS84Coordinate position = new WGS84Coordinate(lon, lat);

        boolean visible = true;
        String visibleRaw = xmlTools.getOptionalAttributeValue(rawNode, "visible");
        if (visibleRaw != null) {
            visible = Boolean.parseBoolean(visibleRaw);
        }

        Node newNode = new Node(id, position, visible);

        NodeList tags = rawNode.getChildNodes();
        newNode.getTags().insertFromXML(tags);

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
        this.position = position;
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
