package io.github.mrmaxguns.freepapermaps;

import javax.xml.parsers.ParserConfigurationException;


public class App {
    public static void main(String[] args) throws ParserConfigurationException {
        if (args.length != 1) {
            System.err.println("Expected exactly one argument: the name of the OSM file to read.");
            System.exit(1);
        }

        try {
            create_map(args[0]);
        } catch (UserInputException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private static void create_map(String file_name) throws ParserConfigurationException, UserInputException {
        OSM map_data = new OSM(file_name);
        MapRenderer renderer = new MapRenderer();
        renderer.render_to_file(map_data, "map.svg");
    }
}
