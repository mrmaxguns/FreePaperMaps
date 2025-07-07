package io.github.mrmaxguns.freepapermaps.osm;

import io.github.mrmaxguns.freepapermaps.projections.BoundingBox;
import io.github.mrmaxguns.freepapermaps.projections.WGS84Coordinate;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class OSM {
    private BoundingBox<WGS84Coordinate> boundingBox;

    private final ArrayList<Node> nodes;
    private final ArrayList<Way> ways;

    // Invariant: nodeBoundingBox is always at least large enough to display all data nodes, or null when there are no nodes
    private BoundingBox<WGS84Coordinate> nodeBoundingBox;

    public OSM(BoundingBox<WGS84Coordinate> boundingBox) {
        this.boundingBox = boundingBox;
        this.nodes = new ArrayList<>();
        this.ways = new ArrayList<>();
        this.nodeBoundingBox = null;
    }

    public static OSM fromXML(Document doc) {
        doc.getDocumentElement().normalize();

        // Get map bounds
        NamedNodeMap bounds = doc.getElementsByTagName("bounds").item(0).getAttributes();
        double minLat = Double.parseDouble(bounds.getNamedItem("minlat").getNodeValue());
        double minLon = Double.parseDouble(bounds.getNamedItem("minlon").getNodeValue());
        double maxLat = Double.parseDouble(bounds.getNamedItem("maxlat").getNodeValue());
        double maxLon = Double.parseDouble(bounds.getNamedItem("maxlon").getNodeValue());

        // Create the OSM object
        OSM newOSM = new OSM(new BoundingBox<>(
                new WGS84Coordinate(minLon, maxLat),
                new WGS84Coordinate(maxLon, minLat)
        ));

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

    public List<Node> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    public Node getNodeById(long id) {
        return nodes.stream().filter(n -> n.getId() == id).findAny().orElse(null);
    }

    public void addNode(Node newNode) {
        // Add the node
        this.nodes.add(newNode);

        // Adjust bounds accordingly
        adjustBoundsIfNecessary(newNode);
    }

    public void removeNodeById(long id, boolean adjustBounds) {
        nodes.removeIf(n -> n.getId() == id);

        if (adjustBounds) {
            nodeBoundingBox = null;
            for (Node n : nodes) {
                adjustBoundsIfNecessary(n);
            }
        }
    }

    public void removeNodeById(long id) {
        removeNodeById(id, false);
    }

    private void adjustBoundsIfNecessary(Node n) {
        boolean boundsChanged = false;
        WGS84Coordinate position = n.getPosition();

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

    public void clearNodes() {
        nodes.clear();
        nodeBoundingBox = null;
    }

    public List<Way> getWays() {
        return Collections.unmodifiableList(ways);
    }

    public Way getWayById(long id) {
        return ways.stream().filter(w -> w.getId() == id).findAny().orElse(null);
    }

    public void addWay(Way newWay) {
        this.ways.add(newWay);
    }

    public void removeWayById(long id) {
        ways.removeIf(w -> w.getId() == id);
    }

    public void clearWays() {
        ways.clear();
    }
}
