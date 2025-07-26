package io.github.mrmaxguns.freepapermaps.styling;

import io.github.mrmaxguns.freepapermaps.UserInputException;
import io.github.mrmaxguns.freepapermaps.rendering.ScaledCoordinate;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.bridge.*;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;


public class SVGVectorIcon {
    private final GraphicsNode graphicsNode;
    private final Rectangle2D bounds;

    public SVGVectorIcon(InputStream svgInputStream) throws UserInputException {
        this(svgInputStream, null);
    }

    public SVGVectorIcon(InputStream svgInputStream, String overrideColorHex) throws UserInputException {
        this(svgInputStream, overrideColorHex, null);
    }

    public SVGVectorIcon(InputStream svgInputStream, String overrideColorHex, String filename) throws
            UserInputException {
        String errorContext = filename == null ? "" : ("File: " + filename);

        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
        SVGDocument doc;
        try {
            doc = factory.createSVGDocument(null, svgInputStream);
        } catch (IOException e) {
            throw new UserInputException("Error occurred when reading SVG icon." + errorContext);
        }

        // Recolor the SVG if required
        if (overrideColorHex != null) {
            applyColorOverride(doc, overrideColorHex);
        }

        // Load the SVG document
        UserAgent userAgent = new UserAgentAdapter();
        DocumentLoader loader = new DocumentLoader(userAgent);
        BridgeContext ctx = new BridgeContext(userAgent, loader);
        ctx.setDynamicState(BridgeContext.STATIC);

        // Get the DOM as a GraphicsNode
        GVTBuilder builder = new GVTBuilder();
        graphicsNode = builder.build(ctx, doc);

        // Save the SVG bounds so that we can then scale it
        bounds = graphicsNode.getPrimitiveBounds();
        if (bounds == null) {
            throw new UserInputException("SVG has no drawable bounds." + errorContext);
        }
    }

    public void renderCentered(Graphics2D g2d, ScaledCoordinate center, double width, double height) {
        double scaleX = width / bounds.getWidth();
        double scaleY = height / bounds.getHeight();
        double scale = Math.min(scaleX, scaleY);

        double scaledWidth = bounds.getWidth() * scale;
        double scaledHeight = bounds.getHeight() * scale;

        AffineTransform old = g2d.getTransform();

        // Center around (centerX, centerY)
        g2d.translate(center.getX() - (scaledWidth / 2.0), center.getY() - (scaledHeight / 2.0));
        g2d.scale(scale, scale);
        // Account for the fact that the (x, y) of the icon itself might not be (0, 0)
        g2d.translate(-bounds.getX(), -bounds.getY());

        graphicsNode.paint(g2d);
        g2d.setTransform(old);
    }

    private void applyColorOverride(SVGDocument doc, String hexColor) {
        NodeList elements = doc.getElementsByTagName("*");
        for (int i = 0; i < elements.getLength(); i++) {
            Element el = (Element) elements.item(i);

            // Apply only to shape elements
            if (el.hasAttribute("fill") || el.getTagName().matches("path|rect|circle|polygon|line")) {
                el.setAttribute("fill", hexColor);
                el.removeAttribute("stroke"); // Optional: remove strokes if needed
            }
        }
    }
}
