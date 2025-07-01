package io.github.mrmaxguns.freepapermaps;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import java.io.File;
import java.io.IOException;

public class OSM {
    public OSM(String file_name) throws ParserConfigurationException, UserInputException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

        Document doc;
        try {
            doc = builder.parse(new File(file_name));
        } catch (SAXException e) {
            throw new UserInputException("Malformed XML detected in input file:\n" + e.getMessage());
        } catch (IOException e) {
            throw new UserInputException("File '" + file_name + "' could not be opened. Does it exist?");
        }

        doc.getDocumentElement().normalize();
    }
}

