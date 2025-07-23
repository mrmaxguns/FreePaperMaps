package io.github.mrmaxguns.freepapermaps.styling;

import io.github.mrmaxguns.freepapermaps.UserInputException;
import io.github.mrmaxguns.freepapermaps.XMLTools;
import io.github.mrmaxguns.freepapermaps.rendering.DistanceUnitManager;
import io.github.mrmaxguns.freepapermaps.rendering.UnitManager;
import org.w3c.dom.Element;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.OptionalDouble;
import java.util.OptionalInt;


public class StyleAttributeParser {
    private static final double RAD_TO_DEG = 57.295779513;

    private final XMLTools xmlTools;

    // TODO: Consolidate data structures
    private final ArrayList<String> allProperties;
    private final ArrayList<String> requiredStringProperties;
    private final ArrayList<String> optionalStringProperties;
    private final ArrayList<String> requiredColorProperties;
    private final ArrayList<String> optionalColorProperties;
    private final ArrayList<String[]> requiredStrokeProperties;
    private final ArrayList<String[]> optionalStrokeProperties;
    private final ArrayList<String> requiredLengthProperties;
    private final ArrayList<String> optionalLengthProperties;
    private final ArrayList<String> requiredCountProperties;
    private final ArrayList<String> optionalCountProperties;
    private final ArrayList<String> requiredAngleProperties;
    private final ArrayList<String> optionalAngleProperties;

    private final UnitManager distanceUnitManager;
    private final UnitManager angleUnitManager;

    public StyleAttributeParser(XMLTools xmlTools) {
        this.xmlTools = xmlTools;

        allProperties = new ArrayList<>();
        requiredStringProperties = new ArrayList<>();
        optionalStringProperties = new ArrayList<>();
        requiredColorProperties = new ArrayList<>();
        optionalColorProperties = new ArrayList<>();
        requiredStrokeProperties = new ArrayList<>();
        optionalStrokeProperties = new ArrayList<>();
        requiredLengthProperties = new ArrayList<>();
        optionalLengthProperties = new ArrayList<>();
        requiredCountProperties = new ArrayList<>();
        optionalCountProperties = new ArrayList<>();
        requiredAngleProperties = new ArrayList<>();
        optionalAngleProperties = new ArrayList<>();

        distanceUnitManager = new DistanceUnitManager();
        angleUnitManager = new UnitManager();
        angleUnitManager.addUnitMapping("", 1);
        angleUnitManager.addUnitMapping("deg", 1);
        angleUnitManager.addUnitMapping("rad", RAD_TO_DEG);
    }

    public ParsedAttributes parse(Element el) throws UserInputException {
        ParsedAttributes result = new ParsedAttributes();

        for (String prop : requiredStringProperties) {
            result.stringProperties.put(prop, xmlTools.getAttributeValue(el, prop));
        }

        for (String prop : optionalStringProperties) {
            result.stringProperties.put(prop, xmlTools.getAttributeValue(el, prop, false));
        }

        for (String prop : requiredColorProperties) {
            result.colorProperties.put(prop, parseColor(xmlTools.getAttributeValue(el, prop)));
        }

        for (String prop : optionalColorProperties) {
            result.colorProperties.put(prop, parseColor(xmlTools.getAttributeValue(el, prop, false)));
        }

        for (String[] prop : requiredStrokeProperties) {
            String thickness = xmlTools.getAttributeValue(el, prop[0]);
            String cap = xmlTools.getAttributeValue(el, prop[1]);
            String join = xmlTools.getAttributeValue(el, prop[2]);
            result.strokeProperties.put(thickness, parseStroke(thickness, cap, join));
        }

        for (String[] prop : optionalStrokeProperties) {
            String thickness = xmlTools.getAttributeValue(el, prop[0], false);
            String cap = xmlTools.getAttributeValue(el, prop[1], false);
            String join = xmlTools.getAttributeValue(el, prop[2], false);
            result.strokeProperties.put(thickness, parseStroke(thickness, cap, join));
        }

        for (String prop : requiredLengthProperties) {
            result.lengthProperties.put(prop, parseLength(xmlTools.getAttributeValue(el, prop)));
        }

        for (String prop : optionalLengthProperties) {
            result.lengthProperties.put(prop, parseLength(xmlTools.getAttributeValue(el, prop, false)));
        }

        for (String prop : requiredCountProperties) {
            result.countProperties.put(prop, parseCount(xmlTools.getAttributeValue(el, prop)));
        }

        for (String prop : optionalCountProperties) {
            result.countProperties.put(prop, parseCount(xmlTools.getAttributeValue(el, prop, false)));
        }

        for (String prop : requiredAngleProperties) {
            result.angleProperties.put(prop, parseAngle(xmlTools.getAttributeValue(el, prop)));
        }

        for (String prop : optionalAngleProperties) {
            result.angleProperties.put(prop, parseAngle(xmlTools.getAttributeValue(el, prop, false)));
        }

        return result;
    }

    public void addStringProperty(String name, boolean required) {
        checkDuplicateProperty(name);
        if (required) {
            requiredStringProperties.add(name);
        } else {
            optionalStringProperties.add(name);
        }
        allProperties.add(name);
    }

