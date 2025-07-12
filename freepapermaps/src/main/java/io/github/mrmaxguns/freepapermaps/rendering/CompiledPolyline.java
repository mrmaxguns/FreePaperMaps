package io.github.mrmaxguns.freepapermaps.rendering;

import io.github.mrmaxguns.freepapermaps.projections.ProjectedCoordinate;
import io.github.mrmaxguns.freepapermaps.styling.PolylineLayer;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;


/** A CompiledPolyline is a line formed by going from one point to the next in a straight line. */
public class CompiledPolyline extends CompiledGeometry {
    private final ArrayList<ProjectedCoordinate> points;
    private final PolylineLayer style;

    public CompiledPolyline(PolylineLayer style) {
        points = new ArrayList<>();
        this.style = style;
    }

    public ArrayList<ProjectedCoordinate> getPoints() {
        return points;
    }

    public void render(Graphics2D g2d, Scaler scaler) {
        // Create a new GeneralPath, which will be the path traced out by the polyline
        GeneralPath polyline = new GeneralPath(GeneralPath.WIND_EVEN_ODD, points.size());

        // Go from one point to the next
        for (int i = 0; i < points.size(); ++i) {
            ProjectedCoordinate rawCoordinate = points.get(i);
            ScaledCoordinate coordinate = scaler.scale(rawCoordinate);

            if (i == 0) {
                polyline.moveTo(coordinate.getX(), coordinate.getY());
            } else {
                polyline.lineTo(coordinate.getX(), coordinate.getY());
            }
        }

        // Set stroke properties
        if (style.getStrokeProperties() != null) {
            g2d.setStroke(style.getStrokeProperties());
        } else {
            g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        }

        // If there is a fill, do that first
        if (style.getFill() != null) {
            polyline.closePath();
            g2d.setColor(style.getFill());
            g2d.fill(polyline);
        }

        // If there is a stroke, do that
        if (style.getStroke() != null) {
            g2d.setColor(style.getStroke());
            g2d.draw(polyline);
        }
    }
}
