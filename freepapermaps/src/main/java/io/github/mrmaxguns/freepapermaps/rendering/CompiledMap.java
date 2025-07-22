package io.github.mrmaxguns.freepapermaps.rendering;

import io.github.mrmaxguns.freepapermaps.osm.OSM;
import io.github.mrmaxguns.freepapermaps.projections.BoundingBox;
import io.github.mrmaxguns.freepapermaps.projections.Projection;
import io.github.mrmaxguns.freepapermaps.styling.MapStyle;

import java.awt.*;
import java.util.ArrayList;

import static io.github.mrmaxguns.freepapermaps.rendering.Scaler.asInteger;


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

    /** Render the complete map to g2d. The dimensions of the map itself can be accessed by calling g2d.getClip(). */
    public void render(Graphics2D g2d) {
        // Clip the bounds. Our OSM data likely extends beyond the bounding box the user wants to render, so that
        // geometry that extends beyond the boundaries is still rendered properly. Once we are done rendering, we should
        // hide any nodes outside the bounds that we used purely for maintaining correct geometry.
        BoundingBox<ScaledCoordinate> finalBounds = scaler.scale(projection.project(mapData.getBoundingBox()));
        ScaledCoordinate finalOrigin = finalBounds.getTopLeftCorner();
        // TODO: Replace instances of fillRect/Rectangle with variants that allow double values
        Rectangle clippingRect = new Rectangle(asInteger(finalOrigin.getX()), asInteger(finalOrigin.getY()),
                                               asInteger(finalBounds.getWidth()),
                                               asInteger(Math.round(finalBounds.getHeight())));
        g2d.translate(-asInteger(finalOrigin.getX()), -asInteger(finalOrigin.getY()));
        g2d.setClip(clippingRect);

        // Background color
        g2d.setBackground(style.getBackgroundColor());
        g2d.clearRect(clippingRect.x, clippingRect.y, clippingRect.width, clippingRect.height);

        // Render each layer of geometry
        for (CompiledGeometry geometry : geometries) {
            geometry.render(g2d, scaler);
        }
    }
}