    public void addColorProperty(String name, boolean required) {
        checkDuplicateProperty(name);
        if (required) {
            requiredColorProperties.add(name);
        } else {
            optionalColorProperties.add(name);
        }
        allProperties.add(name);
    }

    public void addStrokeProperty(String thicknessName, String capName, String joinName, boolean required) {
        checkDuplicateProperty(thicknessName);
        checkDuplicateProperty(capName);
        checkDuplicateProperty(joinName);

        if (required) {
            requiredStrokeProperties.add(new String[]{ thicknessName, capName, joinName });
        } else {
            optionalStrokeProperties.add(new String[]{ thicknessName, capName, joinName });
        }

        allProperties.add(thicknessName);
        allProperties.add(capName);
        allProperties.add(joinName);
    }

    public void addLengthProperty(String name, boolean required) {
        checkDuplicateProperty(name);
        if (required) {
            requiredLengthProperties.add(name);
        } else {
            optionalLengthProperties.add(name);
        }
        allProperties.add(name);
    }

    public void addCountProperty(String name, boolean required) {
        checkDuplicateProperty(name);
        if (required) {
            requiredCountProperties.add(name);
        } else {
            optionalCountProperties.add(name);
        }
        allProperties.add(name);
    }

    public void addAngleProperty(String name, boolean required) {
        checkDuplicateProperty(name);
        if (required) {
            requiredAngleProperties.add(name);
        } else {
            optionalAngleProperties.add(name);
        }
        allProperties.add(name);
    }

    private void checkDuplicateProperty(String property) {
        if (allProperties.contains(property)) {
            throw new IllegalArgumentException(
                    "Property '" + property + "' is already registered and cannot be duplicated.");
        }
    }

    /**
     * Returns a Color parsed from colorName, which should be a 24-bit integer representation. A null input produces a
     * null output.
     */
    public Color parseColor(String colorName) throws UserInputException {
        if (colorName == null) {
            return null;
        }

        try {
            return Color.decode(colorName);
        } catch (NumberFormatException e) {
            throw xmlTools.error("Invalid color '" + colorName + "'.");
        }
    }

    public Stroke parseStroke(String rawThickness, String rawCap, String rawJoin) throws UserInputException {
        if (rawThickness == null && rawCap == null && rawJoin == null) {
            return null;
        }

        double thickness;
        if (rawThickness == null) {
            thickness = 1.0;
        } else {
            try {
                thickness = parseLength(rawThickness).orElseThrow();
            } catch (UserInputException e) {
                throw xmlTools.error("Invalid thickness value. " + e.getMessage());
            }
        }

        int cap;
        if (rawCap == null) {
            cap = BasicStroke.CAP_ROUND;
        } else {
            switch (rawCap.toLowerCase()) {
                case "butt" -> cap = BasicStroke.CAP_BUTT;
                case "round" -> cap = BasicStroke.CAP_ROUND;
                case "square" -> cap = BasicStroke.CAP_SQUARE;
                default -> throw xmlTools.error(
                        "Invalid cap specification '" + rawCap + "'. Must be one of 'butt', 'round', 'square'.");
            }
        }

        int join;
        if (rawJoin == null) {
            join = BasicStroke.JOIN_ROUND;
        } else {
            switch (rawJoin.toLowerCase()) {
                case "miter" -> join = BasicStroke.JOIN_MITER;
                case "round" -> join = BasicStroke.JOIN_ROUND;
                case "bevel" -> join = BasicStroke.JOIN_BEVEL;
                default -> throw xmlTools.error(
                        "Invalid join specification '" + rawJoin + "'. Must be one of 'miter', 'round', 'bevel'.");
            }
        }

        return new BasicStroke((float) thickness, cap, join);
    }

    public OptionalDouble parseLength(String rawLength) throws UserInputException {
        if (rawLength == null) {
            return OptionalDouble.empty();
        }

        // TODO: Support real-world units in addition to map units
        return OptionalDouble.of(distanceUnitManager.parseNumberWithUnit(rawLength));
    }

    public OptionalInt parseCount(String rawCount) throws UserInputException {
        if (rawCount == null) {
            return OptionalInt.empty();
        }

        OptionalInt result;
        try {
            result = OptionalInt.of(Integer.parseInt(rawCount));
        } catch (NumberFormatException e) {
            throw new UserInputException("Could not parse count value '" + rawCount + "'.");
        }

        if (result.getAsInt() < 0) {
            throw new UserInputException("Count must be positive, but instead got '" + result + "'.");
        }

        return result;
    }

    public OptionalDouble parseAngle(String rawAngle) throws UserInputException {
        if (rawAngle == null) {
            return OptionalDouble.empty();
        }

        return OptionalDouble.of(angleUnitManager.parseNumberWithUnit(rawAngle));
    }

    public static class ParsedAttributes {
        public HashMap<String, String> stringProperties = new HashMap<>();
        public HashMap<String, Color> colorProperties = new HashMap<>();
        public HashMap<String, Stroke> strokeProperties = new HashMap<>();
        public HashMap<String, OptionalDouble> lengthProperties = new HashMap<>();
        public HashMap<String, OptionalInt> countProperties = new HashMap<>();
        public HashMap<String, OptionalDouble> angleProperties = new HashMap<>();
    }
}
