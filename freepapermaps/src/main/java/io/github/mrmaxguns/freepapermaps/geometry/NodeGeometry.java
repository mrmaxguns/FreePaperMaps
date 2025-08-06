package io.github.mrmaxguns.freepapermaps.geometry;

import io.github.mrmaxguns.freepapermaps.osm.Node;
import io.github.mrmaxguns.freepapermaps.projections.DummyProjection;
import io.github.mrmaxguns.freepapermaps.projections.Projection;
import io.github.mrmaxguns.freepapermaps.rendering.Scaler;

import java.util.Objects;


public class NodeGeometry extends Geometry {
    private final Coordinate position;

    public NodeGeometry(Coordinate position) {
        this.position = Objects.requireNonNull(position);
    }

    public static NodeGeometry fromOSM(Node node) {
        return fromOSM(node, new DummyProjection(), new Scaler(1));
    }

    public static NodeGeometry fromOSM(Node node, Projection projection, Scaler scaler) {
        Coordinate position = scaler.scale(projection.project(node.getPosition()));
        NodeGeometry result = new NodeGeometry(position);
        result.getTags().putAll(node.getTags());
        return result;
    }

    public Coordinate getPosition() {
        return position;
    }

    public String coordsToString() {
        return position.getLon() + " " + position.getLat();
    }

    public boolean isValid() {
        return true;
    }

    public boolean equals(NodeGeometry other) {
        return position.equals(other.getPosition());
    }

    public String toString() {
        return "POINT (" + coordsToString() + ")";
    }
}
