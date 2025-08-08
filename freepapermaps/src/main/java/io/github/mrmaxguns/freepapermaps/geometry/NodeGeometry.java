package io.github.mrmaxguns.freepapermaps.geometry;

import io.github.mrmaxguns.freepapermaps.osm.Node;
import io.github.mrmaxguns.freepapermaps.projections.Projection;
import io.github.mrmaxguns.freepapermaps.rendering.Scaler;

import java.util.Objects;


/** Represents OSM Node geometry similarly to a SimpleFeatures Point. */
public class NodeGeometry extends Geometry {
    /** The node's position coordinate. Never <code>null</code>. */
    private final Coordinate<?> position;

    /** Constructs a <code>NodeGeometry</code> from a non-<code>null</code> position. */
    public NodeGeometry(Coordinate<?> position) {
        this.position = Objects.requireNonNull(position);
    }

    /** Constructs a <code>NodeGeometry</code> from a <code>Node</code>, maintaining its coordinate system. */
    public static NodeGeometry fromOSM(Node node) {
        return fromOSM(node, null, null);
    }

    /**
     * Constructs a <code>NodeGeometry</code> from a <code>Node</code>, projecting and scaling it.
     * <p>
     * This is the preferred way to construct <code>NodeGeometry</code> from OSM data because scaling prevents "warping"
     * on algorithms that operate in Euclidean space. If <em>either</em> <code>projection</code> or <code>scaler</code>s
     * <code>null</code>, the <code>NodeGeometry</code> is created from the node's coordinate as-is, with no
     * scaling/projection applied.
     */
    public static NodeGeometry fromOSM(Node node, Projection projection, Scaler scaler) {
        Coordinate<?> position;
        if (projection != null && scaler != null) {
            position = scaler.scale(projection.project(node.getPosition()));
        } else {
            position = node.getPosition();
        }

        NodeGeometry result = new NodeGeometry(position);
        result.getTags().putAll(node.getTags());
        return result;
    }

    public Coordinate<?> getPosition() {
        return position;
    }

    @Override
    public boolean isValid() {
        return Double.isFinite(getPosition().getX()) && Double.isFinite(getPosition().getY());
    }

    @Override
    public boolean isCompletelyValid() {
        return true;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof NodeGeometry)) { return false; }
        return equals((NodeGeometry) other);
    }

    /** Returns true if this node is "on top of" another node, accounting for slight floating-point error. */
    public boolean equals(NodeGeometry other) {
        return position.almostEquals(other.getPosition());
    }

    /** Formats the coordinate's position like this: "x y". " */
    public String coordsToString() {
        return position.getLon() + " " + position.getLat();
    }

    /** Formats the coordinate as a WellKnownText point. */
    @Override
    public String toString() {
        return "POINT (" + coordsToString() + ")";
    }
}
