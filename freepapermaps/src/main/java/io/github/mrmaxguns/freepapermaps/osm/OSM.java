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


/**
 * OSM is a representation of the OpenStreetMap data format. There are three fundamental building blocks for geographic
 * data in this format:
 * <ul>
 *     <li><em>Node:</em> a point associated with a position in space</li>
 *     <li><em>Way:</em> an ordered list of nodes</li>
 *     <li><em>Relation:</em> an ordered list of nodes, ways, and other relations</li>
 * </ul>
 * See also <a href="https://wiki.openstreetmap.org/wiki/Elements">the OSM wiki</a>.
 * <p>
 * All three elements can also have tags, which are key/value pairs describing the element in question.
 * <p>
 * The OSM class represents a complete region of OpenStreetMap data. It provides utilities for adding and modifying the
 * data. Additionally, it provides utilities for parsing OSM data from an OSM XML file.
 * */
public class OSM {
    /** A bounding box defining the geographic area to be rendered. Can be null. */
    private BoundingBox<WGS84Coordinate> boundingBox;

    /** A list of nodes. */
    private final HashMap<Long, Node> nodes;
    /** A list of ways. */
    private final HashMap<Long, Way> ways;

    /**
     * A bounding box defining an area containing all nodes. When exporting a region from OpenStreetMap, the region's
     * size will be given by <code>boundingBox</code>, but in reality, there will be nodes outside that area if any
     * geometry was "cut" by the region.
     * <p>
     * Invariant: nodeBoundingBox is always at least large enough to display all data nodes, and is null when there are no nodes
     */
    private BoundingBox<WGS84Coordinate> nodeBoundingBox;

    /**
     * Constructs an OSM object.
     * @param boundingBox a geographic region representing the rendering extent (typically given by minlat, minlon,
     *                    maxlat, and maxlon in an OSM XML file. Can be null.
     */
    public OSM(BoundingBox<WGS84Coordinate> boundingBox) {
        this.boundingBox = boundingBox;
        this.nodes = new HashMap<>();
        this.ways = new HashMap<>();
        this.nodeBoundingBox = null;
    }

    /** Constructs an OSM object from an OSM XML file. */
    public static OSM fromXML(Document doc) throws UserInputException {
        XMLTools xmlTools = new XMLTools("OSM Data File");

        // Get map bounds
        BoundingBox<WGS84Coordinate> boundingBox = null;

        Element bounds = xmlTools.getSingleChildElementByTagName(doc, "bounds");
        double minLat = xmlTools.getAttributeValueDouble(bounds, "minlat");
        double minLon = xmlTools.getAttributeValueDouble(bounds, "minlon");
        double maxLat = xmlTools.getAttributeValueDouble(bounds, "maxlat");
        double maxLon = xmlTools.getAttributeValueDouble(bounds, "maxlon");
        boundingBox = new BoundingBox<>(new WGS84Coordinate(minLon, maxLat), new WGS84Coordinate(maxLon, minLat));

        // Create the OSM object
        OSM newOSM = new OSM(boundingBox);

        // Get all nodes
        NodeList rawNodes = doc.getElementsByTagName("node");
        for (int i = 0; i < rawNodes.getLength(); ++i) {
            newOSM.addNode(Node.fromXML(rawNodes.item(i)));
        }

        // Get all ways
        NodeList rawWays = doc.getElementsByTagName("way");
        for (int i = 0; i < rawWays.getLength(); ++i) {
            newOSM.addWay(Way.fromXML(rawWays.item(i)));
        }

        return newOSM;
    }

    public BoundingBox<WGS84Coordinate> getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(BoundingBox<WGS84Coordinate> boundingBox) {
        this.boundingBox = boundingBox;
    }

    /** Returns a non-modifiable list of Nodes. */
    public List<Node> getNodes() {
        return List.copyOf(nodes.values());
    }

    /** Returns a Node given an id, or null if no such Node exists. */
    public Node getNodeById(long id) {
        return nodes.get(id);
    }

    /** Adds a new Node to the list of Nodes. */
    public void addNode(Node newNode) {
        // Add the node
        nodes.put(newNode.getId(), newNode);

        // Adjust bounds accordingly
        adjustBoundsIfNecessary(newNode);
    }

    /**
     * Removes a Node by id, or does nothing if such a Node doesn't exist.
     * @param id the id of the node to remove
     * @param adjustBounds Whether to shrink the nodeBoundingBox if the removal of the node requires it. This is an O(n)
     *                     process where n is the number of nodes, so for multiple removals in a row, it is best to set
     *                     this to false and then call adjustNodeBounds at the end.
     */
    public void removeNodeById(long id, boolean adjustBounds) {
        nodes.remove(id);

        if (adjustBounds) {
            adjustNodeBounds();
        }
    }

    /**
     * Removes a Node by id, or does nothing if such a Node doesn't exist. Automatically adjusts the nodeBoundingBox
     * as necessary.
     */
    public void removeNodeById(long id) {
        removeNodeById(id, true);
    }

    /** Clears the list of Nodes. */
    public void clearNodes() {
        nodes.clear();
        nodeBoundingBox = null;
    }

    /** Returns a non-modifiable list of Ways. */
    public List<Way> getWays() {
        return List.copyOf(ways.values());
    }

    /** Returns a Way given an id, or null if no such Way exists. */
    public Way getWayById(long id) {
        return ways.get(id);
    }

    /** Adds a new Way to the list of Ways. */
    public void addWay(Way newWay) {
        ways.put(newWay.getId(), newWay);
    }

    /** Removes a Way by id, or does nothing if such a Way doesn't exist. */
    public void removeWayById(long id) {
        ways.remove(id);
    }

    /** Clears the list of Ways. */
    public void clearWays() {
        ways.clear();
    }

    public BoundingBox<WGS84Coordinate> getNodeBoundingBox() {
        return nodeBoundingBox;
    }

    /** Shrinks the bounding box as necessary to fully contain all nodes exactly. */
    public void adjustNodeBounds() {
        nodeBoundingBox = null;
        for (Node n : nodes.values()) {
            adjustBoundsIfNecessary(n);
        }
    }

    /** Given a way id, creates a list of all Nodes in the Way. */
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

    /** If node n is outside the current nodeBoundingBox, this function expands it to contain n. */
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
            nodeBoundingBox = new BoundingBox<>(
                    new WGS84Coordinate(minLon, maxLat),
                    new WGS84Coordinate(maxLon, minLat)
            );
        }
    }
}
