package io.github.mrmaxguns.freepapermaps.osm;

import io.github.mrmaxguns.freepapermaps.UserInputException;
import io.github.mrmaxguns.freepapermaps.XMLTools;
import org.w3c.dom.Element;


public abstract class OSMElement {
    private final TagList tags = new TagList();
    private long id;
    private boolean visible;

    public OSMElement(long id, boolean visible) {
        this.id = id;
        this.visible = visible;
    }

    protected static long getIdFromXML(Element rawElement, XMLTools xmlTools) throws UserInputException {
        return xmlTools.getAttributeValueLong(rawElement, "id");
    }

    protected static boolean getVisibleFromXML(Element rawElement, XMLTools xmlTools) throws UserInputException {
        String visibleRaw = xmlTools.getAttributeValue(rawElement, "visible", false);
        if (visibleRaw != null) {
            return Boolean.parseBoolean(visibleRaw);
        }
        return true;
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

    public TagList getTags() {
        return tags;
    }
}
