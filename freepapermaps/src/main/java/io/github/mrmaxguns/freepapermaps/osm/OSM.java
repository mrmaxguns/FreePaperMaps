package io.github.mrmaxguns.freepapermaps.osm;

import io.github.mrmaxguns.freepapermaps.UserInputException;
import io.github.mrmaxguns.freepapermaps.XMLTools;
import io.github.mrmaxguns.freepapermaps.projections.BoundingBox;
import io.github.mrmaxguns.freepapermaps.projections.WGS84Coordinate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


/**
 * <code>OSM</code> is a representation of the OpenStreetMap data format. There are three fundamental building blocks
 * for geographic data in this format:
 * <ul>
 *     <li><em>Node:</em> a point associated with a position in space. See <a href="#{@link}>{@link Node}</a>.</li>
 *     <li><em>Way:</em> an ordered list of nodes. See <a href="#{@link}>{@link Way}</a>.</li>
 *     <li><em>Relation:</em> an ordered list of nodes, ways, and other relations. (Not implemented yet.)</li>
 * </ul>
 * See also <a href="https://wiki.openstreetmap.org/wiki/Elements">the OSM wiki</a>. Currently, only nodes and ways
 * are supported.
 * <p>
 * All three elements can also have tags (see <a href="#{@link}>{@link TagList}</a>), which are key/value pairs
 * describing the element in question.
 * <p>
 * This class represents a complete region of OpenStreetMap data. It provides utilities for adding and modifying the
 * data. Additionally, it provides utilities for parsing OSM data from an OSM XML file.
 */
public class OSM {
    /** A list of nodes, where keys are node ids for fast access. */
    private final HashMap<Long, Node> nodes;
    /** A list of ways, where keys are way ids for fast access. */
    private final HashMap<Long, Way> ways;
    /** A bounding box defining the geographic area to be rendered. Can be null. */
    private BoundingBox<WGS84Coordinate> boundingBox;
    /**
     * A bounding box defining an area containing all nodes. When exporting a region from OpenStreetMap, the region's
     * size will be given by <code>boundingBox</code>, but in reality, there will be nodes outside that area if any
     * geometry was "cut" by the region.
     * <p>
     * Invariant: nodeBoundingBox is always at least large enough to display all data nodes, and is null when there are
     * no nodes.
     */
    private BoundingBox<WGS84Coordinate> nodeBoundingBox;

    /** Constructs an OSM object with a <code>null</code> <code>boundingBox</code>. */
    public OSM() {
        this(null);
    }

    /**
     * Constructs an OSM object.
     *
     * @param boundingBox a geographic region representing the rendering extent (typically given by minlat, minlon,
     *                    maxlat, and maxlon in an OSM XML file). Can be null.
     */
    public OSM(BoundingBox<WGS84Coordinate> boundingBox) {
        this.boundingBox = boundingBox;
        this.nodes = new HashMap<>();
        this.ways = new HashMap<>();
        this.nodeBoundingBox = null;
    }

    /** Constructs an OSM object from an OSM XML file. */
    public static OSM fromXML(Document doc) throws UserInputException {
        return fromXML(doc, new XMLTools());
    }

    /**
     * Constructs an OSM object from an OSM XML file, with the addition of an <code>XMLTools</code> object to provide
     * useful context for user error messages.
     */
    public static OSM fromXML(Document doc, XMLTools xmlTools) throws UserInputException {
        // Get map bounds
        Element bounds = xmlTools.getSingleChildElementByTagName(doc, "bounds", false);
        BoundingBox<WGS84Coordinate> boundingBox;

        if (bounds != null) {
            double minLat = xmlTools.getRequiredAttributeValueDouble(bounds, "minlat");
            double minLon = xmlTools.getRequiredAttributeValueDouble(bounds, "minlon");
            double maxLat = xmlTools.getRequiredAttributeValueDouble(bounds, "maxlat");
            double maxLon = xmlTools.getRequiredAttributeValueDouble(bounds, "maxlon");
            boundingBox = new BoundingBox<>(new WGS84Coordinate(minLon, maxLat), new WGS84Coordinate(maxLon, minLat));
        } else {
            boundingBox = null;
        }

        // Create the OSM object
        OSM newOSM = new OSM(boundingBox);

        // Get all nodes
        NodeList rawNodes = doc.getElementsByTagName("node");
        for (int i = 0; i < rawNodes.getLength(); ++i) {
            if (rawNodes.item(i).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                newOSM.addNode(Node.fromXML((Element) rawNodes.item(i)));
            }
        }

        // Get all ways
        NodeList rawWays = doc.getElementsByTagName("way");
        for (int i = 0; i < rawWays.getLength(); ++i) {
            if (rawWays.item(i).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                newOSM.addWay(Way.fromXML((Element) rawWays.item(i)));
            }
        }

        return newOSM;
    }

    public BoundingBox<WGS84Coordinate> getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(BoundingBox<WGS84Coordinate> boundingBox) {
        this.boundingBox = boundingBox;
    }

    /** Returns a non-modifiable list of <code>Node</code>s. */
    public List<Node> getNodes() {
        return List.copyOf(nodes.values());
    }

