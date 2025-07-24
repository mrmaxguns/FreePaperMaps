package io.github.mrmaxguns.freepapermaps.rendering;

import io.github.mrmaxguns.freepapermaps.osm.OSM;
import io.github.mrmaxguns.freepapermaps.projections.BoundingBox;
import io.github.mrmaxguns.freepapermaps.projections.Projection;
import io.github.mrmaxguns.freepapermaps.projections.WGS84Coordinate;
import io.github.mrmaxguns.freepapermaps.styling.MapStyle;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;


/** Represents a map with all data and styling information processed to allow rendering to a Graphics2D object. */
public class CompiledMap {
    private final ArrayList<CompiledGeometry> geometries;
    private final OSM mapData;
    private final MapStyle style;
    private final Projection projection;
    private final Scaler scaler;

    public CompiledMap(OSM mapData, MapStyle style, Projection projection, Scaler scaler) {
        this.geometries = new ArrayList<>();
        this.mapData = mapData;
        this.style = style;
        this.projection = projection;
        this.scaler = scaler;
    }

    public void add(CompiledGeometry geometry) {
        geometries.add(geometry);
    }

    /** Renders the complete map to g2d and returns a rectangle representing the dimensions of the map. */
    public Rectangle2D render(Graphics2D g2d, boolean attribution) {

        // Clip the bounds. Our OSM data likely extends beyond the bounding box the user wants to render, so that
        // geometry that extends beyond the boundaries is still rendered properly. Once we are done rendering, we should
        // hide any nodes outside the bounds that we used purely for maintaining correct geometry.
        BoundingBox<WGS84Coordinate> rawBounds =
                mapData.getNodeBoundingBox() != null ? mapData.getBoundingBox() : mapData.getNodeBoundingBox();
        BoundingBox<ScaledCoordinate> finalBounds = scaler.scale(projection.project(rawBounds));
        ScaledCoordinate finalOrigin = finalBounds.getTopLeftCorner();
        Rectangle2D clippingRect = new Rectangle2D.Double(finalOrigin.getX(), finalOrigin.getY(),
                                                          finalBounds.getWidth(), finalBounds.getHeight());

        // From this point forward, the coordinate (0, 0) will correspond to our map's uncropped origin.
        g2d.translate(-finalOrigin.getX(), -finalOrigin.getY());
        g2d.setClip(clippingRect);

        // Background color
        g2d.setColor(style.getSettings().backgroundColor);
        g2d.fill(new Rectangle2D.Double(clippingRect.getX(), clippingRect.getY(), clippingRect.getWidth(),
                                        clippingRect.getHeight()));

        // Render each layer of geometry
        for (CompiledGeometry geometry : geometries) {
            geometry.render(g2d, scaler);
        }

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
        MapStyle.GlobalSettings globalSettings = style.getSettings();

        Font attributionFont = new Font(globalSettings.attributionFont, Font.PLAIN, globalSettings.attributionFontSize);
        g2d.setFont(attributionFont);
        final String attributionString = globalSettings.attributionText;
        FontRenderContext frc = g2d.getFontRenderContext();
        Rectangle2D boundsAttribution = attributionFont.getStringBounds(attributionString, frc);
        LineMetrics attributionMetrics = attributionFont.getLineMetrics(attributionString, frc);

        double width = boundsAttribution.getWidth();
        double height = boundsAttribution.getHeight();

        double boxX, boxY;
        double textX, textY;
        switch (globalSettings.attributionPosition) {
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

        g2d.setColor(globalSettings.attributionBackgroundColor);
        g2d.fill(new Rectangle2D.Double(boxX, boxY, width, height));
        g2d.setColor(globalSettings.attributionTextColor);
        g2d.drawString(attributionString, (float) textX, (float) textY);
    }
}
