package io.github.mrmaxguns.freepapermaps;

import javax.xml.parsers.ParserConfigurationException;
import java.io.UnsupportedEncodingException;
import org.apache.batik.svggen.SVGGraphics2DIOException;



public class App {
    public static void main(String[] args) throws ParserConfigurationException, UnsupportedEncodingException, SVGGraphics2DIOException {
        if (args.length != 1) {
            System.err.println("Expected exactly one argument: the name of the OSM file to read.");
            System.exit(1);
        }

        try {
            createMap(args[0]);
        } catch (UserInputException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private static void createMap(String file_name) throws ParserConfigurationException, UserInputException, UnsupportedEncodingException, SVGGraphics2DIOException  {
        OSM mapData = new OSM(file_name);
        MapRenderer renderer = new MapRenderer();
        renderer.renderToFile(mapData, "map.svg");
    }
}
