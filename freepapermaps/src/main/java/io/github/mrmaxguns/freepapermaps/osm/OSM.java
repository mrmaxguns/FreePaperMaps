package io.github.mrmaxguns.freepapermaps.osm;

import io.github.mrmaxguns.freepapermaps.UserInputException;
import io.github.mrmaxguns.freepapermaps.projections.BoundingBox;
import io.github.mrmaxguns.freepapermaps.projections.WGS84Coordinate;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collections;
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
    private final ArrayList<Node> nodes;
    /** A list of ways. */
    private final ArrayList<Way> ways;

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
        this.nodes = new ArrayList<>();
        this.ways = new ArrayList<>();
        this.nodeBoundingBox = null;
    }

    /** Constructs an OSM object from an OSM XML file. */
    public static OSM fromXML(Document doc) throws UserInputException {
        // Get map bounds
        BoundingBox<WGS84Coordinate> boundingBox = null;
        try {
            NamedNodeMap bounds = doc.getElementsByTagName("bounds").item(0).getAttributes();
            double minLat = Double.parseDouble(bounds.getNamedItem("minlat").getNodeValue());
            double minLon = Double.parseDouble(bounds.getNamedItem("minlon").getNodeValue());
            double maxLat = Double.parseDouble(bounds.getNamedItem("maxlat").getNodeValue());
            double maxLon = Double.parseDouble(bounds.getNamedItem("maxlon").getNodeValue());

            boundingBox = new BoundingBox<>(
                    new WGS84Coordinate(minLon, maxLat),
                    new WGS84Coordinate(maxLon, minLat)
            );
        } catch (NullPointerException ignored) {}

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
        return Collections.unmodifiableList(nodes);
    }

    /** Returns a Node given an id, or null if no such Node exists. */
    public Node getNodeById(long id) {
        return nodes.stream().filter(n -> n.getId() == id).findAny().orElse(null);
    }

    /** Adds a new Node to the list of Nodes. */
    public void addNode(Node newNode) {
        // Add the node
        this.nodes.add(newNode);

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
        nodes.removeIf(n -> n.getId() == id);

        if (adjustBounds) {
            adjustNodeBounds();
        }
    }

    /**
     * Removes a Node by id, or does nothing if such a Node doesn't exist. Automatically adjusts the nodeBoundingBox
     * as necessary.
     */
    public void removeNodeById(long id) {
        removeNodeById(id, false);
    }

    /** Clears the list of Nodes. */
    public void clearNodes() {
        nodes.clear();
        nodeBoundingBox = null;
    }

    /** Returns a non-modifiable list of Ways. */
    public List<Way> getWays() {
        return Collections.unmodifiableList(ways);
    }

    /** Returns a Way given an id, or null if no such Way exists. */
    public Way getWayById(long id) {
        return ways.stream().filter(w -> w.getId() == id).findAny().orElse(null);
    }

    /** Adds a new Way to the list of Ways. */
    public void addWay(Way newWay) {
        this.ways.add(newWay);
    }

    /** Removes a Way by id, or does nothing if such a Way doesn't exist. */
    public void removeWayById(long id) {
        ways.removeIf(w -> w.getId() == id);
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
        for (Node n : nodes) {
            adjustBoundsIfNecessary(n);
        }
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
