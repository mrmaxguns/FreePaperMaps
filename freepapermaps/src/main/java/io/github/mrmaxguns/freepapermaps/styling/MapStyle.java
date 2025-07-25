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
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;


/**
 * Represents a complete map style, independent of any data. A style can be <em>compiled</em> with other information to
 * produce a CompiledMap.
 */
public class MapStyle {
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
    /** Global settings that apply to the whole map. */
    private final GlobalSettings settings;


    /** Constructs a new <code>MapStyle</code>. */
    public MapStyle() {
        this.nodeSelectors = new HashMap<>();
        this.waySelectors = new HashMap<>();
        this.nodeLayers = new ArrayList<>();
        this.wayLayers = new ArrayList<>();
        this.orderedLayers = new ArrayList<>();
        this.settings = new GlobalSettings();
    }

    /** Constructs a new MapStyle from an XML "styling" document specific to FreePaperMaps. */
    public static MapStyle fromXML(Document doc, XMLTools xmlTools) throws UserInputException {
        // Create the map style
        MapStyle style = new MapStyle();

        // Parse global settings
        StyleAttributeParser parser = new StyleAttributeParser(xmlTools);
        java.util.List<Element> settings = xmlTools.getDirectChildElementsByTagName(doc, "setting");
        for (Element setting : settings) {
            String key = xmlTools.getAttributeValue(setting, "k");
            String val = xmlTools.getAttributeValue(setting, "v");

            GlobalSettings mapSettings = style.getSettings();
            switch (key) {
                case "background-color" -> mapSettings.backgroundColor = parser.parseColor(val);
                case "attribution-font" -> mapSettings.attributionFont = val;
                case "attribution-font-size" ->
                        mapSettings.attributionFontSize = parser.parseFontSize(val).orElseThrow();
                case "attribution-bg-color" -> mapSettings.attributionBackgroundColor = parser.parseColor(val);
                case "attribution-text-color" -> mapSettings.attributionTextColor = parser.parseColor(val);
                case "attribution-position" -> {
                    switch (val.toLowerCase()) {
                        case "top-left" -> mapSettings.attributionPosition = GlobalSettings.AttributionPosition.TopLeft;
                        case "top-right" ->
                                mapSettings.attributionPosition = GlobalSettings.AttributionPosition.TopRight;
                        case "bottom-left" ->
                                mapSettings.attributionPosition = GlobalSettings.AttributionPosition.BottomLeft;
                        case "bottom-right" ->
                                mapSettings.attributionPosition = GlobalSettings.AttributionPosition.BottomRight;
                        default -> throw new UserInputException(
                                "Could not parse 'attribution-position'. Must be one of 'top-left', 'top-right', " +
                                "'bottom-left', 'bottom-right'.");
                    }
                }
                case "attribution-text" -> mapSettings.attributionText = val;
            default -> throw new UserInputException("Unknown setting '" + key + "'.");
            }
        }

        // Parse selectors
        NodeList selectors = xmlTools.getSingleChildElementByTagName(doc, "selectors").getChildNodes();
        for (int i = 0; i < selectors.getLength(); ++i) {
            if (selectors.item(i).getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                continue;
            }
            Element selector = (Element) selectors.item(i);
            switch (selector.getTagName()) {
                case "node" -> style.addNodeSelector(NodeSelector.fromXML(selector, xmlTools));
                case "way" -> style.addWaySelector(WaySelector.fromXML(selector, xmlTools));
                default -> throw new UserInputException("Undefined selector '" + selector.getTagName() + "'.");
            }
        }

        // Parse layers
        NodeList layers = xmlTools.getSingleChildElementByTagName(doc, "layers").getChildNodes();
        for (int i = 0; i < layers.getLength(); ++i) {
            if (layers.item(i).getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                continue;
            }
            Element layer = (Element) layers.item(i);
            switch (layer.getTagName()) {
                case "polyline" -> style.addWayLayer(PolylineLayer.fromXML(layer, xmlTools));
                case "nodeshape" -> style.addNodeLayer(NodeShapeLayer.fromXML(layer, xmlTools));
                default -> throw new UserInputException("Undefined layer type '" + layer.getTagName() + "'.");
            }
        }

        return style;
    }

