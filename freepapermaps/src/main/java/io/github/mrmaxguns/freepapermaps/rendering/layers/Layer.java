package io.github.mrmaxguns.freepapermaps.rendering.layers;

import io.github.mrmaxguns.freepapermaps.UserInputException;
import io.github.mrmaxguns.freepapermaps.geometry.NodeGeometry;
import io.github.mrmaxguns.freepapermaps.geometry.PolygonGeometry;
import io.github.mrmaxguns.freepapermaps.geometry.RelationGeometry;
import io.github.mrmaxguns.freepapermaps.geometry.WayGeometry;

import java.awt.*;
import java.util.Objects;


public abstract class Layer {
    public static String name;

    private final String ref;

    public Layer(String ref) {
        this.ref = Objects.requireNonNull(ref);
    }

    public void renderNode(Graphics2D g2d, NodeGeometry node) throws UserInputException {
        throw new UserInputException("Layer " + name + " doesn't support node geometry.");
    }

    public void renderWay(Graphics2D g2d, WayGeometry way) throws UserInputException {
        throw new UserInputException("Layer " + name + " doesn't support way geometry.");
    }

    public void renderPolygon(Graphics2D g2d, PolygonGeometry polygon) throws UserInputException {
        throw new UserInputException("Layer " + name + " doesn't support polygon geometry.");
    }

    public void renderRelation(Graphics2D g2d, RelationGeometry relation) throws UserInputException {
        throw new UserInputException("Layer " + name + " doesn't support relation geometry.");
    }

    public String getRef() {
        return ref;
    }
}
