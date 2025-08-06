package io.github.mrmaxguns.freepapermaps.geometry;

import io.github.mrmaxguns.freepapermaps.UserInputException;
import io.github.mrmaxguns.freepapermaps.osm.Node;
import io.github.mrmaxguns.freepapermaps.osm.OSM;
import io.github.mrmaxguns.freepapermaps.osm.Way;
import io.github.mrmaxguns.freepapermaps.projections.DummyProjection;
import io.github.mrmaxguns.freepapermaps.projections.Projection;
import io.github.mrmaxguns.freepapermaps.rendering.Scaler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class WayGeometry extends Geometry {
    private final List<NodeGeometry> nodes;

    public WayGeometry(List<NodeGeometry> nodes) {
        if (nodes == null) {
            throw new IllegalArgumentException("Nodes list cannot be null");
        }

        if (nodes.size() < 2) {
            throw new IllegalArgumentException("Way must have at least 2 nodes");
        }

        this.nodes = Collections.unmodifiableList(nodes);
    }

    public static WayGeometry fromOSM(OSM osm, Way way) throws UserInputException {
        return fromOSM(osm, way, new DummyProjection(), new Scaler(1));
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

    public boolean isClosed() {
        return nodes.get(0).equals(nodes.get(nodes.size() - 1));
    }

    public boolean isValid() {
        // Test node validity
        for (NodeGeometry node : nodes) {
            if (!node.isValid()) { return false; }
        }

        // Test for self-intersections. Currently, a rudimentary O(n^2) algorithm, which is prob bad for large
        // multipolygons.
        for (int i = 0; i < nodes.size(); ++i) {
            for (int j = 0; j < nodes.size(); ++j) {
                if (i == j) {
                    continue;
                }

                LineSegment s1 = new LineSegment(nodes.get(i).getPosition(),
                                                 nodes.get((i + 1) % nodes.size()).getPosition());
                LineSegment s2 = new LineSegment(nodes.get(j).getPosition(),
                                                 nodes.get((j + 1) % nodes.size()).getPosition());
                if (s1.intersects(s2)) {
                    return false;
                }
            }
        }
        return true;
    }

    public NodeGeometry getFirstNode() {
        return nodes.get(0);
    }

    public NodeGeometry getLastNode() {
        return nodes.get(nodes.size() - 1);
    }

    public boolean canBeCombined(WayGeometry other) {
        return (getFirstNode().equals(other.getFirstNode()) || getFirstNode().equals(other.getLastNode()) ||
                getLastNode().equals(other.getFirstNode()) || getLastNode().equals(other.getLastNode()));
    }

    public WayGeometry combine(WayGeometry other) {
        if (isClosed() || other.isClosed()) {
            throw new IllegalArgumentException("Cannot combine to a closed Way Geometry.");
        }

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

        if (tl.equals(of) || tf.equals(ol)) {
            // Ways form a single line, no reversal needed
            otherNodes.remove(0);
            thisNodes.addAll(otherNodes);
            return new WayGeometry(thisNodes);
        }

        if (tl.equals(ol) || tf.equals(of)) {
            // Ways form a single line, but one needs to be reversed
            Collections.reverse(otherNodes);
            thisNodes.addAll(otherNodes);
            return new WayGeometry(thisNodes);
        }

        throw new RuntimeException("Incorrect control flow.");
    }

    // https://alienryderflex.com/polygon/
    public boolean contains(NodeGeometry node) {
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

    public boolean contains(WayGeometry other) {
        if (!isClosed() || !other.isClosed()) {
            throw new IllegalArgumentException("Cannot test containment with an unclosed way.");
        }

        // Check node containment
        for (NodeGeometry n : nodes) {
            if (!contains(n)) {
                return false;
            }
        }

        // Check edge intersection
        for (int i = 0; i < nodes.size(); ++i) {
            for (int j = 0; j < other.nodes.size(); ++j) {
                LineSegment s1 = new LineSegment(nodes.get(i).getPosition(),
                                                 nodes.get((i + 1) % nodes.size()).getPosition());
                LineSegment s2 = new LineSegment(nodes.get(j).getPosition(),
                                                 nodes.get((j + 1) % nodes.size()).getPosition());

                if (s1.intersects(s2)) {
                    return false;
                }
            }
        }

        return true;
    }

    public String toString() {
        StringBuilder result = new StringBuilder("LINESTRING (");

        for (int i = 0; i < nodes.size(); ++i) {
            if (i != 0) {
                result.append(", ");
            }
            result.append(nodes.get(i).coordsToString());
        }

        return result.toString();
    }
}
