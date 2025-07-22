package io.github.mrmaxguns.freepapermaps.rendering;

import io.github.mrmaxguns.freepapermaps.projections.ProjectedCoordinate;
import io.github.mrmaxguns.freepapermaps.styling.NodeShapeLayer;

import java.awt.*;
import java.awt.geom.GeneralPath;


public class CompiledNodeShape extends CompiledGeometry {
    public static final float DEFAULT_STROKE_WIDTH = 2;
    public static final Color DEFAULT_FILL = Color.GREEN;
    private final ProjectedCoordinate position;
    private final NodeShapeLayer style;

    public CompiledNodeShape(ProjectedCoordinate position, NodeShapeLayer style) {
        this.position = position;
        this.style = style;
    }

    public void render(Graphics2D g2d, Scaler scaler) {
        GeneralPath polyline = new GeneralPath(GeneralPath.WIND_EVEN_ODD, style.getVertices());

        double initialAngle = Math.toRadians(style.getAngle());

        // Current x and y positions in a cartesian coordinate system where the center is the middle of our shape
        double currentX = style.getRadius() * Math.cos(initialAngle);
        double currentY = style.getRadius() * Math.sin(initialAngle);

        // Offset to convert our x and y positions to the onscreen coordinate system
        ScaledCoordinate offset = scaler.scale(position);

        // Remember that y is inverted in the onscreen coordinate system
        polyline.moveTo(offset.getX() + currentX, offset.getY() - currentY);

        double rotationAngle = Math.toRadians(360.0 / style.getVertices());

        for (int i = 0; i < style.getVertices() - 1; ++i) {
            double oldX = currentX; // Save the value of x since we want the value before reassignment to calculate y
            currentX = (currentX * Math.cos(rotationAngle)) - (currentY * Math.sin(rotationAngle));
            currentY = (oldX * Math.sin(rotationAngle)) + (currentY * Math.cos(rotationAngle));

            polyline.lineTo(offset.getX() + currentX, offset.getY() - currentY);
        }

        // Set stroke properties
        if (style.getStrokeProperties() != null) {
            g2d.setStroke(style.getStrokeProperties());
        } else {
            // TODO: Refactor the stroking system so that cap and join have defaults that show up only in the
            //  rendering phase
            g2d.setStroke(new BasicStroke(DEFAULT_STROKE_WIDTH, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
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

        // If there is neither a fill, nor stroke, do a default fill
        if (style.getFill() == null && style.getStroke() == null) {
            polyline.closePath();
            g2d.setColor(DEFAULT_FILL);
            g2d.fill(polyline);
        }
    }
}
