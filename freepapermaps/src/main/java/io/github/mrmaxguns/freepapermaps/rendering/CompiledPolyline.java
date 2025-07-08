package io.github.mrmaxguns.freepapermaps.rendering;

import io.github.mrmaxguns.freepapermaps.projections.ProjectedCoordinate;
import io.github.mrmaxguns.freepapermaps.styling.PolylineLayer;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;

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
        g2d.setColor(style.getStroke());
        GeneralPath polyline = new GeneralPath(GeneralPath.WIND_EVEN_ODD, points.size());

        for (int i = 0; i < points.size(); ++i) {
            ProjectedCoordinate rawCoordinate = points.get(i);
            ScaledCoordinate coordinate = scaler.scale(rawCoordinate);

            if (i == 0) {
                polyline.moveTo(coordinate.getX(), coordinate.getY());
            } else {
                polyline.lineTo(coordinate.getX(), coordinate.getY());
            }
        }

        g2d.draw(polyline);
    }
}
