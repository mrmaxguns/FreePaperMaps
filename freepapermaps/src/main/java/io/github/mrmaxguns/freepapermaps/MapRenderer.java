package io.github.mrmaxguns.freepapermaps;

import io.github.mrmaxguns.freepapermaps.osm.Node;
import io.github.mrmaxguns.freepapermaps.osm.OSM;
import io.github.mrmaxguns.freepapermaps.osm.Way;
import io.github.mrmaxguns.freepapermaps.projections.Coordinate;
import io.github.mrmaxguns.freepapermaps.projections.Projection;
import io.github.mrmaxguns.freepapermaps.projections.PseudoMercatorProjection;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.dom.GenericDOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import org.apache.batik.svggen.SVGGraphics2DIOException;


public class MapRenderer {
    private final OSM mapData;
    private final Projection projection;

    public MapRenderer(OSM mapData, Projection projection) {
        this.mapData = mapData;
        this.projection = projection;
    }

    public void renderToStream(OutputStream outputFile) throws SVGGraphics2DIOException {
        // Set up the SVG and canvas on which to draw the map
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
        String svgNS = "http://www.w3.org/2000/svg";
        Document document = domImpl.createDocument(svgNS, "svg", null);
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

        // Draw the map
        renderToGraphics2D(svgGenerator);

        // Stream the SVG to a file
        boolean useCSS = true;
        Writer out = new OutputStreamWriter(outputFile, StandardCharsets.UTF_8);
        svgGenerator.stream(out, useCSS);
    }

    public void renderToGraphics2D(Graphics2D g2d) {
        // TODO: Do some actual mapping

        // A simple algorithm that draws each way with a different-colored line.
        Random rd = new Random();
        for (Way way : mapData.getWays()) {
            g2d.setColor(new Color(rd.nextFloat(), rd.nextFloat(), rd.nextFloat()));
            GeneralPath polyline = new GeneralPath(GeneralPath.WIND_EVEN_ODD, way.getNodeIds().size());
            for (int i = 0; i < way.getNodeIds().size(); ++i) {
                Coordinate position = projection.project(mapData.getNodeById(way.getNodeIds().get(i)).getPosition());
                if (i == 0) {
                    polyline.moveTo(position.getX(), position.getY());
                } else {
                    polyline.lineTo(position.getX(), position.getY());
                }
            }
            g2d.draw(polyline);
        }
    }
}
