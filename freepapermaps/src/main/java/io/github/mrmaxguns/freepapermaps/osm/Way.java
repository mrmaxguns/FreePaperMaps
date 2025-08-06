package io.github.mrmaxguns.freepapermaps.osm;

import io.github.mrmaxguns.freepapermaps.UserInputException;
import io.github.mrmaxguns.freepapermaps.XMLTools;
import io.github.mrmaxguns.freepapermaps.projections.WGS84Coordinate;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;


/** Represents an OSM way, which is an ordered list of node ids. */
public class Way extends OSMElement {
    /** An ordered list of Node IDs associated with this Way. */
    private final ArrayList<Long> nodeIds;
    /** A mapping containing all inlined nodes (nodes whose positions were specified in the way itself). */
    private final Map<Long, io.github.mrmaxguns.freepapermaps.osm.Node> inlineNodes;

    /** Constructs a Way. */
    public Way(long id, boolean visible) {
        super(id, visible);
        this.inlineNodes = new HashMap<>();
        this.nodeIds = new ArrayList<>();
    }

    /** Constructs a Way object from an org.w3c.dom XML Node. */
    public static Way fromXML(Element rawWay) throws UserInputException {
        return fromXML(rawWay, new XMLTools());
    }

    /**
     * Constructs a Way object from an org.w3c.dom XML Node, with an XML context provided by an <code>XMLTools</code>
     * object.
     */
    public static Way fromXML(Element rawWay, XMLTools xmlTools) throws UserInputException {
        long id = getIdFromXML(rawWay, xmlTools);
        boolean visible = getVisibleFromXML(rawWay, xmlTools);
        Way newWay = new Way(id, visible);

        // A way's children are usually both node references and tags. To speed up the process, we will collect both
        // nodes and tags in one iteration rather than calling TagList's insertFromXML.
        NodeList children = rawWay.getChildNodes();
        for (int i = 0; i < children.getLength(); ++i) {
            if (children.item(i).getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element child = (Element) children.item(i);
            if (child.getTagName().equals("nd")) {
                // Parse node references
                long ref = xmlTools.getAttributeValueLong(child, "ref");
                newWay.addNodeId(ref);

                // Handle inline lon/lat, which is the format returned by Overpass Turbo.
                OptionalDouble lon = xmlTools.getAttributeValueDouble(child, "lon", false);
                OptionalDouble lat = xmlTools.getAttributeValueDouble(child, "lat", false);

                if (lon.isPresent() && lat.isPresent()) {
                    WGS84Coordinate position = new WGS84Coordinate(lon.getAsDouble(), lat.getAsDouble());
                    newWay.inlineNodes.put(ref, new io.github.mrmaxguns.freepapermaps.osm.Node(ref, position, false));
                }
            } else if (child.getTagName().equals("tag")) {
                // Parse tags
                newWay.getTags().put(
                        xmlTools.getAttributeValue(child, "k"),
                        xmlTools.getAttributeValue(child, "v"));
            }
        }

        return newWay;
    }

    /** Returns an unmodifiable list of <code>Node</code> ids. */
    public List<Long> getNodeIds() {
        return Collections.unmodifiableList(nodeIds);
    }

    /** Adds a <code>Node</code> id to the end of the list. */
    public void addNodeId(long id) {
        nodeIds.add(id);
    }

    /** Clears the list of <code>Node</code> ids and list of inline nodes. */
    public void clearNodes() {
        nodeIds.clear();
        inlineNodes.clear();
    }

    public Map<Long, io.github.mrmaxguns.freepapermaps.osm.Node> getInlineNodes() {
        return Collections.unmodifiableMap(inlineNodes);
    }

    public void addInlineNode(long id, io.github.mrmaxguns.freepapermaps.osm.Node node) {
        if (!nodeIds.contains(id)) {
            nodeIds.add(id);
        }
        inlineNodes.put(id, node);
    }
}
