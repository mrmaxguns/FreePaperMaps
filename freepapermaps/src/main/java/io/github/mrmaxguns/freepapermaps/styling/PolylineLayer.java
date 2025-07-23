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
import org.w3c.dom.Element;

import java.awt.*;


/** A PolylineLayer operates on Way geometry and compiles to a CompiledPolyline. */
public class PolylineLayer extends Layer<Way> {
    /** The stroke of a polyline is the color of the line itself. Can be <code>null</code>. */
    private Color stroke;
    /**
     * The fill of a polyline is the color within the polygon traced out by the polyline. If <code>null</code>, no fill
     * is applied.
     */
    private Color fill;
    /** Rendering attributes for the line. Can be <code>null</code>. */
    private Stroke strokeProperties;

    public PolylineLayer(String ref, Color stroke, Color fill, Stroke strokeProperties) {
        super(ref);
        this.stroke = stroke;
        this.fill = fill;
        this.strokeProperties = strokeProperties;
    }

    public static PolylineLayer fromXML(Element rawLayer) throws UserInputException {
        return fromXML(rawLayer, new XMLTools());
    }

    /** Constructs a new PolylineLayer from a node found in an XML style file. */
    public static PolylineLayer fromXML(Element rawLayer, XMLTools xmlTools) throws UserInputException {
        StyleAttributeParser parser = new StyleAttributeParser(xmlTools);
        parser.addStringProperty("ref", true);
        parser.addColorProperty("stroke", false);
        parser.addColorProperty("fill", false);
        parser.addStrokeProperty("thickness", "cap", "join", false);
        StyleAttributeParser.ParsedAttributes attributes = parser.parse(rawLayer);

        return new PolylineLayer(attributes.stringProperties.get("ref"), attributes.colorProperties.get("stroke"),
                                 attributes.colorProperties.get("fill"), attributes.strokeProperties.get("thickness"));
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
}
