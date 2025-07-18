package io.github.mrmaxguns.freepapermaps.rendering;

import io.github.mrmaxguns.freepapermaps.UserInputException;
import io.github.mrmaxguns.freepapermaps.osm.OSM;
import io.github.mrmaxguns.freepapermaps.projections.Projection;
import io.github.mrmaxguns.freepapermaps.styling.MapStyle;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.awt.*;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;


/** The MapRenderer renders a map using Apache Batik. */
public class MapRenderer {
    /** The OSM data to render the map with. */
    private final OSM mapData;
    /** Style information for the map. */
    private final MapStyle style;
    /** The map projection to use. */
    private final Projection projection;
    /** The scaler to use for final output. */
    private final Scaler scaler;

    /** Constructs a MapRenderer object. */
    public MapRenderer(OSM mapData, MapStyle style, Projection projection, Scaler scaler) {
        this.mapData = mapData;
        this.style = style;
        this.projection = projection;
        this.scaler = scaler;
    }

    /** Renders an SVG map to outputFile. */
    public void renderToStream(OutputStream outputFile) throws SVGGraphics2DIOException, UserInputException {
        // Set up the SVG and canvas on which to draw the map
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
        String svgNS = "http://www.w3.org/2000/svg";
        Document document = domImpl.createDocument(svgNS, "svg", null);
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

        // Draw the map
        renderToGraphics2D(svgGenerator);

        // Adjust SVG properties so that units are scaled properly
        Element svgRoot = svgGenerator.getRoot(document.getDocumentElement());
        svgRoot.appendChild(svgGenerator.getRoot());

        // The clipping boundaries are guaranteed to give us the map's actual dimensions in mm
        int width = svgGenerator.getClipBounds().width;
        int height = svgGenerator.getClipBounds().height;

        // Setting the root width and height with mm sets the units, and setting the viewBox with the same dimensions
        // ensures a 1:1 scale
        svgRoot.setAttribute("width", width + "mm");
        svgRoot.setAttribute("height", height + "mm");
        svgRoot.setAttribute("viewBox", "0 0 " + width + " " + height);

        // Stream the SVG to a file
        boolean useCSS = true;
        boolean escaped = true;
        Writer out = new OutputStreamWriter(outputFile, StandardCharsets.UTF_8);
        svgGenerator.stream(svgRoot, out, useCSS, escaped);
    }

    /** Renders a map to the g2d object. */
    public void renderToGraphics2D(Graphics2D g2d) throws UserInputException {
        style.compile(mapData, projection, scaler).render(g2d);
        g2d.dispose();
    }
}
