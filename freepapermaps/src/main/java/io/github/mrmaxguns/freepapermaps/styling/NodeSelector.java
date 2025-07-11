package io.github.mrmaxguns.freepapermaps.styling;

import io.github.mrmaxguns.freepapermaps.UserInputException;
import io.github.mrmaxguns.freepapermaps.XMLTools;
import io.github.mrmaxguns.freepapermaps.osm.Node;


/** A Selector for Nodes. */
public class NodeSelector extends Selector<Node> {
    public NodeSelector(String id) {
        super(id);
    }

    /** Constructs a NodeSelector from an XML Node. */
    public static NodeSelector fromXML(org.w3c.dom.Node rawNode) throws UserInputException {
        XMLTools xmlTools = new XMLTools();
        String id = xmlTools.getAttributeValue(rawNode, "id");
        NodeSelector newNodeSelector = new NodeSelector(id);
        newNodeSelector.getTags().insertFromXML(rawNode.getChildNodes());
        return newNodeSelector;
    }

    public boolean matches(Node val) {
        return getTags().isQuerySubset(val.getTags());
    }
}
