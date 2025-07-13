package io.github.mrmaxguns.freepapermaps.styling;

import io.github.mrmaxguns.freepapermaps.UserInputException;
import io.github.mrmaxguns.freepapermaps.XMLTools;
import io.github.mrmaxguns.freepapermaps.osm.Node;
import io.github.mrmaxguns.freepapermaps.osm.OSM;
import io.github.mrmaxguns.freepapermaps.osm.Way;
import io.github.mrmaxguns.freepapermaps.projections.Projection;
import io.github.mrmaxguns.freepapermaps.rendering.CompiledMap;
import io.github.mrmaxguns.freepapermaps.rendering.Scaler;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Represents a complete map style, independent of any data. A style can be <em>compiled</em> with other information to
 * produce a CompiledMap.
 */
public class MapStyle {
    /** The default background color, when left unspecified. */
    public final static Color DEFAULT_BACKGROUND_COLOR = Color.LIGHT_GRAY;
    /** An ordered list of all selectors that apply to Nodes. */
    private final HashMap<String, NodeSelector> nodeSelectors;
    /** An ordered list of all selectors that apply to Ways. */
    private final HashMap<String, WaySelector> waySelectors;
    /** An ordered list of all Node layers. */
    private final ArrayList<Layer<Node>> nodeLayers;
    /** An ordered list of all Way layers. */
    private final ArrayList<Layer<Way>> wayLayers;
    /** An ordered list of TaggedLayers, keeping track of all Node and Way layers. */
    private final ArrayList<TaggedLayer> orderedLayers;
    /** The background color of the map, which is a layer of fill applied before all other layers. */
    private Color backgroundColor;

    /** Constructs a new MapStyle. */
    public MapStyle(Color backgroundColor) {
        this.backgroundColor = backgroundColor != null ? backgroundColor : DEFAULT_BACKGROUND_COLOR;
        this.nodeSelectors = new HashMap<>();
        this.waySelectors = new HashMap<>();
        this.nodeLayers = new ArrayList<>();
        this.wayLayers = new ArrayList<>();
        this.orderedLayers = new ArrayList<>();
    }

    /** Constructs a new MapStyle from an XML "styling" document specific to FreePaperMaps. */
    public static MapStyle fromXML(Document doc) throws UserInputException {
        XMLTools xmlTools = new XMLTools("Map Style File");

        // Parse global settings
        NodeList settings = doc.getElementsByTagName("setting");
        Color backgroundColor = null;
        for (int i = 0; i < settings.getLength(); ++i) {
            org.w3c.dom.Node setting = settings.item(i);

            String key = xmlTools.getAttributeValue(setting, "k");
            String val = xmlTools.getAttributeValue(setting, "v");

            switch (key) {
            case "background-color" -> backgroundColor = parseColor(val);
            default -> throw new UserInputException("Unknown setting '" + key + "'.");
            }
        }

        // Create the map style
        MapStyle style = new MapStyle(backgroundColor);

        // Parse selectors
        NodeList selectors = xmlTools.getSingleChildElementByTagName(doc, "selectors").getChildNodes();
        for (int i = 0; i < selectors.getLength(); ++i) {
            org.w3c.dom.Node selector = selectors.item(i);
            switch (selector.getNodeName()) {
            case "node" -> style.addNodeSelector(NodeSelector.fromXML(selector));
            case "way" -> style.addWaySelector(WaySelector.fromXML(selector));
                case "#text", "#comment" -> {}
            default -> throw new UserInputException("Undefined selector '" + selector.getNodeName() + "'.");
            }
        }

        // Parse layers
        NodeList layers = xmlTools.getSingleChildElementByTagName(doc, "layers").getChildNodes();
        for (int i = 0; i < layers.getLength(); ++i) {
            org.w3c.dom.Node rawLayer = layers.item(i);

            switch (rawLayer.getNodeName()) {
            case "polyline" -> style.addWayLayer(PolylineLayer.fromXML(rawLayer));
                case "#text", "#comment" -> {}
            default -> throw new UserInputException("Undefined layer type '" + rawLayer.getNodeName() + "'.");
            }
        }

        return style;
    }

    /** Constructs a minimal map style to use for debugging. */
    public static MapStyle defaultMapStyle() {
        MapStyle style = new MapStyle(null);

        // Draw all ways
        WaySelector waySelector = new WaySelector("ways");
        style.addWaySelector(waySelector);
        style.addWayLayer(new PolylineLayer("ways", null, null, null));

        return style;
    }

