package io.github.mrmaxguns.freepapermaps.geometry;

import io.github.mrmaxguns.freepapermaps.UserInputException;
import io.github.mrmaxguns.freepapermaps.osm.Node;
import io.github.mrmaxguns.freepapermaps.osm.OSM;
import io.github.mrmaxguns.freepapermaps.osm.Way;
import io.github.mrmaxguns.freepapermaps.projections.Projection;
import io.github.mrmaxguns.freepapermaps.rendering.Scaler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/** Represents OSM way geometry similarly to a SimpleFeatures LineString. */
public class WayGeometry extends Geometry {
    /** An ordered and IMMUTABLE list of nodes. Never <code>null</code>. */
    private final List<NodeGeometry> nodes;

    // Cached computations
    private transient Boolean cachedIsValid;
    private transient Boolean cachedIsCompletelyValid;
    private transient Boolean cachedIsValidRing;

    public WayGeometry(List<NodeGeometry> nodes) {
        if (nodes == null) {
            throw new NullPointerException("Nodes list cannot be null.");
        }

        // Quick validity check
        if (nodes.size() < 2) {
            throw new IllegalArgumentException("Way must have at least 2 nodes.");
        }

        this.nodes = Collections.unmodifiableList(nodes);
    }

    public static WayGeometry fromOSM(OSM osm, Way way) throws UserInputException {
        return fromOSM(osm, way, null, null);
    }

    public static WayGeometry fromOSM(OSM osm, Way way, Projection projection, Scaler scaler) throws
            UserInputException {
        List<Node> rawNodes = osm.getNodesInWay(way);
        List<NodeGeometry> nodes = rawNodes.stream().map(n -> NodeGeometry.fromOSM(n, projection, scaler)).toList();

        WayGeometry result = new WayGeometry(nodes);
        result.getTags().putAll(way.getTags());

        return result;
    }

    public List<NodeGeometry> getNodes() {
        return nodes;
    }

    /** Returns the first node in this way. */
    public NodeGeometry getFirstNode() {
        return nodes.get(0);
    }

    /** Returns the last node in this way. */
    public NodeGeometry getLastNode() {
        return nodes.get(nodes.size() - 1);
    }

