package io.github.mrmaxguns.freepapermaps;

import io.github.mrmaxguns.freepapermaps.osm.OSM;
import io.github.mrmaxguns.freepapermaps.projections.Projection;
import io.github.mrmaxguns.freepapermaps.projections.PseudoMercatorProjection;
import io.github.mrmaxguns.freepapermaps.projections.WGS84Coordinate;
import io.github.mrmaxguns.freepapermaps.rendering.MapRenderer;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.apache.commons.cli.*;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;


public class App {
    public static void main(String[] args) throws ParserConfigurationException, UnsupportedEncodingException, SVGGraphics2DIOException {
        // Define command-line flags
        Options options = new Options();
        options.addOption("o", "output", true, "write the SVG to a specified output file instead of stdout");

        // Parse command-line options
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println("Could not parse command-line arguments.");
            System.exit(1);
        }

        // Handle the input file argument
        String[] leftOverArgs = cmd.getArgs();
        if (leftOverArgs.length != 1) {
            System.err.println("Expected exactly one (non-flag) argument: the name of the OSM input file.");
            System.exit(1);
        }
        String inputFileName = leftOverArgs[0];

        // Handle flags
        OutputStream outputFile = null;
        if (cmd.hasOption("o")) {
            try {
                outputFile = new FileOutputStream(cmd.getOptionValue("o"));
            } catch (FileNotFoundException e) {
                System.err.println("Could not write to output file.");
                System.exit(1);
            }
        } else {
            outputFile = System.out;
        }

        // Create the map!
        try {
            createMap(inputFileName, outputFile);
        } catch (UserInputException e) {
            // Handle gracefully any errors that should be messaged to the user
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private static void createMap(String inputFileName, OutputStream outputFile)
            throws ParserConfigurationException, UserInputException, UnsupportedEncodingException, SVGGraphics2DIOException  {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

        // Try to open the file
        Document doc;
        try {
            doc = builder.parse(new File(inputFileName));
        } catch (SAXException e) {
            throw new UserInputException("Malformed XML detected in input file:\n" + e.getMessage());
        } catch (IOException e) {
            throw new UserInputException("File '" + inputFileName + "' could not be opened. Does it exist?");
        }

        // Parse the OSM XML file
        OSM mapData = OSM.fromXML(doc);

        // Render the map
        Projection projection = new PseudoMercatorProjection(new WGS84Coordinate(mapData.getBoundingBox().getMinLon(), mapData.getBoundingBox().getMaxLat()));
        MapRenderer renderer = new MapRenderer(mapData, projection);
        renderer.renderToStream(outputFile);
    }
}
