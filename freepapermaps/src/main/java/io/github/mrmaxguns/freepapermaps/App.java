package io.github.mrmaxguns.freepapermaps;

import io.github.mrmaxguns.freepapermaps.osm.OSM;
import io.github.mrmaxguns.freepapermaps.projections.*;
import io.github.mrmaxguns.freepapermaps.rendering.MapRenderer;
import io.github.mrmaxguns.freepapermaps.styling.MapStyle;
import io.github.mrmaxguns.freepapermaps.rendering.Scaler;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.apache.commons.cli.*;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.Objects;


public class App {
    enum ScaleOption {
        Fixed,
        Width,
        Height
    }

    public static void main(String[] args) throws ParserConfigurationException, UnsupportedEncodingException, SVGGraphics2DIOException {
        // Define command-line flags
        Options options = getOptions();

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

        String styleFileName = null;
        if (cmd.hasOption("s")) {
            styleFileName = cmd.getOptionValue("s");
        }

        double scale;
        ScaleOption scaleOption;
        if (cmd.hasOption("c")) {
            scale = Double.parseDouble(cmd.getOptionValue("c"));
            scaleOption = ScaleOption.Fixed;
        } else if (cmd.hasOption("w")) {
            scale = Double.parseDouble(cmd.getOptionValue("w"));
            scaleOption = ScaleOption.Width;
        } else if (cmd.hasOption("h")) {
            scale = Double.parseDouble(cmd.getOptionValue("h"));
            scaleOption = ScaleOption.Height;
        } else {
            scale = 1;
            scaleOption = ScaleOption.Fixed;
        }

        // Create the map!
        try {
            createMap(inputFileName, styleFileName, outputFile, scale, scaleOption);
        } catch (UserInputException e) {
            // Handle gracefully any errors that should be messaged to the user
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private static Options getOptions() {
        Options options = new Options();
        options.addOption("o", "output", true, "write the SVG to a specified output file instead of stdout");
        options.addOption("s", "style", true, "specify an XML style file");
        options.addOption("c", "scale", true, "set the map scale (1:SCALE) (cannot use with -w or -h)");
        options.addOption("w", "width", true, "set the map width in mm (cannot use with -c or -h)");
        options.addOption("h", "height", true, "set the map height in mm (cannot use with -c or -w)");
        return options;
    }

    private static void createMap(String inputFileName, String styleFileName, OutputStream outputFile,
                                  double scale, ScaleOption scaleOption)
            throws ParserConfigurationException, UserInputException, SVGGraphics2DIOException  {
        // Gather necessary resources
        OSM mapData = OSM.fromXML(openXMLFile(Objects.requireNonNull(inputFileName)));

        MapStyle mapStyle;
        if (styleFileName != null) {
            mapStyle = MapStyle.fromXML(openXMLFile(styleFileName));
        } else {
            mapStyle = MapStyle.defaultMapStyle();
        }

        // Create the projection so that the origin is the top-left-most point (even if the point is outside our final
        // bounding box).
        WGS84Coordinate origin = mapData.getNodeBoundingBox().getTopLeftCorner();
        Projection projection = new PseudoMercatorProjection(new WGS84Coordinate(origin.getLon(), origin.getLat()));

        // Create a scaler based on user options. The scaler is based on the final bounding box, since that is what's
        // returned to the user.
        BoundingBox<ProjectedCoordinate> projectedBounds = projection.project(mapData.getBoundingBox());
        Scaler scaler = null;
        switch (scaleOption) {
            case Fixed -> scaler = new Scaler(scale);
            case Width -> scaler = Scaler.newScalerFromWidth(projectedBounds, scale);
            case Height -> scaler = Scaler.newScalerFromHeight(projectedBounds, scale);
        }

        // Render the map!
        MapRenderer renderer = new MapRenderer(mapData, mapStyle, projection, scaler);
        renderer.renderToStream(outputFile);
    }

    private static Document openXMLFile(String fileName) throws ParserConfigurationException, UserInputException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc;
        try {
            doc = builder.parse(new File(fileName));
        } catch (SAXException e) {
            throw new UserInputException("Malformed XML detected in input file:\n" + e.getMessage());
        } catch (IOException e) {
            throw new UserInputException("File '" + fileName + "' could not be opened. Does it exist?");
        }

        doc.getDocumentElement().normalize();
        return doc;
    }
}
