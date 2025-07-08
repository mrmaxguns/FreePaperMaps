package io.github.mrmaxguns.freepapermaps.rendering;

import io.github.mrmaxguns.freepapermaps.UserInputException;
import io.github.mrmaxguns.freepapermaps.osm.Node;
import io.github.mrmaxguns.freepapermaps.osm.OSM;
import io.github.mrmaxguns.freepapermaps.osm.Way;
import io.github.mrmaxguns.freepapermaps.projections.Projection;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class MapStyle {
    public final static Color DEFAULT_BACKGROUND_COLOR = Color.LIGHT_GRAY;
    private Color backgroundColor;

    private final HashMap<String, Node> nodeSelectors;
    private final HashMap<String, Way> waySelectors;

    private final ArrayList<Layer> layers;

    public MapStyle(Color backgroundColor) {
        this.backgroundColor = backgroundColor != null ? backgroundColor : DEFAULT_BACKGROUND_COLOR;
        this.nodeSelectors = new HashMap<>();
        this.waySelectors = new HashMap<>();
        this.layers = new ArrayList<>();
    }

    public static MapStyle fromXML(Document doc) throws UserInputException {
        // Parse global settings
        NodeList settings = doc.getElementsByTagName("setting");
        Color backgroundColor = null;
        for (int i = 0; i < settings.getLength(); ++i) {
            org.w3c.dom.Node setting = settings.item(i);

            String key, val;
            try {
                key = setting.getAttributes().getNamedItem("k").getNodeValue();
                val = setting.getAttributes().getNamedItem("v").getNodeValue();
            } catch (NullPointerException e) {
                throw new UserInputException("Found setting without either key or value (or both).");
            }

            switch (key) {
                case "background-color" -> backgroundColor = parseColor(val);
                default -> throw new UserInputException("Unknown setting '" + key + "'.");
            }
        }

        // Create the map style
        MapStyle style = new MapStyle(backgroundColor);

        // Parse selectors
        NodeList selectors;
        try {
            selectors = doc.getElementsByTagName("selectors").item(0).getChildNodes();
        } catch (NullPointerException e) {
            throw new UserInputException("Settings file missing required element 'selectors'.");
        }
        for (int i = 0; i < selectors.getLength(); ++i) {
            org.w3c.dom.Node selector = selectors.item(i);
            switch (selector.getNodeName()) {
                case "node" -> style.getNodeSelectors().put(selector.getAttributes().getNamedItem("id").getNodeValue(), Node.fromXML(selector));
                case "way" -> style.getWaySelectors().put(selector.getAttributes().getNamedItem("id").getNodeValue(), Way.fromXML(selector));
                case "#text" -> {}
                default -> throw new UserInputException("Undefined selector '" + selector.getNodeName() + "'.");
            }
        }

        // Parse layers
        NodeList layers;
        try {
            layers = doc.getElementsByTagName("layers").item(0).getChildNodes();
        } catch (NullPointerException e) {
            throw new UserInputException("Settings file missing required element 'layers'.");
        }
        for (int i = 0; i < layers.getLength(); ++i) {
            org.w3c.dom.Node rawLayer = layers.item(i);
            Layer layer;

            switch (rawLayer.getNodeName()) {
                case "polyline" -> layer = PolylineLayer.fromXML(rawLayer);
                case "polygon" -> layer = PolygonLayer.fromXML(rawLayer);
                default -> throw new UserInputException("Undefined layer type '" + rawLayer.getNodeName() + "'.");
            }

            style.getLayers().add(layer);
        }

        return style;
    }

    public static MapStyle defaultMapStyle() {
        return new MapStyle(null);
    }

    public CompiledMap compile(OSM mapData, Projection projection, Scaler scaler) {

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

    public HashMap<String, Node> getNodeSelectors() {
        return nodeSelectors;
    }

    public HashMap<String, Way> getWaySelectors() {
        return waySelectors;
    }

    public ArrayList<Layer> getLayers() {
        return layers;
    }
}
