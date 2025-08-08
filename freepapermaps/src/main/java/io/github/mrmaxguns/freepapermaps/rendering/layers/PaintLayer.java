package io.github.mrmaxguns.freepapermaps.rendering.layers;

import io.github.mrmaxguns.freepapermaps.UserInputException;
import io.github.mrmaxguns.freepapermaps.XMLTools;
import io.github.mrmaxguns.freepapermaps.geometry.Coordinate;
import io.github.mrmaxguns.freepapermaps.geometry.WayGeometry;
import io.github.mrmaxguns.freepapermaps.styling.language.Interpreter;
import org.w3c.dom.Element;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.util.Map;


public class PaintLayer extends Layer {
    public static final double DEFAULT_LINE_WIDTH = 1.0;
    public static final String DEFAULT_LINE_CAP = "round";
    public static final String DEFAULT_LINE_JOIN = "round";
    public static final Color DEFAULT_LINE_COLOR = Color.BLUE;

    public static final String LINE_COLOR_FIELD = "line-color";
    public static final String FILL_COLOR_FIELD = "fill-color";
    public static final String LINE_WIDTH_FIELD = "line-width";
    public static final String LINE_CAP_FIELD = "line-cap";
    public static final String LINE_JOIN_FIELD = "line-join";

    static {
        name = "PaintLayer";

        defineOptionalField(LINE_COLOR_FIELD, Interpreter.Primitive.Type.Color);
        defineOptionalField(FILL_COLOR_FIELD, Interpreter.Primitive.Type.Color);
        defineOptionalField(LINE_WIDTH_FIELD, Interpreter.Primitive.Type.Distance);
        defineOptionalField(LINE_CAP_FIELD, Interpreter.Primitive.Type.String);
        defineOptionalField(LINE_JOIN_FIELD, Interpreter.Primitive.Type.String);
    }

    public PaintLayer(String ref, Map<String, String> rawFields) throws UserInputException {
        super(ref, rawFields);
    }

    public static PaintLayer fromXML(Element rawLayer, XMLTools xmlTools) throws UserInputException {
        return new PaintLayer(getRefFromXML(rawLayer, xmlTools), getRawFieldsFromXML(rawLayer, xmlTools));
    }

    @Override
    public void renderWay(Graphics2D g2d, WayGeometry way, ComputedFieldMap fields) throws UserInputException {
        // Create a new GeneralPath, which will be the path traced out by the polyline
        GeneralPath polyline = new GeneralPath(GeneralPath.WIND_EVEN_ODD, way.getNodes().size());

        // Go from one point to the next
        for (int i = 0; i < way.getNodes().size(); ++i) {
            Coordinate coordinate = way.getNodes().get(i).getPosition();

            if (i == 0) {
                polyline.moveTo(coordinate.getX(), coordinate.getY());
            } else {
                polyline.lineTo(coordinate.getX(), coordinate.getY());
            }
        }

        // Set stroke properties
        float width = (float) fields.getNumber(LINE_WIDTH_FIELD, DEFAULT_LINE_WIDTH);

        int cap;
        String rawCap = fields.getString(LINE_CAP_FIELD, DEFAULT_LINE_CAP);
        switch (rawCap.toLowerCase()) {
            case "butt" -> cap = BasicStroke.CAP_BUTT;
            case "round" -> cap = BasicStroke.CAP_ROUND;
            case "square" -> cap = BasicStroke.CAP_SQUARE;
            default -> throw new UserInputException(
                    "Invalid cap specification '" + rawCap + "'. Must be one of 'butt', 'round', 'square'.");
        }

        int join;
        String rawJoin = fields.getString(LINE_JOIN_FIELD, DEFAULT_LINE_JOIN);
        switch (rawJoin.toLowerCase()) {
            case "miter" -> join = BasicStroke.JOIN_MITER;
            case "round" -> join = BasicStroke.JOIN_ROUND;
            case "bevel" -> join = BasicStroke.JOIN_BEVEL;
            default -> throw new UserInputException(
                    "Invalid join specification '" + rawJoin + "'. Must be one of 'miter', 'round', 'bevel'.");
        }

        g2d.setStroke(new BasicStroke(width, cap, join));

        // If there is a fill, do that first
        if (fields.containsKey(FILL_COLOR_FIELD)) {
            polyline.closePath();
            g2d.setColor(fields.getColor(FILL_COLOR_FIELD));
            g2d.fill(polyline);
        }

        // If there is a stroke, do that
        if (fields.containsKey(LINE_COLOR_FIELD)) {
            g2d.setColor(fields.getColor(LINE_COLOR_FIELD));
            g2d.draw(polyline);
        }

        // If there is neither a fill, nor stroke, do a default stroke
        if (!fields.containsKey(FILL_COLOR_FIELD) && !fields.containsKey(LINE_COLOR_FIELD)) {
            g2d.setColor(DEFAULT_LINE_COLOR);
            g2d.draw(polyline);
        }
    }
}
