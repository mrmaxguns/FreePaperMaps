package io.github.mrmaxguns.freepapermaps.styling;

import io.github.mrmaxguns.freepapermaps.UserInputException;
import io.github.mrmaxguns.freepapermaps.XMLTools;
import io.github.mrmaxguns.freepapermaps.osm.Node;
import io.github.mrmaxguns.freepapermaps.osm.OSM;
import io.github.mrmaxguns.freepapermaps.osm.Way;
import io.github.mrmaxguns.freepapermaps.projections.Projection;
import io.github.mrmaxguns.freepapermaps.projections.WGS84Coordinate;
import io.github.mrmaxguns.freepapermaps.rendering.CompiledGeometry;
import io.github.mrmaxguns.freepapermaps.rendering.CompiledPolyline;

import java.awt.*;


/** A PolylineLayer operates on Way geometry and compiles to a CompiledPolyline. */
public class PolylineLayer extends Layer<Way> {
    /** This is used only if neither a fill nor stroke is specified. */
    public final static Color DEFAULT_STROKE = Color.BLACK;
    /** The stroke of a polyline is the color of the line itself. */
    private Color stroke;
    /** The fill of a polyline is the color within the polygon traced out by the polyline. If null, no fill is
     * applied. */
    private Color fill;
    /** Rendering attributes for the line. If null, no BasicStroke is applied during rendering. */
    private Stroke strokeProperties;

    public PolylineLayer(String ref, Color stroke, Color fill, Stroke strokeProperties) {
        super(ref);
        this.stroke = stroke;
        this.fill = fill;
        this.strokeProperties = strokeProperties;

        if (stroke == null && fill == null) {
            this.stroke = DEFAULT_STROKE;
        }
    }

    /** Constructs a new PolylineLayer from a node found in an XML style file. */
    public static PolylineLayer fromXML(org.w3c.dom.Node rawNode) throws UserInputException {
        XMLTools xmlTools = new XMLTools();

        String ref = xmlTools.getAttributeValue(rawNode, "ref");

        String rawStroke = xmlTools.getOptionalAttributeValue(rawNode, "stroke");
        Color stroke = rawStroke != null ? MapStyle.parseColor(rawStroke) : null;

        String rawFill = xmlTools.getOptionalAttributeValue(rawNode, "fill");
        Color fill = rawFill != null ? MapStyle.parseColor(rawFill) : null;

        String thickness = xmlTools.getOptionalAttributeValue(rawNode, "thickness");
        Stroke strokeProperties = thickness != null ? MapStyle.parseStroke(thickness) : null;

        return new PolylineLayer(ref, stroke, fill, strokeProperties);
    }

    public CompiledGeometry compile(Way way, OSM mapData, Projection projection) throws UserInputException {
        CompiledPolyline polyline = new CompiledPolyline(this);
        java.util.List<WGS84Coordinate> points = mapData.getNodesInWay(way.getId())
                .stream()
                .map(Node::getPosition)
                .toList();
        polyline.getPoints().addAll(projection.project(points));
        return polyline;
    }

    public Color getStroke() {
        return stroke;
    }

    public void setStroke(Color stroke) {
        if (fill == null && stroke == null) {
            this.stroke = DEFAULT_STROKE;
        } else {
            this.stroke = stroke;
        }
    }

    public Color getFill() {
        return fill;
    }

    public void setFill(Color fill) {
        if (fill == null && stroke == null) {
            this.fill = null;
            stroke = DEFAULT_STROKE;
        } else {
            this.fill = fill;
        }
    }

    public Stroke getStrokeProperties() {
        return strokeProperties;
    }

    public void setStrokeProperties(Stroke strokeProperties) {
        this.strokeProperties = strokeProperties;
    }
}
