package io.github.mrmaxguns.freepapermaps;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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

    public List<Element> getDirectChildElementsByTagName(Document doc, String tagName) throws UserInputException {
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
        List<Element> results = getDirectChildElementsByTagName(doc, tagName);
        if (results.isEmpty()) {
            error("Could not find required tag '" + tagName + "'.");
        } else if (results.size() > 1) {
            error("The tag '" + tagName + "' is supposed to appear only once, but it was found " + results.size() +
                  " times.");
        }
        return results.get(0);
    }

    public String getAttributeValue(Node node, String attributeName) throws UserInputException {
        Node attribute = node.getAttributes().getNamedItem(attributeName);
        if (attribute == null) {
            error("Found tag that does not have required attribute '" + attributeName + "'.");
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
