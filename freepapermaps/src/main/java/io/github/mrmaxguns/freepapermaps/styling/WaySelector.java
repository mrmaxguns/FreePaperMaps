package io.github.mrmaxguns.freepapermaps.styling;

import io.github.mrmaxguns.freepapermaps.UserInputException;
import io.github.mrmaxguns.freepapermaps.XMLTools;
import io.github.mrmaxguns.freepapermaps.osm.Way;
import org.w3c.dom.Node;


/** A Selector for Ways. */
public class WaySelector extends Selector<Way> {
    public WaySelector(String id) {
        super(id);
    }

    /** Constructs a WaySelector from an XML Node. */
    public static WaySelector fromXML(Node rawNode) throws UserInputException {
        XMLTools xmlTools = new XMLTools();
        String id = xmlTools.getAttributeValue(rawNode, "id");
        WaySelector newWaySelector = new WaySelector(id);
        newWaySelector.getTags().insertFromXML(rawNode.getChildNodes());
        return newWaySelector;
    }

    public boolean matches(Way val) {
        return getTags().isQuerySubset(val.getTags());
    }
}
