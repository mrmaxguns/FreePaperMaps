package io.github.mrmaxguns.freepapermaps.osm;

import org.w3c.dom.NodeList;

import java.util.ArrayList;

public class Way {
    private long id;
    private boolean visible;
    private final ArrayList<Long> nodeIds;
    private final TagList tags;

    public Way(long id, boolean visible) {
        this.id = id;
        this.visible = visible;
        this.nodeIds = new ArrayList<>();
        this.tags = new TagList();
    }

    public static Way fromXML(org.w3c.dom.Node rawNode) {
        long id = Long.parseLong(rawNode.getAttributes().getNamedItem("id").getNodeValue());
        boolean visible = Boolean.parseBoolean(rawNode.getAttributes().getNamedItem("visible").getNodeValue());

        Way newWay = new Way(id, visible);

        NodeList children = rawNode.getChildNodes();
        for (int i = 0; i < children.getLength(); ++i) {
            org.w3c.dom.Node child = children.item(i);
            if (child.getNodeName().equals("nd")) {
                newWay.getNodeIds().add(Long.parseLong(child.getAttributes().getNamedItem("ref").getNodeValue()));
            } else if (child.getNodeName().equals("tag")) {
                newWay.getTags().put(child.getAttributes().getNamedItem("k").getNodeValue(), child.getAttributes().getNamedItem("v").getNodeValue());
            }
        }

        return newWay;
    }


    public ArrayList<Long> getNodeIds() {
        return nodeIds;
    }

    public TagList getTags() {
        return tags;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
