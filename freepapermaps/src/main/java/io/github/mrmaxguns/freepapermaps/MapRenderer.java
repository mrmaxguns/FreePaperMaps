package io.github.mrmaxguns.freepapermaps;

import io.github.mrmaxguns.freepapermaps.projections.Projection;
import io.github.mrmaxguns.freepapermaps.projections.Coordinate;
import io.github.mrmaxguns.freepapermaps.projections.PseudoMercatorProjection;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.dom.GenericDOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;

import java.awt.Color;
import java.awt.geom.Path2D;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;


public class MapRenderer {
    public void renderToFile(OSM map_data, String filename) throws UnsupportedEncodingException, SVGGraphics2DIOException, UserInputException {
        // Set up the SVG and canvas on which to draw the map
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
        String svgNS = "http://www.w3.org/2000/svg";
        Document document = domImpl.createDocument(svgNS, "svg", null);
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

        // TEST
        svgGenerator.setPaint(Color.red);

//        Projection proj = new PseudoMercatorProjection(24.3758030, 56.8541140);
//        Path2D polygon = new Path2D.Double();
//
//        Coordinate c1 = proj.project(Coordinate.createWGS84(24.3761312, 56.8539403));
//        polygon.moveTo(c1.getX() * 10, c1.getY() * 10);
//
//        Coordinate c2 = proj.project(Coordinate.createWGS84(24.3761578, 56.8539748));
//        polygon.lineTo(c2.getX() * 10, c2.getY() * 10);
//
//        Coordinate c3 = proj.project(Coordinate.createWGS84(24.3762245, 56.8539595));
//        polygon.lineTo(c3.getX() * 10, c3.getY() * 10);
//
//        Coordinate c4 = proj.project(Coordinate.createWGS84(24.3762112, 56.8539423));
//        polygon.lineTo(c4.getX() * 10, c4.getY() * 10);
//
//        Coordinate c5 = proj.project(Coordinate.createWGS84(24.3762376, 56.8539362));
//        polygon.lineTo(c5.getX() * 10, c5.getY() * 10);
//
//        Coordinate c6 = proj.project(Coordinate.createWGS84(24.3761572, 56.8538319));
//        polygon.lineTo(c6.getX() * 10, c6.getY() * 10);
//
//        Coordinate c7 = proj.project(Coordinate.createWGS84(24.3760251, 56.8538623));
//        polygon.lineTo(c7.getX() * 10, c7.getY() * 10);
//
//        Coordinate c8 = proj.project(Coordinate.createWGS84(24.3760921, 56.8539493));
//        polygon.lineTo(c8.getX() * 10, c8.getY() * 10);

        Projection proj = new PseudoMercatorProjection(-14.3106660, 22.5659960);
        Path2D polygon = new Path2D.Double();

        Coordinate c1 = proj.project(Coordinate.newWGS84Coordinate(-14.3106481, 22.5659566));
        polygon.moveTo(c1.getX() * 10, c1.getY() * 10);

        Coordinate c2 = proj.project(Coordinate.newWGS84Coordinate(-14.3105952, 22.5658847));
        polygon.lineTo(c2.getX() * 10, c2.getY() * 10);

        Coordinate c3 = proj.project(Coordinate.newWGS84Coordinate(-14.3105630, 22.5659048));
        polygon.lineTo(c3.getX() * 10, c3.getY() * 10);

        Coordinate c4 = proj.project(Coordinate.newWGS84Coordinate(-14.3106159, 22.5659768));
        polygon.lineTo(c4.getX() * 10, c4.getY() * 10);

        polygon.closePath();
        svgGenerator.fill(polygon);

        // Stream the SVG to a file
        boolean useCSS = true;
       
        Writer out;
        try {
            out = new OutputStreamWriter(new FileOutputStream(new File(filename)), "UTF-8");
        } catch (FileNotFoundException e) {
            throw new UserInputException("Could not write to file '" + filename + "'");
        }
        svgGenerator.stream(out, useCSS);
    }
}

