package io.github.mrmaxguns.freepapermaps.rendering.layers;

import io.github.mrmaxguns.freepapermaps.UserInputException;
import io.github.mrmaxguns.freepapermaps.XMLTools;
import io.github.mrmaxguns.freepapermaps.geometry.*;
import io.github.mrmaxguns.freepapermaps.styling.language.Interpreter;
import org.w3c.dom.Element;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public abstract class Layer {
    public final static Map<String, Interpreter.Primitive.Type> requiredFieldDefinitions = new HashMap<>();
    public final static Map<String, Interpreter.Primitive.Type> optionalFieldDefinitions = new HashMap<>();
    public static String name = "Layer";

    private final String ref;
    private final Map<String, Interpreter> fields = new HashMap<>();
    private final Map<String, Interpreter.Primitive.Type> allFieldDefinitions = new HashMap<>();

    public Layer(String ref, Map<String, String> rawFields) throws UserInputException {
        this.ref = Objects.requireNonNull(ref);

        java.util.List<String> requiredFields = new ArrayList<>(requiredFieldDefinitions.keySet());
        for (Map.Entry<String, String> entry : rawFields.entrySet()) {
            if (requiredFieldDefinitions.containsKey(entry.getKey()) ||
                optionalFieldDefinitions.containsKey(entry.getKey())) {
                fields.put(entry.getKey(), Interpreter.of(entry.getValue()));
            } else {
                throw new UserInputException("Layer " + name + " received invalid field '" + entry.getKey() + "'.");
            }

            requiredFields.remove(entry.getKey());
        }

        if (!requiredFields.isEmpty()) {
            throw new UserInputException("Layer " + name + " is missing one or more required fields.");
        }

        allFieldDefinitions.putAll(requiredFieldDefinitions);
        allFieldDefinitions.putAll(optionalFieldDefinitions);
    }

    public static void defineRequiredField(String name, Interpreter.Primitive.Type type) {
        requiredFieldDefinitions.put(name, type);
    }

    public static void defineOptionalField(String name, Interpreter.Primitive.Type type) {
        optionalFieldDefinitions.put(name, type);
    }

    public static Map<String, String> getRawFieldsFromXML(Element rawLayer, XMLTools xmlTools) throws
            UserInputException {
        Map<String, String> rawFields = new HashMap<>();

        for (String field : requiredFieldDefinitions.keySet()) {
            rawFields.put(field, xmlTools.getAttributeValue(rawLayer, field, true));
        }

        for (String field : optionalFieldDefinitions.keySet()) {
            String value = xmlTools.getAttributeValue(rawLayer, field, false);

            if (value != null) {
                rawFields.put(field, value);
            }
        }

        return rawFields;
    }

    public static String getRefFromXML(Element rawLayer, XMLTools xmlTools) throws UserInputException {
        return xmlTools.getAttributeValue(rawLayer, "ref", true);
    }

    public void render(Graphics2D g2d, GeometryCollection geometries, Interpreter.Context context) {

    }

    public void render(Graphics2D g2d, Geometry geometry, Interpreter.Context context) throws UserInputException {
        Interpreter.Context newContext = context.extendWithValueVariables(geometry.getTags());

        ComputedFieldMap computedFields = new ComputedFieldMap();
        for (Map.Entry<String, Interpreter> field : fields.entrySet()) {
            Interpreter.Primitive result = field.getValue().interpret(newContext);

            if (result.type != allFieldDefinitions.get(field.getKey())) {
                throw new UserInputException(
                        "Field '" + field.getKey() + "' is expected to be of type " + result.type.name());
            }

            computedFields.put(field.getKey(), result);
        }

        if (geometry instanceof NodeGeometry) {
            renderNode(g2d, (NodeGeometry) geometry, computedFields);
        } else if (geometry instanceof WayGeometry) {
            renderWay(g2d, (WayGeometry) geometry, computedFields);
        } else if (geometry instanceof PolygonGeometry) {
            renderPolygon(g2d, (PolygonGeometry) geometry, computedFields);
        } else if (geometry instanceof RelationGeometry) {
            renderRelation(g2d, (RelationGeometry) geometry, computedFields);
        } else {
            throw new RuntimeException("Unexpected geometry type encountered.");
        }
    }

    public void renderNode(Graphics2D g2d, NodeGeometry node, ComputedFieldMap fields)                        {}

    public void renderWay(Graphics2D g2d, WayGeometry way, ComputedFieldMap fields) throws UserInputException {}

    public void renderPolygon(Graphics2D g2d, PolygonGeometry polygon, ComputedFieldMap fields)               {}

    public void renderRelation(Graphics2D g2d, RelationGeometry relation, ComputedFieldMap fields)            {}

    public String getRef() {
        return ref;
    }

    public static class ComputedFieldMap extends HashMap<String, Interpreter.Primitive> {
        public double getNumber(String key) {
            return ((Interpreter.NumericPrimitive) getOrError(key)).value;
        }

        public double getNumber(String key, double defaultVal) {
            if (containsKey(key)) return getNumber(key);
            return defaultVal;
        }

        public Color getColor(String key) {
            return ((Interpreter.ColorPrimitive) getOrError(key)).value;
        }

        public Color getColor(String key, Color defaultVal) {
            if (containsKey(key)) return getColor(key);
            return defaultVal;
        }

        public String getString(String key) {
            return ((Interpreter.StringPrimitive) getOrError(key)).value;
        }

        public String getString(String key, String defaultVal) {
            if (containsKey(key)) return getString(key);
            return defaultVal;
        }

        private Interpreter.Primitive getOrError(String key) {
            if (containsKey(key)) return get(key);
            throw new RuntimeException("Optional field '" + key + "' was treated as required.");
        }
    }
}
