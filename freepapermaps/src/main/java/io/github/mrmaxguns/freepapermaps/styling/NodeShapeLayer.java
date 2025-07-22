package io.github.mrmaxguns.freepapermaps.styling;

import io.github.mrmaxguns.freepapermaps.UserInputException;
import io.github.mrmaxguns.freepapermaps.XMLTools;
import io.github.mrmaxguns.freepapermaps.osm.Node;
import io.github.mrmaxguns.freepapermaps.osm.OSM;
import io.github.mrmaxguns.freepapermaps.projections.Projection;
import io.github.mrmaxguns.freepapermaps.rendering.CompiledGeometry;
import io.github.mrmaxguns.freepapermaps.rendering.CompiledNodeShape;
import org.w3c.dom.Element;

import java.awt.*;


public class NodeShapeLayer extends Layer<Node> {
    private Color stroke;
    private Color fill;
    private Stroke strokeProperties;
    private int vertices;
    private double radius;
    private double angle;

    public NodeShapeLayer(String ref, Color stroke, Color fill, Stroke strokeProperties, int vertices, double radius,
                          double angle) {
        super(ref);
        this.stroke = stroke;
        this.fill = fill;
        this.strokeProperties = strokeProperties;
        this.vertices = vertices;
        this.radius = radius;
        this.angle = angle;
    }

    public static NodeShapeLayer fromXML(Element rawLayer) throws UserInputException {
        return fromXML(rawLayer, new XMLTools());
    }

    public static NodeShapeLayer fromXML(Element rawLayer, XMLTools xmlTools) throws UserInputException {
        String ref = xmlTools.getAttributeValue(rawLayer, "ref");

        String rawStroke = xmlTools.getAttributeValue(rawLayer, "stroke", false);
        Color stroke = rawStroke != null ? xmlTools.parseColor(rawStroke) : null;

        String rawFill = xmlTools.getAttributeValue(rawLayer, "fill", false);
        Color fill = rawFill != null ? xmlTools.parseColor(rawFill) : null;

        String thickness = xmlTools.getAttributeValue(rawLayer, "thickness", false);
        Stroke strokeProperties = thickness != null ? xmlTools.parseStroke(thickness) : null;

        int vertices = xmlTools.getRequiredAttributeValueInt(rawLayer, "vertices");
        if (vertices < 3) {
            throw new UserInputException("Found node shape layer with less than 3 vertices.");
        }

        double radius = xmlTools.getRequiredAttributeValueDouble(rawLayer, "radius");

        double angle = xmlTools.getRequiredAttributeValueDouble(rawLayer, "angle");

        return new NodeShapeLayer(ref, stroke, fill, strokeProperties, vertices, radius, angle);
    }

    public CompiledGeometry compile(Node node, OSM mapData, Projection projection) {
        return new CompiledNodeShape(projection.project(node.getPosition()), this);
    }

    public Color getStroke() {
        return stroke;
    }

    public void setStroke(Color stroke) {
        this.stroke = stroke;
    }

    public Color getFill() {
        return fill;
    }

    public void setFill(Color fill) {
        this.fill = fill;
    }

    public Stroke getStrokeProperties() {
        return strokeProperties;
    }

    public void setStrokeProperties(Stroke strokeProperties) {
        this.strokeProperties = strokeProperties;
    }

    public int getVertices() {
        return vertices;
    }

    public void setVertices(int vertices) {
        this.vertices = vertices;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }
}
