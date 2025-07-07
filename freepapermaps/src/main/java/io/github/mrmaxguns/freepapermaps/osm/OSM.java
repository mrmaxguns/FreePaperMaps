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

    public OSM(BoundingBox<WGS84Coordinate> boundingBox) {
        this.boundingBox = boundingBox;
        this.nodes = new ArrayList<>();
        this.ways = new ArrayList<>();
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
            newOSM.getNodes().add(Node.fromXML(rawNodes.item(i)));
        }

        // Get all ways
        NodeList rawWays = doc.getElementsByTagName("way");
        for (int i = 0; i < rawWays.getLength(); ++i) {
            newOSM.getWays().add(Way.fromXML(rawWays.item(i)));
        }

        return newOSM;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public Node getNodeById(long id) {
        return nodes.stream().filter(n -> n.getId() == id).findAny().orElse(null);
    }

    public void removeNodeById(long id) {
        nodes.removeIf(n -> n.getId() == id);
    }

    public List<Way> getWays() {
        return ways;
    }

    public BoundingBox<WGS84Coordinate> getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(BoundingBox<WGS84Coordinate> boundingBox) {
        this.boundingBox = boundingBox;
    }
}
