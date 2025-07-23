package io.github.mrmaxguns.freepapermaps;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.*;


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

    public double getAttributeValueDouble(Element el, String attributeName) throws UserInputException {
        return getAttributeValueDouble(el, attributeName, true).orElseThrow();
    }

    public OptionalDouble getAttributeValueDouble(Element el, String attributeName, boolean required) throws
            UserInputException {
        String rawValue = getAttributeValue(el, attributeName, required);

        if (rawValue == null) {
            return OptionalDouble.empty();
        }

        double value;
        try {
             value = Double.parseDouble(rawValue);
        } catch (NumberFormatException e) {
            throw error("Found attribute '" + attributeName + "' of tag '" + el.getTagName() + "' that has value '"
                    + rawValue + "', that could not be parsed as a decimal number.");
        }

        return OptionalDouble.of(value);
    }

    public int getAttributeValueInt(Element el, String attributeName) throws UserInputException {
        return getAttributeValueInt(el, attributeName, true).orElseThrow();
    }

    public OptionalInt getAttributeValueInt(Element el, String attributeName, boolean required) throws
            UserInputException {
        String rawValue = getAttributeValue(el, attributeName, required);

        if (rawValue == null) {
            return OptionalInt.empty();
        }

        int value;
        try {
            value = Integer.parseInt(rawValue);
        } catch (NumberFormatException e) {
            throw error("Found attribute '" + attributeName + "' of tag '" + el.getTagName() + "' that has value '" +
                        rawValue + "', that could not be parsed as an integer.");
        }

        return OptionalInt.of(value);
    }

    public long getAttributeValueLong(Element el, String attributeName) throws UserInputException {
        return getAttributeValueLong(el, attributeName, true).orElseThrow();
    }

    public OptionalLong getAttributeValueLong(Element el, String attributeName, boolean required) throws
            UserInputException {
        String rawValue = getAttributeValue(el, attributeName, required);

        if (rawValue == null) {
            return OptionalLong.empty();
        }

        long value;
        try {
            value = Long.parseLong(rawValue);
        } catch (NumberFormatException e) {
            throw error("Found attribute '" + attributeName + "' of tag '" + el.getTagName() + "' that has value '"
                    + rawValue + "', that could not be parsed as an integer.");
        }

        return OptionalLong.of(value);
    }

    public UserInputException error(String message) {
        if (fileIdentifier != null) {
            return new UserInputException("XML Error: " + fileIdentifier + ": " + message);
        }
        return new UserInputException("XML Error: " + message);
    }
}
