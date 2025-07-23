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
import java.util.OptionalDouble;


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
        StyleAttributeParser parser = new StyleAttributeParser(xmlTools);
        parser.addStringProperty("ref", true);
        parser.addColorProperty("stroke", false);
        parser.addColorProperty("fill", false);
        parser.addStrokeProperty("thickness", "cap", "join", false);
        parser.addCountProperty("vertices", true);
        parser.addLengthProperty("radius", true);
        parser.addAngleProperty("angle", false);
        StyleAttributeParser.ParsedAttributes attributes = parser.parse(rawLayer);

        int vertices = attributes.countProperties.get("vertices").orElseThrow();
        if (vertices < 3) {
            throw new UserInputException("Found node shape layer with less than 3 vertices.");
        }

        OptionalDouble radius = attributes.lengthProperties.get("radius");
        OptionalDouble angle = attributes.angleProperties.get("angle");

        return new NodeShapeLayer(attributes.stringProperties.get("ref"), attributes.colorProperties.get("stroke"),
                                  attributes.colorProperties.get("fill"), attributes.strokeProperties.get("thickness"),
                                  vertices, radius.isEmpty() ? 2.0 : radius.getAsDouble(),
                                  angle.isEmpty() ? 0.0 : angle.getAsDouble());
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
