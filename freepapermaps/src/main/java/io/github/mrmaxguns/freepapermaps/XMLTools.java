package io.github.mrmaxguns.freepapermaps;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLTools {
    private final String fileIdentifier;

    public XMLTools() {
        fileIdentifier = null;
    }

    public XMLTools(String fileIdentifier) {
        this.fileIdentifier = fileIdentifier;
    }

    public Node getSingleTag(Document doc, String tagName) throws UserInputException {
        NodeList results = doc.getElementsByTagName(tagName);
        if (results.getLength() == 0) {
            error("Could not find required tag '" + tagName + "'.");
        } else if (results.getLength() > 1) {
            error("The tag '" + tagName + "' is supposed to appear only once, but it was found " + results.getLength() + " times.");
        }
        return results.item(0);
    }

    public String getAttributeValue(Node node, String attributeName) throws UserInputException {
        Node attribute = node.getAttributes().getNamedItem(attributeName);
        if (attribute == null) {
            error("Found tag at " + node.getBaseURI() + " that does not have required attribute " + attributeName + "'.");
        }
        //noinspection DataFlowIssue
        return attribute.getNodeValue();
    }

    public String getOptionalAttributeValue(Node node, String attributeName) {
        Node attribute = node.getAttributes().getNamedItem(attributeName);
        if (attribute == null) {
            return null;
        }
        return attribute.getNodeValue();
    }

    public double getAttributeValueDouble(Node node, String attributeName) throws UserInputException {
        String rawValue = getAttributeValue(node, attributeName);

        double value = 0;
        try {
             value = Double.parseDouble(rawValue);
        } catch (NumberFormatException e) {
            error("Found attribute '" + attributeName + "' of tag '" + node.getNodeName() + "' that has value '"
                    + rawValue + "', that could not be parsed as a decimal number.");
        }

        return value;
    }

    public long getAttributeValueLong(Node node, String attributeName) throws UserInputException {
        String rawValue = getAttributeValue(node, attributeName);

        long value = 0;
        try {
            value = Long.parseLong(rawValue);
        } catch (NumberFormatException e) {
            error("Found attribute '" + attributeName + "' of tag '" + node.getNodeName() + "' that has value '"
                    + rawValue + "', that could not be parsed as an integer.");
        }

        return value;
    }

    private void error(String message) throws UserInputException {
        if (fileIdentifier != null) {
            throw new UserInputException("XML Error: " + fileIdentifier + ": " + message);
        }
        throw new UserInputException("XML Error: " + message);
    }
}