    /** Returns true if the first and last node is at the same position. */
    public boolean isClosed() {
        return getFirstNode().equals(getLastNode());
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof WayGeometry)) { return false; }
        return equals((WayGeometry) other);
    }

    public boolean equals(WayGeometry other) {
        return nodes.equals(other.getNodes());
    }

    /**
     * Tests for:
     * - Child node existence and validity
     */
    @Override
    public boolean isValid() {
        if (cachedIsValid != null) {
            return cachedIsValid;
        }

        // Test node validity
        for (NodeGeometry node : nodes) {
            if (node == null) { return cachedIsValid = false; }
            if (!node.isValid()) { return cachedIsValid = false; }
        }

        return cachedIsValid = true;
    }

    /**
     * Tests for
     * - Basic validity
     * - Degenerate (immediately repeating) segments
     * - Spikes (immediately doubling back)
     */
    @Override
    public boolean isCompletelyValid() {
        if (cachedIsCompletelyValid != null) {
            return cachedIsCompletelyValid;
        }

        // Check basic validity first
        if (!isValid()) {
            return cachedIsCompletelyValid = false;
        }

        // Test node validity (complete)
        for (NodeGeometry node : nodes) {
            if (!node.isCompletelyValid()) { return cachedIsCompletelyValid = false; }
        }

        // Test for degenerate (duplicated) segments (except for start/end points)
        for (int i = 0; i < nodes.size() - 1; ++i) {
            if (nodes.get(i).equals(nodes.get(i + 1))) {
                return cachedIsCompletelyValid = false;
            }
        }

        // Test for spikes (doubling back)
        for (int i = 1; i < nodes.size() - 1; ++i) {
            if (nodes.get(i - 1).equals(nodes.get(i + 1))) {
                return cachedIsCompletelyValid = false;
            }
        }

        return cachedIsCompletelyValid = true;
    }

    /**
     * Tests for
     * - Basic validity
     * - Closedness
     * - Minimum node count
     * - No self-intersections
     */
    public boolean isValidRing() {
        if (cachedIsValidRing != null) {
            return cachedIsValidRing;
        }

        // Basic validity is required for ring validity
        if (!isValid()) {
            return cachedIsValidRing = false;
        }

        // A ring must be closed
        if (!isClosed()) {
            return cachedIsValidRing = false;
        }

        // A ring must have at least four nodes
        if (nodes.size() < 4) {
            return cachedIsValidRing = false;
        }

        // Test for self-intersections
        if (intersects(null)) {
            return cachedIsValidRing = false;
        }

        return cachedIsValidRing = true;
    }

    /**
     * Returns <code>true</code> if this way intersects another way or itself.
     *
     * @param other the way to check for intersection with, or <code>null</code> to check for self-intersections
     * @return <code>true</code> if an intersection was detected
     */
    public boolean intersects(WayGeometry other) {
        // Currently a pretty inefficient O(n^2) algorithm that just checks each segment with all the others.

        boolean checkingSelfIntersection = false;
        if (other == null) {
            other = this;
            checkingSelfIntersection = true;
        }
        List<NodeGeometry> otherNodes = other.getNodes();

        for (int i = 0; i < nodes.size(); ++i) {
            for (int j = 0; j < otherNodes.size(); ++j) {
                // If we are checking for self-intersections, we should ignore checking intersections between a segment
                // and itself.
                if (checkingSelfIntersection && i == j) {
                    continue;
                }

                LineSegment s1 = new LineSegment(nodes.get(i).getPosition(),
                                                 nodes.get((i + 1) % nodes.size()).getPosition());
                LineSegment s2 = new LineSegment(otherNodes.get(j).getPosition(),
                                                 otherNodes.get((j + 1) % otherNodes.size()).getPosition());

                if (s1.isDegenerate() || s2.isDegenerate()) {
                    continue;
                }

                if (s1.intersects(s2)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Returns <code>true</code> if this way shares at least one endpoint with <code>other</code>. */
    public boolean canBeCombined(WayGeometry other) {
        if (isClosed() || other.isClosed()) return false;
        return (getFirstNode().equals(other.getFirstNode()) || getFirstNode().equals(other.getLastNode()) ||
                getLastNode().equals(other.getFirstNode()) || getLastNode().equals(other.getLastNode()));
    }

    /**
     * Returns a new <code>WayGeometry</code> that is a combination of this one and <code>other</code>.
     * <p>
     * Both this way and <code>other</code>  must share at least one endpoint (this can be
     * checked with <code>canBeCombined</code>). This function handles the edge case of the two ways forming a loop.
     * It does not check for topology issues such as self-intersections in the resulting way.
     * <p>
     * In the case when a way must be reversed for the ways to be combined, <code>other</code> is always the way
     * to be reversed.
     * @param other the other way to combine this one with
     * @return a new <code>WayGeometry</code> that is a combination of this and <code>other</code>
     * @throws IllegalArgumentException if the geometries don't share nodes
     */
    public WayGeometry combine(WayGeometry other) {
        if (!canBeCombined(other)) {
            throw new IllegalArgumentException("Cannot directly combine Way Geometries: they don't share nodes.");
        }

        NodeGeometry tf = getFirstNode(), tl = getLastNode(), of = other.getFirstNode(), ol = other.getLastNode();
        List<NodeGeometry> thisNodes = new ArrayList<>(nodes), otherNodes = new ArrayList<>(other.getNodes());

        if (tf.equals(ol) && tl.equals(of)) {
            // Ways form a closed loop, no reversal needed
            otherNodes.remove(0);
            thisNodes.addAll(otherNodes);
            return new WayGeometry(thisNodes);
        }

        if (tf.equals(of) && tl.equals(ol)) {
            // Ways form a closed loop, but one needs to be reversed
            Collections.reverse(otherNodes);
            otherNodes.remove(0);
            thisNodes.addAll(otherNodes);
            return new WayGeometry(thisNodes);
        }

        if (tl.equals(of)) {
            // Ways form a single line, no reversal needed, this way is in front
            otherNodes.remove(0);
            thisNodes.addAll(otherNodes);
            return new WayGeometry(thisNodes);
        }

        if (tf.equals(ol)) {
            // Ways form a single line, no reversal needed, this way is in back
            thisNodes.remove(0);
            otherNodes.addAll(thisNodes);
            return new WayGeometry(otherNodes);
        }

        if (tl.equals(ol)) {
            // Ways form a single line, but one needs to be reversed
            Collections.reverse(otherNodes);
            otherNodes.remove(0);
            thisNodes.addAll(otherNodes);
            return new WayGeometry(thisNodes);
        }

        if (tf.equals(of)) {
            // Ways form a single line, but one needs to be reversed
            Collections.reverse(otherNodes);
            thisNodes.remove(0);
            otherNodes.addAll(thisNodes);
            return new WayGeometry(otherNodes);
        }

        throw new RuntimeException("Incorrect control flow.");
    }

    /**
     * Checks if this way contains a node.
     * @param node the node to check
     * @return <code>true</code> if <code>node</code> lies within this way
     * @throws IllegalArgumentException if this is an unclosed way
     */
    public boolean contains(NodeGeometry node) {
        // Algorithm based on https://alienryderflex.com/polygon/
        if (!isClosed()) {
            throw new IllegalArgumentException("Cannot test containment with an unclosed way.");
        }

        boolean oddNodes = false;

        double x = node.getPosition().getX();
        double y = node.getPosition().getY();

        for (int i = 0, j = nodes.size() - 1; i < nodes.size(); j = i, ++i) {
            double polyXI = nodes.get(i).getPosition().getX();
            double polyYI = nodes.get(i).getPosition().getY();
            double polyXJ = nodes.get(j).getPosition().getX();
            double polyYJ = nodes.get(j).getPosition().getY();

            if ((polyYI < y && polyYJ >= y) || (polyYJ < y && polyYI >= y)) {
                if (polyXI + (((y - polyYI) / (polyYJ - polyYI)) * (polyXJ - polyXI)) < x) {
                    oddNodes = !oddNodes;
                }
            }
        }

        return oddNodes;
    }

    /**
     * Checks if this way contains another way.
     * @param other the way to check
     * @return <code>true</code> if <code>other</code> is completely contained by this way
     * @throws IllegalArgumentException if one or both ways are unclosed
     */
    public boolean contains(WayGeometry other) {
        if (!isClosed() || !other.isClosed()) {
            throw new IllegalArgumentException("Cannot test containment with an unclosed way.");
        }

        // Check node containment
        for (NodeGeometry n : other.getNodes()) {
            if (!contains(n)) {
                return false;
            }
        }

        // Check edge intersection. This must be done, since it is possible for all nodes to be contained, yet edges to
        // extend outside the outer way.
        return !intersects(other);
    }

    /** Formats the way as a WellKnownText LINESTRING. */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("LINESTRING (");

        for (int i = 0; i < nodes.size(); ++i) {
            if (i != 0) {
                result.append(", ");
            }
            result.append(nodes.get(i).coordsToString());
        }

        result.append(")");
        return result.toString();
    }
}