    /** Returns a <code>Node</code> given an id, or <code>null</code> if no such <code>Node</code> exists. */
    public Node getNodeById(long id) {
        return nodes.get(id);
    }

    /**
     * Adds a new <code>Node</code> to the list of <code>Node</code>s. The <code>Node</code> cannot be
     * <code>null</code>.
     */
    public void addNode(Node newNode) {
        // Add the node
        nodes.put(newNode.getId(), Objects.requireNonNull(newNode));

        // Adjust bounds accordingly
        adjustBoundsIfNecessary(newNode);
    }

    /**
     * Removes a <code>Node</code> by id, or does nothing if it doesn't exist.
     *
     * @param id           the id of the <code>Node</code> to remove
     * @param adjustBounds Whether to shrink the <code>nodeBoundingBox</code> if the removal of the node requires it.
     *                     This is an O(n) process where n is the number of nodes, so for multiple removals in a row,
     *                     it is best to set this to <code>false</code> and then call
     *                     <a href="#{@link}>{@link #adjustNodeBounds()}</a> at the end.
     */
    public void removeNodeById(long id, boolean adjustBounds) {
        nodes.remove(id);

        if (adjustBounds) {
            adjustNodeBounds();
        }
    }

    /**
     * Removes a <code>Node</code> by id, or does nothing if it doesn't exist. Automatically adjusts the
     * <code>nodeBoundingBox</code> as necessary.
     */
    public void removeNodeById(long id) {
        removeNodeById(id, true);
    }

    /** Clears the list of <code>Node</code>s. */
    public void clearNodes() {
        nodes.clear();
        nodeBoundingBox = null;
    }

    /** Returns a non-modifiable list of <code>Way</code>s. */
    public List<Way> getWays() {
        return List.copyOf(ways.values());
    }

    /** Returns a <code>Way</code> given an id, or null if it doesn't exist. */
    public Way getWayById(long id) {
        return ways.get(id);
    }

    /**
     * Adds a new <code>Way</code> to the list of <code>Way</code>s. The <code>Way</code> cannot be
     * <code>null</code>.
     */
    public void addWay(Way newWay) {
        ways.put(newWay.getId(), Objects.requireNonNull(newWay));
    }

    /** Removes a <code>Way</code> by id, or does nothing if it doesn't exist. */
    public void removeWayById(long id) {
        ways.remove(id);
    }

    /** Clears the list of <code>Way</code>s. */
    public void clearWays() {
        ways.clear();
    }

    public BoundingBox<WGS84Coordinate> getNodeBoundingBox() {
        return nodeBoundingBox;
    }

    /** Shrinks the <code>nodeBoundingBox</code> as necessary to fully contain all <code>Node</code>s exactly. */
    public void adjustNodeBounds() {
        nodeBoundingBox = null;
        for (Node n : nodes.values()) {
            adjustBoundsIfNecessary(n);
        }
    }

    /**
     * Given a <code>Way</code> id, creates a list of all <code>Node</code>s in the <code>Way</code>.
     *
     * @return the list of <code>Nodes</code> or <code>null</code> if such a <code>Way</code> doesn't exist
     */
    public List<Node> getNodesInWay(long wayId) throws UserInputException {
        Way way = getWayById(wayId);

        if (way == null) {
            return null;
        }

        ArrayList<Node> nodes = new ArrayList<>(way.getNodeIds().size());
        for (long nodeId : way.getNodeIds()) {
            Node n = getNodeById(nodeId);

            if (n == null) {
                throw new UserInputException(
                        "Way with id " + wayId + " references node with id " + nodeId + " that doesn't exist.");
            }

            nodes.add(n);
        }

        return nodes;
    }

    /**
     * If <code>Node</code> <code>n</code> is outside the current <code>nodeBoundingBox</code>, this function expands
     * the bounding box to contain <code>n</code>.
     */
    private void adjustBoundsIfNecessary(Node n) {
        boolean boundsChanged = false;
        WGS84Coordinate position = n.getPosition();

        // Make the bounding box the size of the current node, given that this is the first node we're inserting.
        if (nodeBoundingBox == null) {
            nodeBoundingBox = new BoundingBox<>(position, position);
            return;
        }

        double minLon = nodeBoundingBox.getMinLon();
        double maxLon = nodeBoundingBox.getMaxLon();
        double minLat = nodeBoundingBox.getMinLat();
        double maxLat = nodeBoundingBox.getMaxLat();

        if (position.getLon() < minLon) {
            minLon = position.getLon();
            boundsChanged = true;
        } else if (position.getLon() > maxLon) {
            maxLon = position.getLon();
            boundsChanged = true;
        }

        if (position.getLat() < minLat) {
            minLat = position.getLat();
            boundsChanged = true;
        } else if (position.getLat() > maxLat) {
            maxLat = position.getLat();
            boundsChanged = true;
        }

        if (boundsChanged) {
            nodeBoundingBox = new BoundingBox<>(new WGS84Coordinate(minLon, maxLat),
                                                new WGS84Coordinate(maxLon, minLat));
        }
    }
}
