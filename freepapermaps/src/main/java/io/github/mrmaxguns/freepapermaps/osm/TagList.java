package io.github.mrmaxguns.freepapermaps.osm;

import io.github.mrmaxguns.freepapermaps.UserInputException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;

/** Represents a collection of tags associated with a Node, Way, or Relation. */
public class TagList extends HashMap<String, String> {
    public void insertFromXML(NodeList tags) throws UserInputException {
        for (int i = 0; i < tags.getLength(); ++i) {
            Node tag = tags.item(i);
            if (tag.getNodeName().equals("tag")) {
                try {
                    put(
                            tag.getAttributes().getNamedItem("k").getNodeValue(),
                            tag.getAttributes().getNamedItem("v").getNodeValue()
                    );
                } catch (NullPointerException e) {
                    throw new UserInputException("Found a tag missing either 'k' or 'v' (or both).");
                }
            }
        }
    }
}
