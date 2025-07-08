package io.github.mrmaxguns.freepapermaps.styling;

import io.github.mrmaxguns.freepapermaps.UserInputException;
import io.github.mrmaxguns.freepapermaps.XMLTools;
import io.github.mrmaxguns.freepapermaps.osm.Node;
import io.github.mrmaxguns.freepapermaps.osm.OSM;
import io.github.mrmaxguns.freepapermaps.osm.Way;
import io.github.mrmaxguns.freepapermaps.projections.Projection;
import io.github.mrmaxguns.freepapermaps.rendering.CompiledGeometry;
import io.github.mrmaxguns.freepapermaps.rendering.CompiledPolyline;

import java.awt.*;

public class PolylineLayer extends Layer<Way> {
    public final static Color DEFAULT_STROKE = Color.BLACK;
    private Color stroke;

    public PolylineLayer(String ref, Color stroke) {
        super(ref);
        this.stroke = stroke != null ? stroke : DEFAULT_STROKE;
    }

    public static PolylineLayer fromXML(org.w3c.dom.Node rawNode) throws UserInputException {
        XMLTools xmlTools = new XMLTools();

        String ref = xmlTools.getAttributeValue(rawNode, "ref");

        String rawStroke = xmlTools.getOptionalAttributeValue(rawNode, "stroke");
        Color stroke = rawStroke != null ? MapStyle.parseColor(rawStroke) : null;

        return new PolylineLayer(ref, stroke);
    }

    public CompiledGeometry compile(Way way, OSM mapData, Projection projection) {
        CompiledPolyline polyline = new CompiledPolyline(this);
        for (long nodeId : way.getNodeIds()) {
            // TODO Error handling
            Node node = mapData.getNodeById(nodeId);
            polyline.getPoints().add(projection.project(node.getPosition()));
        }
        return polyline;
    }

    public Color getStroke() {
        return stroke;
    }
}
