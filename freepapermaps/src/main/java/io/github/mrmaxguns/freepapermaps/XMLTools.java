package io.github.mrmaxguns.freepapermaps;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class XMLTools {
    private final String fileIdentifier;

    public XMLTools() {
        fileIdentifier = null;
    }

    public XMLTools(String fileIdentifier) {
        this.fileIdentifier = fileIdentifier;
    }

    public List<Element> getDirectChildElementsByTagName(Document doc, String tagName) {
        ArrayList<Element> results = new ArrayList<>();
        for (Node childNode = doc.getDocumentElement().getFirstChild(); childNode.getNextSibling() != null;
             childNode = childNode.getNextSibling()) {
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) childNode;
                if (childElement.getTagName().equals(tagName)) {
                    results.add(childElement);
                }
            }
        }
        return results;
    }

    public Element getSingleChildElementByTagName(Document doc, String tagName) throws UserInputException {
        return getSingleChildElementByTagName(doc, tagName, true);
    }

    public Element getSingleChildElementByTagName(Document doc, String tagName, boolean required) throws
            UserInputException {
        List<Element> results = getDirectChildElementsByTagName(doc, tagName);
        if (results.isEmpty()) {
            if (required) {
                throw error("Could not find required tag '" + tagName + "'.");
            }
            return null;
        } else if (results.size() > 1) {
            throw error(
                    "The tag '" + tagName + "' is supposed to appear only once, but it was found " + results.size() +
                  " times.");
        }
        return results.get(0);
    }

    public String getAttributeValue(Element el, String attributeName) throws UserInputException {
        return getAttributeValue(el, attributeName, true);
    }

    public String getAttributeValue(Element el, String attributeName, boolean required) throws UserInputException {
        Node attribute = el.getAttributes().getNamedItem(attributeName);
        if (attribute == null) {
            if (required) {
                throw error("Found tag that does not have required attribute '" + attributeName + "'.");
            }
            return null;
        }
        return attribute.getNodeValue();
    }

    public double getRequiredAttributeValueDouble(Element el, String attributeName) throws UserInputException {
        String rawValue = getAttributeValue(el, attributeName);

        double value;
        try {
             value = Double.parseDouble(rawValue);
        } catch (NumberFormatException e) {
            throw error("Found attribute '" + attributeName + "' of tag '" + el.getTagName() + "' that has value '"
                    + rawValue + "', that could not be parsed as a decimal number.");
        }

        return value;
    }

    public long getRequiredAttributeValueLong(Element el, String attributeName) throws UserInputException {
        String rawValue = getAttributeValue(el, attributeName);

        long value;
        try {
            value = Long.parseLong(rawValue);
        } catch (NumberFormatException e) {
            throw error("Found attribute '" + attributeName + "' of tag '" + el.getTagName() + "' that has value '"
                    + rawValue + "', that could not be parsed as an integer.");
        }

        return value;
    }

    /** Returns a Color parsed from colorName, which should be a 24-bit integer representation. */
    public Color parseColor(String colorName) throws UserInputException {
        try {
            return Color.decode(colorName);
        } catch (NumberFormatException e) {
            throw error("Invalid color '" + colorName + "'.");
        }
    }

    public Stroke parseStroke(String rawThickness) throws UserInputException {
        float thickness;
        try {
            thickness = Float.parseFloat(rawThickness);
        } catch (NumberFormatException e) {
            throw error("Invalid thickness value '" + rawThickness + "'.");
        }
        return new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    }

    private UserInputException error(String message) {
        if (fileIdentifier != null) {
            return new UserInputException("XML Error: " + fileIdentifier + ": " + message);
        }
        return new UserInputException("XML Error: " + message);
    }
}
