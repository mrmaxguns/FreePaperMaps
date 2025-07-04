package io.github.mrmaxguns.freepapermaps;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import io.github.mrmaxguns.freepapermaps.osm.OSM;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;


public class App {
    public static void main(String[] args) throws ParserConfigurationException, UnsupportedEncodingException, SVGGraphics2DIOException {
//        if (args.length != 1) {
//            System.err.println("Expected exactly one argument: the name of the OSM file to read.");
//            System.exit(1);
//        }

        try {
            // createMap(args[0]);
            createMap("/home/maxim/downloads/map(1).osm");
        } catch (UserInputException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private static void createMap(String file_name) throws ParserConfigurationException, UserInputException, UnsupportedEncodingException, SVGGraphics2DIOException  {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

        // Try to open the file
        Document doc;
        try {
            doc = builder.parse(new File(file_name));
        } catch (SAXException e) {
            throw new UserInputException("Malformed XML detected in input file:\n" + e.getMessage());
        } catch (IOException e) {
            throw new UserInputException("File '" + file_name + "' could not be opened. Does it exist?");
        }

        OSM mapData = OSM.fromXML(doc);
        MapRenderer renderer = new MapRenderer();
        renderer.renderToFile(mapData, "map.svg");
    }
}
