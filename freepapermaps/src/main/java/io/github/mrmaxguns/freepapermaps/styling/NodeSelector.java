package io.github.mrmaxguns.freepapermaps.styling;

import io.github.mrmaxguns.freepapermaps.UserInputException;
import io.github.mrmaxguns.freepapermaps.XMLTools;
import io.github.mrmaxguns.freepapermaps.osm.Node;
import org.w3c.dom.Element;


/** A Selector for Nodes. */
public class NodeSelector extends Selector<Node> {
    public NodeSelector(String id, TagQuery query) {
        super(id, query);
    }

    public static NodeSelector fromXML(Element rawSelector) throws UserInputException {
        return fromXML(rawSelector, new XMLTools());
    }

    /** Constructs a NodeSelector from an XML Node. */
    public static NodeSelector fromXML(Element rawSelector, XMLTools xmlTools) throws UserInputException {
        String id = xmlTools.getAttributeValue(rawSelector, "id");
        TagQuery query = TagQuery.fromXML(rawSelector, xmlTools);
        return new NodeSelector(id, query);
    }

    public boolean matches(Node val) {
        return getQuery().matches(val.getTags());
    }
}
