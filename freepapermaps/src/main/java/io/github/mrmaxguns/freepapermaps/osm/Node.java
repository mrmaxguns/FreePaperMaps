package io.github.mrmaxguns.freepapermaps.osm;

import io.github.mrmaxguns.freepapermaps.UserInputException;
import io.github.mrmaxguns.freepapermaps.XMLTools;
import io.github.mrmaxguns.freepapermaps.projections.WGS84Coordinate;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Objects;


/** Represents an OSM node, which is a point in space that optionally has tags. */
public class Node extends OSMElement {
    private WGS84Coordinate position;

    /** Constructs a new Node. */
    public Node(long id, WGS84Coordinate position, boolean visible) {
        super(id, visible);
        this.position = Objects.requireNonNull(position);
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
        long id = getIdFromXML(rawNode, xmlTools);
        boolean visible = getVisibleFromXML(rawNode, xmlTools);

        // Get position
        double lon = xmlTools.getAttributeValueDouble(rawNode, "lon");
        double lat = xmlTools.getAttributeValueDouble(rawNode, "lat");
        WGS84Coordinate position = new WGS84Coordinate(lon, lat);

        Node newNode = new Node(id, position, visible);

        NodeList tags = rawNode.getChildNodes();
        newNode.getTags().insertFromXML(tags, xmlTools);

        return newNode;
    }

    public WGS84Coordinate getPosition() {
        return position;
    }

    public void setPosition(WGS84Coordinate position) {
        this.position = Objects.requireNonNull(position);
    }
}
