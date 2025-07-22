package io.github.mrmaxguns.freepapermaps.osm;

import io.github.mrmaxguns.freepapermaps.UserInputException;
import io.github.mrmaxguns.freepapermaps.XMLTools;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;


/**
 * Represents a collection of tags associated with a Node, Way, or Relation. Permits <code>null</code> keys and
 * values.
 */
public class TagList extends HashMap<String, String> {
    public TagList() {
        super();
    }

    public TagList(int initialCapacity) {
        super(initialCapacity);
    }

    public TagList(int initialCapacity, int loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public TagList(TagList t) {
        super(t);
    }

    public void insertFromXML(NodeList tags) throws UserInputException {
        insertFromXML(tags, new XMLTools());
    }

    public void insertFromXML(NodeList tags, XMLTools xmlTools) throws UserInputException {
        for (int i = 0; i < tags.getLength(); ++i) {
            if (tags.item(i).getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element tag = (Element) tags.item(i);
            if (tag.getTagName().equals("tag")) {
                put(xmlTools.getAttributeValue(tag, "k"), xmlTools.getAttributeValue(tag, "v"));
            }
        }
    }

    /**
     * Whether this TagList is a subset of <code>other</code>.
     * <p>
     * This is a special type of subset that is useful for queries. There is a very particular treatment of ""/null values
     * that allows ""/null to be used as a "wildcard". In other words, if this TagList contains a key with a ""/null value,
     * it will count as a match even if the other TagList we're checking has a non-null value, but this property doesn't
     * go both ways.
     */
    public boolean isQuerySubset(TagList other) {
        if (this == other) {
            return true;
        }

        for (Map.Entry<String, String> entry : entrySet()) {
            if (!other.containsKey(entry.getKey())) {
                return false;
            }

            String val = entry.getValue();
            String otherVal = other.get(entry.getKey());
            boolean valIsEmpty = val == null || val.isEmpty();
            boolean otherValIsEmpty = otherVal == null || otherVal.isEmpty();

            if (!valIsEmpty && otherValIsEmpty) {
                return false;
            } else if (!valIsEmpty) {
                if (!val.equals(otherVal)) {
                    return false;
                }
            }
        }
        return true;
    }
}
