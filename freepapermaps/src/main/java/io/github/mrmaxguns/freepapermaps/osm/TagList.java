package io.github.mrmaxguns.freepapermaps.osm;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;

public class TagList extends HashMap<String, String> {
    public void insertFromXML(NodeList tags) {
        for (int i = 0; i < tags.getLength(); ++i) {
            Node tag = tags.item(i);
            if (tag.getNodeName().equals("tag")) {
                put(tag.getAttributes().getNamedItem("k").getNodeValue(), tag.getAttributes().getNamedItem("v").getNodeValue());
            }
        }
    }
}
