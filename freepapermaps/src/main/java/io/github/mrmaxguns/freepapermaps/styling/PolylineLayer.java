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
    /** The default stroke value. */
    public final static Color DEFAULT_STROKE = Color.BLACK;
    /** The stroke of a polyline is the color of the line itself. */
    private Color stroke;

    public PolylineLayer(String ref, Color stroke) {
        super(ref);
        setStroke(stroke);
    }

    /** Constructs a new PolylineLayer from a node found in an XML style file. */
    public static PolylineLayer fromXML(org.w3c.dom.Node rawNode) throws UserInputException {
        XMLTools xmlTools = new XMLTools();

        String ref = xmlTools.getAttributeValue(rawNode, "ref");

        String rawStroke = xmlTools.getOptionalAttributeValue(rawNode, "stroke");
        Color stroke = rawStroke != null ? MapStyle.parseColor(rawStroke) : null;

        return new PolylineLayer(ref, stroke);
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
        this.stroke = stroke != null ? stroke : DEFAULT_STROKE;
    }
}
