package io.github.mrmaxguns.freepapermaps.styling;

import io.github.mrmaxguns.freepapermaps.UserInputException;
import io.github.mrmaxguns.freepapermaps.XMLTools;
import io.github.mrmaxguns.freepapermaps.osm.Node;
import io.github.mrmaxguns.freepapermaps.osm.OSM;
import io.github.mrmaxguns.freepapermaps.osm.Way;
import io.github.mrmaxguns.freepapermaps.projections.ProjectedCoordinate;
import io.github.mrmaxguns.freepapermaps.projections.Projection;
import io.github.mrmaxguns.freepapermaps.rendering.*;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class MapStyle {
    public final static Color DEFAULT_BACKGROUND_COLOR = Color.LIGHT_GRAY;
    private Color backgroundColor;

    private final HashMap<String, NodeSelector> nodeSelectors;
    private final HashMap<String, WaySelector> waySelectors;

    public static class TaggedLayer {
        enum Type { Node, Way }

        Type type;
        int index;

        TaggedLayer(Type type, int index) {
            this.type = type;
            this.index = index;
        }
    }

    private final ArrayList<Layer<Node>> nodeLayers;
    private final ArrayList<Layer<Way>> wayLayers;
    private final ArrayList<TaggedLayer> orderedLayers;

    public MapStyle(Color backgroundColor) {
        this.backgroundColor = backgroundColor != null ? backgroundColor : DEFAULT_BACKGROUND_COLOR;
        this.nodeSelectors = new HashMap<>();
        this.waySelectors = new HashMap<>();
        this.nodeLayers = new ArrayList<>();
        this.wayLayers = new ArrayList<>();
        this.orderedLayers = new ArrayList<>();
    }

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
        NodeList selectors = xmlTools.getSingleTag(doc, "selectors").getChildNodes();
        for (int i = 0; i < selectors.getLength(); ++i) {
            org.w3c.dom.Node selector = selectors.item(i);
            switch (selector.getNodeName()) {
                case "node" -> style.getNodeSelectors().put(xmlTools.getAttributeValue(selector, "id"), NodeSelector.fromXML(selector));
                case "way" -> style.getWaySelectors().put(xmlTools.getAttributeValue(selector, "id"), WaySelector.fromXML(selector));
                case "#text" -> {}
                default -> throw new UserInputException("Undefined selector '" + selector.getNodeName() + "'.");
            }
        }

        // Parse layers
        NodeList layers = xmlTools.getSingleTag(doc, "layers").getChildNodes();
        for (int i = 0; i < layers.getLength(); ++i) {
            org.w3c.dom.Node rawLayer = layers.item(i);

            switch (rawLayer.getNodeName()) {
                case "polyline" -> style.addWayLayer(PolylineLayer.fromXML(rawLayer));
                case "#text" -> {}
                default -> throw new UserInputException("Undefined layer type '" + rawLayer.getNodeName() + "'.");
            }
        }

        return style;
    }

    public static MapStyle defaultMapStyle() {
        return new MapStyle(null);
    }

    public CompiledMap compile(OSM mapData, Projection projection, Scaler scaler) {
        CompiledMap map = new CompiledMap(mapData, projection, scaler);

        for (TaggedLayer taggedLayer : orderedLayers) {
            if (taggedLayer.type == TaggedLayer.Type.Node) {
                // TODO: Error handling
                Layer<Node> layer = nodeLayers.get(taggedLayer.index);
                NodeSelector selector = nodeSelectors.get(layer.getRef());
                for (Node node : mapData.getNodes()) {
                    if (selector.matches(node)) {
                        map.add(layer.compile(node, mapData, projection));
                    }
                }
            }

            if (taggedLayer.type == TaggedLayer.Type.Way) {
                Layer<Way> layer = wayLayers.get(taggedLayer.index);
                WaySelector selector = waySelectors.get(layer.getRef());
                for (Way way : mapData.getWays()) {
                    if (selector.matches(way)) {
                        map.add(layer.compile(way, mapData, projection));
                    }
                }
            }
        }

        return map;
    }

    public static Color parseColor(String colorName) throws UserInputException {
        try {
            return Color.decode(colorName);
        } catch (NumberFormatException e) {
            throw new UserInputException("Invalid color '" + colorName + "'.");
        }
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public HashMap<String, NodeSelector> getNodeSelectors() {
        return nodeSelectors;
    }

    public HashMap<String, WaySelector> getWaySelectors() {
        return waySelectors;
    }

    public void addNodeLayer(Layer<Node> layer) {
        nodeLayers.add(layer);
        orderedLayers.add(new TaggedLayer(TaggedLayer.Type.Node, nodeLayers.size() - 1));
    }

    public void addWayLayer(Layer<Way> layer) {
        wayLayers.add(layer);
        orderedLayers.add(new TaggedLayer(TaggedLayer.Type.Way, wayLayers.size() - 1));
    }
}
