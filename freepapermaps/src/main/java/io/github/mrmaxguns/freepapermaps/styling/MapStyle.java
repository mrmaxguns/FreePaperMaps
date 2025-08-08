package io.github.mrmaxguns.freepapermaps.styling;

import io.github.mrmaxguns.freepapermaps.UserInputException;
import io.github.mrmaxguns.freepapermaps.XMLTools;
import io.github.mrmaxguns.freepapermaps.geometry.BoundingBox;
import io.github.mrmaxguns.freepapermaps.geometry.GeometryCollection;
import io.github.mrmaxguns.freepapermaps.osm.OSM;
import io.github.mrmaxguns.freepapermaps.projections.Projection;
import io.github.mrmaxguns.freepapermaps.projections.WGS84Coordinate;
import io.github.mrmaxguns.freepapermaps.rendering.ScaledCoordinate;
import io.github.mrmaxguns.freepapermaps.rendering.Scaler;
import io.github.mrmaxguns.freepapermaps.rendering.layers.Layer;
import io.github.mrmaxguns.freepapermaps.rendering.layers.PaintLayer;
import io.github.mrmaxguns.freepapermaps.styling.language.Interpreter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class MapStyle {
    private final Interpreter.Context globalContext;
    private final Map<String, Selector> selectors;
    private final java.util.List<Layer> layers;
    private final GlobalSettings settings;
    private final Projection projection;
    private final Scaler scaler;

    /** Constructs a new <code>MapStyle</code>. */
    public MapStyle(Interpreter.Context globalContext, Projection projection, Scaler scaler) {
        this.globalContext = globalContext;
        selectors = new HashMap<>();
        layers = new ArrayList<>();
        settings = new GlobalSettings();
        this.projection = projection;
        this.scaler = scaler;
    }

    /** Constructs a new MapStyle from an XML "styling" document specific to FreePaperMaps. */
    public static MapStyle fromXML(Document doc, XMLTools xmlTools, Interpreter.Context context, Projection projection,
                                   Scaler scaler) throws UserInputException {
        // Create the map style
        MapStyle style = new MapStyle(context, projection, scaler);

        // Parse global settings
        java.util.List<Element> settings = xmlTools.getDirectChildElementsByTagName(doc, "setting");
        for (Element setting : settings) {
            String key = xmlTools.getAttributeValue(setting, "k");
            String val = xmlTools.getAttributeValue(setting, "v");

            GlobalSettings mapSettings = style.getSettings();
            switch (key) {
                case "background-color" ->
                        mapSettings.backgroundColor = Interpreter.of(val).interpretExpectingColor(context);
                case "attribution-font" ->
                        mapSettings.attributionFont = Interpreter.of(val).interpretExpectingString(context);
                case "attribution-font-size" ->
                        mapSettings.attributionFontSize = (int) Interpreter.of(val).interpretExpectingUnitless(context);
                case "attribution-bg-color" ->
                        mapSettings.attributionBackgroundColor = Interpreter.of(val).interpretExpectingColor(context);
                case "attribution-text-color" ->
                        mapSettings.attributionTextColor = Interpreter.of(val).interpretExpectingColor(context);
                case "attribution-position" -> {
                    switch (Interpreter.of(val).interpretExpectingString(context)) {
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
                case "attribution-text" ->
                        mapSettings.attributionText = Interpreter.of(val).interpretExpectingString(context);
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
            style.addSelector(Selector.fromXML(selector, xmlTools));
        }

        // Parse layers
        NodeList layers = xmlTools.getSingleChildElementByTagName(doc, "layers").getChildNodes();
        for (int i = 0; i < layers.getLength(); ++i) {
            if (layers.item(i).getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                continue;
            }
            Element layer = (Element) layers.item(i);
            switch (layer.getTagName()) {
                case "PaintLayer" -> style.addLayer(PaintLayer.fromXML(layer, xmlTools));
                default -> throw new UserInputException("Undefined layer type '" + layer.getTagName() + "'.");
            }
        }

        return style;
    }

    // settings
    public GlobalSettings getSettings() {
        return settings;
    }

    public java.util.List<Selector> getSelectors() {
        return java.util.List.copyOf(selectors.values());
    }

    public Selector getSelectorById(String id) {
        return selectors.get(id);
    }

    public void addSelector(Selector newSelector) {
        selectors.put(newSelector.getId(), Objects.requireNonNull(newSelector));
    }

    public void removeSelectorById(String id) {
        selectors.remove(id);
    }

    public void clearSelectors() {
        selectors.clear();
    }

    // layers
    public void addLayer(Layer layer) {
        layers.add(layer);
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

    public void renderMapData(Graphics2D g2d, OSM mapData) throws UserInputException {
        GeometryCollection geometries = GeometryCollection.fromOSM(mapData, projection, scaler);

        for (Layer layer : layers) {
            Selector selector = selectors.get(layer.getRef());

            if (selector == null) {
                throw new UserInputException("Could not find selector with id '" + layer.getRef() + "'.");
            }

            layer.render(g2d, geometries, globalContext);
        }
    }

    /** Renders the complete map to g2d and returns a rectangle representing the dimensions of the map. */
    public Rectangle2D render(Graphics2D g2d, OSM mapData, boolean attribution) throws UserInputException {

        // Clip the bounds. Our OSM data likely extends beyond the bounding box the user wants to render, so that
        // geometry that extends beyond the boundaries is still rendered properly. Once we are done rendering, we should
        // hide any nodes outside the bounds that we used purely for maintaining correct geometry.
        BoundingBox<WGS84Coordinate> rawBounds =
                mapData.getBoundingBox() != null ? mapData.getBoundingBox() : mapData.getNodeBoundingBox();
        BoundingBox<ScaledCoordinate> finalBounds = scaler.scale(projection.project(rawBounds));
        ScaledCoordinate finalOrigin = finalBounds.getTopLeftCorner();
        Rectangle2D clippingRect = new Rectangle2D.Double(finalOrigin.getX(), finalOrigin.getY(),
                                                          finalBounds.getWidth(), finalBounds.getHeight());

        // From this point forward, the coordinate (0, 0) will correspond to our map's uncropped origin.
        g2d.translate(-finalOrigin.getX(), -finalOrigin.getY());
        g2d.setClip(clippingRect);

        // Background color
        g2d.setColor(settings.backgroundColor);
        g2d.fill(new Rectangle2D.Double(clippingRect.getX(), clippingRect.getY(), clippingRect.getWidth(),
                                        clippingRect.getHeight()));

        // Render each layer of geometry
        renderMapData(g2d, mapData);

        // We reset the origin so that (0, 0) corresponds to the cropped origin.
        g2d.translate(finalOrigin.getX(), finalOrigin.getY());
        Rectangle2D screen = new Rectangle2D.Double(0, 0, finalBounds.getWidth(), finalBounds.getHeight());
        g2d.setClip(screen);

        // Attribution
        if (attribution) {
            renderAttribution(g2d, screen);
        }

        return screen;
    }

    private void renderAttribution(Graphics2D g2d, Rectangle2D screen) {
        Font attributionFont = new Font(settings.attributionFont, Font.PLAIN, settings.attributionFontSize);
        g2d.setFont(attributionFont);
        final String attributionString = settings.attributionText;
        FontRenderContext frc = g2d.getFontRenderContext();
        Rectangle2D boundsAttribution = attributionFont.getStringBounds(attributionString, frc);
        LineMetrics attributionMetrics = attributionFont.getLineMetrics(attributionString, frc);

        double width = boundsAttribution.getWidth();
        double height = boundsAttribution.getHeight();

        // TODO: Implement proper logging
        if (width > screen.getWidth() || height > screen.getHeight()) {
            System.err.println("Warning: attribution text exceeds map dimensions.");
        }

        double boxX, boxY;
        double textX, textY;
        switch (settings.attributionPosition) {
            case TopLeft -> {
                boxX = 0;
                boxY = 0;
                textX = 0;
                textY = attributionMetrics.getAscent();
            }
            case TopRight -> {
                boxX = screen.getWidth() - width;
                boxY = 0;
                textX = screen.getWidth() - width;
                textY = attributionMetrics.getAscent();
            }
            case BottomLeft -> {
                boxX = 0;
                boxY = screen.getHeight() - height;
                textX = 0;
                textY = screen.getHeight() - attributionMetrics.getDescent();
            }
            case BottomRight -> {
                boxX = screen.getWidth() - width;
                boxY = screen.getHeight() - height;
                textX = screen.getWidth() - width;
                textY = screen.getHeight() - attributionMetrics.getDescent();
            }
            default -> throw new RuntimeException("Reached logic flow that should have never happened.");
        }

        g2d.setColor(settings.attributionBackgroundColor);
        g2d.fill(new Rectangle2D.Double(boxX, boxY, width, height));
        g2d.setColor(settings.attributionTextColor);
        g2d.drawString(attributionString, (float) textX, (float) textY);
    }
}
