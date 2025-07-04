package io.github.mrmaxguns.freepapermaps.osm;

import io.github.mrmaxguns.freepapermaps.projections.Coordinate;

public class Node extends Element {
    private long id;
    private Coordinate position;
    private boolean visible;

    public Node(long id, Coordinate position, boolean visible) {
        this.id = id;

        if (position.getCategory() != Coordinate.Category.WGS84) {
            throw new IllegalArgumentException("Node coordinates must be WGS84.");
        }
        this.position = position;

        this.visible = visible;
    }

    public static Node fromXML(org.w3c.dom.Node rawNode) {
        long id = Long.parseLong(rawNode.getAttributes().getNamedItem("id").getNodeValue());

        double lon = Double.parseDouble(rawNode.getAttributes().getNamedItem("lon").getNodeValue());
        double lat = Double.parseDouble(rawNode.getAttributes().getNamedItem("lat").getNodeValue());
        Coordinate position = Coordinate.newWGS84Coordinate(lon, lat);

        boolean visible = Boolean.parseBoolean(rawNode.getAttributes().getNamedItem("visible").getNodeValue());

        return new Node(id, position, visible);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Coordinate getPosition() {
        return position;
    }

    public void setPosition(Coordinate position) {
        this.position = position;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