    public static MapStyle fromXML(Document doc) throws UserInputException {
        return fromXML(doc, new XMLTools());
    }

    /** Constructs a minimal map style to use for debugging. */
    public static MapStyle debugMapStyle() {
        MapStyle style = new MapStyle();

        // Draw all ways
        WaySelector waySelector = new WaySelector("ways", new TagQuery(new TagQuery.And()));
        style.addWaySelector(waySelector);
        style.addWayLayer(new PolylineLayer("ways", null, null, null));

        return style;
    }

    // settings
    public GlobalSettings getSettings() {
        return settings;
    }

    // nodeSelectors
    public java.util.List<NodeSelector> getNodeSelectors() {
        return java.util.List.copyOf(nodeSelectors.values());
    }

    public NodeSelector getNodeSelectorById(String id) {
        return nodeSelectors.get(id);
    }

    public void addNodeSelector(NodeSelector newNodeSelector) {
        nodeSelectors.put(newNodeSelector.getId(), Objects.requireNonNull(newNodeSelector));
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
        waySelectors.put(newWaySelector.getId(), Objects.requireNonNull(newWaySelector));
    }

    public void removeWaySelectorById(String id) {
        waySelectors.remove(id);
    }

    public void clearWaySelectors() {
        waySelectors.clear();
    }

    // nodeLayers
    public void addNodeLayer(Layer<Node> layer) {
        nodeLayers.add(Objects.requireNonNull(layer));
        orderedLayers.add(new TaggedLayer(TaggedLayer.Type.Node, nodeLayers.size() - 1));
    }

    // wayLayers
    public void addWayLayer(Layer<Way> layer) {
        wayLayers.add(Objects.requireNonNull(layer));
        orderedLayers.add(new TaggedLayer(TaggedLayer.Type.Way, wayLayers.size() - 1));
    }

    // all layers
    public void clearLayers() {
        nodeLayers.clear();
        wayLayers.clear();
        orderedLayers.clear();
    }


    /** A set of mutable options that apply to the whole map. */
    public static class GlobalSettings {
        public static final Color DEFAULT_BACKGROUND_COLOR = Color.WHITE;
        public static final String DEFAULT_ATTRIBUTION_FONT = "Serif";
        public static final int DEFAULT_ATTRIBUTION_FONT_SIZE = 10;
        public static final Color DEFAULT_ATTRIBUTION_BACKGROUND_COLOR = Color.WHITE;
        public static final Color DEFAULT_ATTRIBUTION_TEXT_COLOR = Color.BLACK;
        public static final AttributionPosition DEFAULT_ATTRIBUTION_POSITION = AttributionPosition.TopLeft;
        public static final String DEFAULT_ATTRIBUTION_TEXT = "Map data from OpenStreetMap";

        public Color backgroundColor = DEFAULT_BACKGROUND_COLOR;
        public String attributionFont = DEFAULT_ATTRIBUTION_FONT;
        public int attributionFontSize = DEFAULT_ATTRIBUTION_FONT_SIZE;
        public Color attributionBackgroundColor = DEFAULT_ATTRIBUTION_BACKGROUND_COLOR;
        public Color attributionTextColor = DEFAULT_ATTRIBUTION_TEXT_COLOR;
        public AttributionPosition attributionPosition = DEFAULT_ATTRIBUTION_POSITION;
        public String attributionText = DEFAULT_ATTRIBUTION_TEXT;


        public enum AttributionPosition {TopLeft, TopRight, BottomLeft, BottomRight}
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
                    if (node.isVisible() && selector.matches(node)) {
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
                    if (way.isVisible() && selector.matches(way)) {
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
