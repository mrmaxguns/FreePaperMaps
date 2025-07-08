package io.github.mrmaxguns.freepapermaps.rendering;

import io.github.mrmaxguns.freepapermaps.osm.OSM;
import io.github.mrmaxguns.freepapermaps.projections.Projection;
import io.github.mrmaxguns.freepapermaps.styling.MapStyle;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.dom.GenericDOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.w3c.dom.Element;


public class MapRenderer {
    private final OSM mapData;
    private final MapStyle style;
    private final Projection projection;
    private final Scaler scaler;

    public MapRenderer(OSM mapData, MapStyle style, Projection projection, Scaler scaler) {
        this.mapData = mapData;
        this.style = style;
        this.projection = projection;
        this.scaler = scaler;
    }

    public void renderToStream(OutputStream outputFile) throws SVGGraphics2DIOException {
        // Set up the SVG and canvas on which to draw the map
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
        String svgNS = "http://www.w3.org/2000/svg";
        Document document = domImpl.createDocument(svgNS, "svg", null);
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

        // Draw the map
        renderToGraphics2D(svgGenerator);

        // Do some unit magic so that the SVG turns out the correct size
        Element svgRoot = svgGenerator.getRoot(document.getDocumentElement());
        svgRoot.appendChild(svgGenerator.getRoot());

        int width = svgGenerator.getClipBounds().width;
        int height = svgGenerator.getClipBounds().height;

        svgRoot.setAttribute("width", width + "mm");
        svgRoot.setAttribute("height", height + "mm");
        svgRoot.setAttribute("viewBox", "0 0 " + width + " " + height);

        // Stream the SVG to a file
        boolean useCSS = true;
        boolean escaped = true;
        Writer out = new OutputStreamWriter(outputFile, StandardCharsets.UTF_8);
        svgGenerator.stream(svgRoot, out, useCSS, escaped);
    }

    public void renderToGraphics2D(Graphics2D g2d) {
        style.compile(mapData, projection, scaler).render(g2d);
        g2d.dispose();
    }
}
