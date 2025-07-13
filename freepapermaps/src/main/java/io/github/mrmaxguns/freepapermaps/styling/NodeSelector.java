package io.github.mrmaxguns.freepapermaps.styling;

import io.github.mrmaxguns.freepapermaps.UserInputException;
import io.github.mrmaxguns.freepapermaps.XMLTools;
import io.github.mrmaxguns.freepapermaps.osm.Node;
import org.w3c.dom.Element;


/** A Selector for Nodes. */
public class NodeSelector extends Selector<Node> {
    public NodeSelector(String id) {
        super(id);
    }

    public static NodeSelector fromXML(Element rawSelector) throws UserInputException {
        return fromXML(rawSelector, new XMLTools());
    }

    /** Constructs a NodeSelector from an XML Node. */
    public static NodeSelector fromXML(Element rawSelector, XMLTools xmlTools) throws UserInputException {
        String id = xmlTools.getAttributeValue(rawSelector, "id");
        NodeSelector newNodeSelector = new NodeSelector(id);
        newNodeSelector.getTags().insertFromXML(rawSelector.getChildNodes());
        return newNodeSelector;
    }

    public boolean matches(Node val) {
        return getTags().isQuerySubset(val.getTags());
    }
}
