package io.github.mrmaxguns.freepapermaps.styling;

import io.github.mrmaxguns.freepapermaps.UserInputException;
import io.github.mrmaxguns.freepapermaps.XMLTools;
import io.github.mrmaxguns.freepapermaps.osm.Way;
import org.w3c.dom.Element;


/** A Selector for Ways. */
public class WaySelector extends Selector<Way> {
    public WaySelector(String id) {
        super(id);
    }

    public static WaySelector fromXML(Element rawSelector) throws UserInputException {
        return fromXML(rawSelector, new XMLTools());
    }

    /** Constructs a WaySelector from an XML Node. */
    public static WaySelector fromXML(Element rawSelector, XMLTools xmlTools) throws UserInputException {
        String id = xmlTools.getAttributeValue(rawSelector, "id");
        WaySelector newWaySelector = new WaySelector(id);
        newWaySelector.getTags().insertFromXML(rawSelector.getChildNodes());
        return newWaySelector;
    }

    public boolean matches(Way val) {
        return getTags().isQuerySubset(val.getTags());
    }
}
