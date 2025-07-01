package io.github.mrmaxguns.freepapermaps;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.dom.GenericDOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;
import java.awt.Rectangle;
import java.awt.Color;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;


public class MapRenderer {
    public void renderToFile(OSM map_data, String filename) throws UnsupportedEncodingException, SVGGraphics2DIOException, UserInputException {
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();


        String svgNS = "http://www.w3.org/2000/svg";
        Document document = domImpl.createDocument(svgNS, "svg", null);

        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

        svgGenerator.setPaint(Color.red);
        svgGenerator.fill(new Rectangle(10, 10, 100, 100));

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

