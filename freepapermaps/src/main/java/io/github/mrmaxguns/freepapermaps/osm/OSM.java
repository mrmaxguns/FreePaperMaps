package io.github.mrmaxguns.freepapermaps.osm;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OSM {
    private double minLat;
    private double minLon;
    private double maxLat;
    private double maxLon;

    private final ArrayList<Node> nodes;

    public OSM(double minLat, double minLon, double maxLat, double maxLon) {
        this.minLat = minLat;
        this.minLon = minLon;
        this.maxLat = maxLat;
        this.maxLon = maxLon;

        this.nodes = new ArrayList<>();
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
        OSM newOSM = new OSM(minLat, minLon, maxLat, maxLon);

        // Get all nodes
        NodeList rawNodes = doc.getElementsByTagName("node");
        for (int i = 0; i < rawNodes.getLength(); ++i) {
            newOSM.addNode(Node.fromXML(rawNodes.item(i)));
        }

        return newOSM;
    }

    public double getMinLat() {
        return minLat;
    }

    public void setMinLat(double minLat) {
        this.minLat = minLat;
    }

    public double getMinLon() {
        return minLon;
    }

    public void setMinLon(double minLon) {
        this.minLon = minLon;
    }

    public double getMaxLat() {
        return maxLat;
    }

    public void setMaxLat(double maxLat) {
        this.maxLat = maxLat;
    }

    public double getMaxLon() {
        return maxLon;
    }

    public void setMaxLon(double maxLon) {
        this.maxLon = maxLon;
    }

    public List<Node> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    public void addNode(Node n) {
        this.nodes.add(n);
    }

    public Node getNodeById(long id) {
        return nodes.stream().filter(n -> n.getId() == id).findAny().orElse(null);
    }

    public void removeNodeById(long id) {
        nodes.removeIf(n -> n.getId() == id);
    }
}
