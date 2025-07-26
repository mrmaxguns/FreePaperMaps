package io.github.mrmaxguns.freepapermaps.styling;

import io.github.mrmaxguns.freepapermaps.UserInputException;
import io.github.mrmaxguns.freepapermaps.XMLTools;
import io.github.mrmaxguns.freepapermaps.osm.Node;
import io.github.mrmaxguns.freepapermaps.osm.OSM;
import io.github.mrmaxguns.freepapermaps.projections.Projection;
import io.github.mrmaxguns.freepapermaps.rendering.CompiledGeometry;
import io.github.mrmaxguns.freepapermaps.rendering.CompiledIcon;
import org.w3c.dom.Element;

import java.awt.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.OptionalDouble;


public class IconLayer extends Layer<Node> {
    public static final double DEFAULT_ICON_WIDTH = 5.0;

    private double targetWidth;
    private double targetHeight;
    private SVGVectorIcon icon;

    public IconLayer(String ref, double targetWidth, double targetHeight, SVGVectorIcon icon) {
        super(ref);
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
        this.icon = icon;
    }

    public static IconLayer fromXML(Element rawLayer) throws UserInputException {
        return fromXML(rawLayer, new XMLTools());
    }

    public static IconLayer fromXML(Element rawLayer, XMLTools xmlTools) throws UserInputException {
        StyleAttributeParser parser = new StyleAttributeParser(xmlTools);
        parser.addStringProperty("ref", true);
        parser.addStringProperty("path", true);
        parser.addColorProperty("fill", false);
        parser.addLengthProperty("width", false);
        parser.addLengthProperty("height", false);
        StyleAttributeParser.ParsedAttributes attributes = parser.parse(rawLayer);

        Color fillColor = attributes.colorProperties.get("fill");
        String hexFillColor;
        if (fillColor == null) {
            hexFillColor = null;
        } else {
            hexFillColor = String.format("#%02x%02x%02x", fillColor.getRed(), fillColor.getGreen(),
                                         fillColor.getBlue());
        }

        String path = attributes.stringProperties.get("path");
        SVGVectorIcon icon;
        try {
            icon = new SVGVectorIcon(new FileInputStream(path), hexFillColor, path);
        } catch (FileNotFoundException e) {
            throw new UserInputException("Could not find icon file " + path + ".");
        }

        double width, height;
        OptionalDouble rawWidth = attributes.lengthProperties.get("width");
        OptionalDouble rawHeight = attributes.lengthProperties.get("height");
        if (rawWidth.isEmpty() && rawHeight.isEmpty()) {
            width = DEFAULT_ICON_WIDTH;
            height = Double.POSITIVE_INFINITY;
        } else if (rawWidth.isEmpty()) {
            width = Double.POSITIVE_INFINITY;
            height = rawHeight.orElseThrow();
        } else if (rawHeight.isEmpty()) {
            width = rawWidth.orElseThrow();
            height = Double.POSITIVE_INFINITY;
        } else {
            width = rawWidth.orElseThrow();
            height = rawHeight.orElseThrow();
        }

        return new IconLayer(attributes.stringProperties.get("ref"), width, height, icon);
    }

    @Override
    public CompiledGeometry compile(Node item, OSM mapData, Projection projection) throws UserInputException {
        return new CompiledIcon(projection.project(item.getPosition()), this);
    }

    public double getTargetWidth() {
        return targetWidth;
    }

    public void setTargetWidth(double targetWidth) {
        this.targetWidth = targetWidth;
    }

    public double getTargetHeight() {
        return targetHeight;
    }

    public void setTargetHeight(double targetHeight) {
        this.targetHeight = targetHeight;
    }

    public SVGVectorIcon getIcon() {
        return icon;
    }

    public void setIcon(SVGVectorIcon icon) {
        this.icon = icon;
    }
}
