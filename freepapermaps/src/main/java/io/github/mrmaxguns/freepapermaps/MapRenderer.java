package io.github.mrmaxguns.freepapermaps;

import io.github.mrmaxguns.freepapermaps.osm.Node;
import io.github.mrmaxguns.freepapermaps.osm.OSM;
import io.github.mrmaxguns.freepapermaps.projections.Coordinate;
import io.github.mrmaxguns.freepapermaps.projections.Projection;
import io.github.mrmaxguns.freepapermaps.projections.PseudoMercatorProjection;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.dom.GenericDOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.nio.charset.StandardCharsets;

import org.apache.batik.svggen.SVGGraphics2DIOException;


public class MapRenderer {
    public void renderToStream(OSM mapData, OutputStream outputFile) throws SVGGraphics2DIOException {
        // Set up the SVG and canvas on which to draw the map
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
        String svgNS = "http://www.w3.org/2000/svg";
        Document document = domImpl.createDocument(svgNS, "svg", null);
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

        // Simple test render: draw all nodes as red squares
        svgGenerator.setPaint(Color.red);

        Projection proj = new PseudoMercatorProjection(Coordinate.newWGS84Coordinate(mapData.getMinLon(), mapData.getMaxLat()));
        for (Node n : mapData.getNodes()) {
            Coordinate projected = proj.project(n.getPosition());
            Rectangle2D rect = new Rectangle2D.Double(projected.getX() * 10, projected.getY() * 10, 10, 10);
            svgGenerator.draw(rect);
        }

        // Stream the SVG to a file
        boolean useCSS = true;
       
        Writer out = new OutputStreamWriter(outputFile, StandardCharsets.UTF_8);
        svgGenerator.stream(out, useCSS);
    }
}