    // TODO: Move the parse functions to a utility class
    /** Returns a Color parsed from colorName, which should be a 24-bit integer representation. */
    public static Color parseColor(String colorName) throws UserInputException {
        try {
            return Color.decode(colorName);
        } catch (NumberFormatException e) {
            throw new UserInputException("Invalid color '" + colorName + "'.");
        }
    }

    public static Stroke parseStroke(String rawThickness) throws UserInputException {
        float thickness;
        try {
            thickness = Float.parseFloat(rawThickness);
        } catch (NumberFormatException e) {
            throw new UserInputException("Invalid thickness value '" + rawThickness + "'.");
        }
        return new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    }

    // nodeSelectors
    public java.util.List<NodeSelector> getNodeSelectors() {
        return java.util.List.copyOf(nodeSelectors.values());
    }

    public NodeSelector getNodeSelectorById(String id) {
        return nodeSelectors.get(id);
    }

    public void addNodeSelector(NodeSelector newNodeSelector) {
        nodeSelectors.put(newNodeSelector.getId(), newNodeSelector);
    }

    public void removeNodeSelectorById(String id) {
        nodeSelectors.remove(id);
    }

    public void clearNodeSelectors() {
        nodeSelectors.clear();
    }

    // waySelectors
    public java.util.List<WaySelector> getWaySelectors() {
        return java.util.List.copyOf(waySelectors.values());
    }

    public WaySelector getWaySelectorById(String id) {
        return waySelectors.get(id);
    }

    public void addWaySelector(WaySelector newWaySelector) {
        waySelectors.put(newWaySelector.getId(), newWaySelector);
    }

    public void removeWaySelectorById(String id) {
        waySelectors.remove(id);
    }

    public void clearWaySelectors() {
        waySelectors.clear();
    }

    // nodeLayers
    public void addNodeLayer(Layer<Node> layer) {
        nodeLayers.add(layer);
        orderedLayers.add(new TaggedLayer(TaggedLayer.Type.Node, nodeLayers.size() - 1));
    }

    // wayLayers
    public void addWayLayer(Layer<Way> layer) {
        wayLayers.add(layer);
        orderedLayers.add(new TaggedLayer(TaggedLayer.Type.Way, wayLayers.size() - 1));
    }

    // all layers
    public void clearLayers() {
        nodeLayers.clear();
        wayLayers.clear();
        orderedLayers.clear();
    }

    // backgroundColor
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    /**
     * Compiles the current MapStyle, producing a CompiledMap. This means running selectors on geometry to produce a
     * final list of layers, where each layer is associated with a style as well as geometry, so that rendering can
     * happen efficiently.
     */
    public CompiledMap compile(OSM mapData, Projection projection, Scaler scaler) throws UserInputException {
        CompiledMap map = new CompiledMap(mapData, this, projection, scaler);

        for (TaggedLayer taggedLayer : orderedLayers) {
            // Process Node layers
            if (taggedLayer.type == TaggedLayer.Type.Node) {
                Layer<Node> layer = nodeLayers.get(taggedLayer.index);
                NodeSelector selector = nodeSelectors.get(layer.getRef());

                if (selector == null) {
                    throw new UserInputException("Could not find node selector with id '" + layer.getRef() + "'.");
                }

                for (Node node : mapData.getNodes()) {
                    if (selector.matches(node)) {
                        map.add(layer.compile(node, mapData, projection));
                    }
                }
            }

            // Process Way layers
            if (taggedLayer.type == TaggedLayer.Type.Way) {
                Layer<Way> layer = wayLayers.get(taggedLayer.index);
                WaySelector selector = waySelectors.get(layer.getRef());

                if (selector == null) {
                    throw new UserInputException("Could not find way selector with id '" + layer.getRef() + "'.");
                }

                for (Way way : mapData.getWays()) {
                    if (selector.matches(way)) {
                        map.add(layer.compile(way, mapData, projection));
                    }
                }
            }
        }

        return map;
    }


    /**
     * A container for a layer. Because we can't store Nodes and Ways in a single ArrayList without losing type
     * information, we keep two separate lists, and then a third list of TaggedLayers that tells us whether the next
     * layer is a Node or Way.
     */
    public static class TaggedLayer {
        Type type;
        int index;

        TaggedLayer(Type type, int index) {
            this.type = type;
            this.index = index;
        }

        enum Type {Node, Way}
    }
}
