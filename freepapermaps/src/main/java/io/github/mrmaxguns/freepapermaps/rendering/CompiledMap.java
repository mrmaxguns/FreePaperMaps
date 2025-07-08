package io.github.mrmaxguns.freepapermaps.rendering;

import io.github.mrmaxguns.freepapermaps.osm.OSM;
import io.github.mrmaxguns.freepapermaps.projections.BoundingBox;
import io.github.mrmaxguns.freepapermaps.projections.Projection;

import java.awt.*;
import java.util.ArrayList;

public class CompiledMap {
    private final ArrayList<CompiledGeometry> geometries;
    private final OSM mapData;
    private final Projection projection;
    private final Scaler scaler;

    public CompiledMap(OSM mapData, Projection projection, Scaler scaler) {
        this.geometries = new ArrayList<>();
        this.mapData = mapData;
        this.projection = projection;
        this.scaler = scaler;
    }

    public void add(CompiledGeometry geometry) {
        geometries.add(geometry);
    }

    public void render(Graphics2D g2d) {
        // Clip the bounds
        BoundingBox<ScaledCoordinate> finalBounds = scaler.scale(projection.project(mapData.getBoundingBox()));
        ScaledCoordinate finalOrigin = finalBounds.getTopLeftCorner();
        // TODO Figure out why height is negative!!!
        Rectangle clippingRect = new Rectangle((int)Math.round(finalOrigin.getX()), (int)Math.round(finalOrigin.getY()),
                (int)Math.round(finalBounds.getWidth()), -(int)Math.round(finalBounds.getHeight()));
        g2d.translate(-(int)Math.round(finalOrigin.getX()), -(int)Math.round(finalOrigin.getY()));
        g2d.setClip(clippingRect);

        for (CompiledGeometry geometry : geometries) {
            geometry.render(g2d, scaler);
        }
    }
}
