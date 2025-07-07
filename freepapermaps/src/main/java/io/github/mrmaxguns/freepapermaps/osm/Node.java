package io.github.mrmaxguns.freepapermaps.osm;

import io.github.mrmaxguns.freepapermaps.projections.WGS84Coordinate;
import org.w3c.dom.NodeList;


public class Node {
    private long id;
    private WGS84Coordinate position;
    private boolean visible;
    private final TagList tags;

    public Node(long id, WGS84Coordinate position, boolean visible) {
        this.id = id;
        this.position = position;

        this.visible = visible;
        this.tags = new TagList();
    }

    public static Node fromXML(org.w3c.dom.Node rawNode) {
        long id = Long.parseLong(rawNode.getAttributes().getNamedItem("id").getNodeValue());

        double lon = Double.parseDouble(rawNode.getAttributes().getNamedItem("lon").getNodeValue());
        double lat = Double.parseDouble(rawNode.getAttributes().getNamedItem("lat").getNodeValue());
        WGS84Coordinate position = new WGS84Coordinate(lon, lat);

        boolean visible = Boolean.parseBoolean(rawNode.getAttributes().getNamedItem("visible").getNodeValue());

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
