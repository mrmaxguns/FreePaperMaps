package io.github.mrmaxguns.freepapermaps.geometry;

import io.github.mrmaxguns.freepapermaps.UserInputException;
import io.github.mrmaxguns.freepapermaps.osm.OSM;
import io.github.mrmaxguns.freepapermaps.osm.Relation;
import io.github.mrmaxguns.freepapermaps.osm.Way;
import io.github.mrmaxguns.freepapermaps.projections.DummyProjection;
import io.github.mrmaxguns.freepapermaps.projections.Projection;
import io.github.mrmaxguns.freepapermaps.rendering.Scaler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class PolygonGeometry extends Geometry {
    private static final String OUTER_ROLE = "outer";
    private static final String INNER_ROLE = "inner";

    private final WayGeometry exteriorRing;
    private final List<WayGeometry> interiorRings;

    public PolygonGeometry(WayGeometry exteriorRing, List<WayGeometry> interiorRings) {
        if (exteriorRing == null || interiorRings == null || interiorRings.contains(null)) {
            throw new IllegalArgumentException("Exterior and interior rings must not be null.");
        }

        if (!exteriorRing.isClosed()) {
            throw new IllegalArgumentException("The polygon's exterior ring is not closed.");
        }

        if (!interiorRings.stream().allMatch(WayGeometry::isClosed)) {
            throw new IllegalArgumentException("One or more of the interior rings given to the polygon are unclosed.");
        }

        this.exteriorRing = exteriorRing;
        this.interiorRings = Collections.unmodifiableList(interiorRings);
    }

    public static PolygonGeometry fromOSM(OSM osm, Way way) throws UserInputException {
        WayGeometry exteriorRing = WayGeometry.fromOSM(osm, way);

        if (exteriorRing.isClosed()) {
            return new PolygonGeometry(exteriorRing, new ArrayList<>());
        }

        return null;
    }

    public static List<PolygonGeometry> fromOSM(OSM osm, Relation relation) throws UserInputException {
        return fromOSM(osm, relation, new DummyProjection(), new Scaler(1));
    }

    // https://wiki.openstreetmap.org/wiki/Relation:multipolygon/Algorithm
    public static List<PolygonGeometry> fromOSM(OSM osm, Relation relation, Projection projection, Scaler scaler) throws
            UserInputException {
        List<OSM.RelationMember> relationMembers = osm.getRelationMembers(relation);

        WayGeometryList wayGeometries = new WayGeometryList();

        for (OSM.RelationMember member : relationMembers) {
            if (member.element() instanceof Way) {
                wayGeometries.add(member.role(), WayGeometry.fromOSM(osm, (Way) member.element(), projection, scaler));
            }
        }

        if (wayGeometries.isEmpty()) {
            // Prerequisite failed: relation must have at least one way member
            return null;
        }

        WayGeometryList rings = assignRings(wayGeometries);
        if (rings == null) { return null; }

        // Theoretically, we should also check for intersections between polygons, but that is a pretty complicated
        // piece of computational geometry with minimal benefit to the application.
        return groupRings(rings);
    }

    private static WayGeometryList assignRings(WayGeometryList wayGeometries) throws UserInputException {
        WayGeometryList unassignedGeometries = wayGeometries.copy();
        WayGeometryList rings = new WayGeometryList();

        WayGeometryList.RoleWayGeometryPair firstGeometry = unassignedGeometries.popFirst();
        while (!unassignedGeometries.isEmpty()) {
            WayGeometryList.RoleWayGeometryPair ring = findRing(unassignedGeometries, firstGeometry);
            if (ring == null) {
                return null;
            }
            rings.add(ring);
        }

        return rings;
    }

    // Single-solution backtracking algorithm based on https://en.wikipedia.org/wiki/Backtracking#Pseudocode
    private static WayGeometryList.RoleWayGeometryPair findRing(WayGeometryList geometries,
                                                                WayGeometryList.RoleWayGeometryPair currentGeometry) {
        WayGeometry way = currentGeometry.wayGeometry();

        // reject(P, c)
        if (!way.isValid()) {
            return null;
        }

        // accept(P, c)
        if (way.isClosed()) {
            return currentGeometry;
        }

        WayGeometryList candidates = getCandidates(geometries, way);
        WayGeometryList.RoleWayGeometryPair possibleSolution = candidates.popFirst();

        while (possibleSolution != null) {
            if (!possibleSolution.role().equals(currentGeometry.role())) {
                // Technically, we could ignore roles and combine anyway, but we assume that roles are correct, speeding
                // up the algorithm. Furthermore, this is a good way to catch errors in multipolygons since
                // multipolygons
                // with mismatched role rings will not render properly.
                continue;
            }

            WayGeometryList modifiedGeometries = geometries.copy();
            modifiedGeometries.remove(possibleSolution);
            WayGeometryList.RoleWayGeometryPair combination = new WayGeometryList.RoleWayGeometryPair(
                    currentGeometry.role(), currentGeometry.wayGeometry().combine(possibleSolution.wayGeometry()));
            WayGeometryList.RoleWayGeometryPair deeperSolution = findRing(modifiedGeometries, combination);

            // Bubble up a deeper solution
            if (deeperSolution != null) {
                return deeperSolution;
            }

            possibleSolution = candidates.popFirst();
        }

        // No solution found in this branch
        return null;
    }

    private static WayGeometryList getCandidates(WayGeometryList geometries, WayGeometry way) {
        WayGeometryList result = new WayGeometryList();

        for (WayGeometryList.RoleWayGeometryPair g : geometries.getWayGeometryPairs()) {
            if (g.wayGeometry().canBeCombined(way)) {
                result.add(g);
            }
        }

        return result;
    }

    private static List<PolygonGeometry> groupRings(WayGeometryList rings) {
        boolean[][] contains = new boolean[rings.size()][rings.size()];
        Boolean[] ringIsUsed = new Boolean[rings.size()];

        List<PolygonGeometry> result = new ArrayList<>();

        for (int i = 0; i < rings.size(); ++i) {
            for (int j = 0; j < rings.size(); ++j) {
                if (i == j) {
                    contains[i][j] = false;
                }

                contains[i][j] = rings.getGeometry(i).contains(rings.getGeometry(j));
            }
        }

        while (Arrays.asList(ringIsUsed).contains(false)) {
            String exteriorRingRole = null;
            WayGeometry exteriorRing = null;
            int exteriorRingIndex = 0;

            for (int i = 0; i < rings.size(); ++i) {
                if (ringIsUsed[i]) { continue; }

                if (!isContainedByRing(contains, ringIsUsed, i)) {
                    exteriorRingRole = rings.getRole(i);
                    exteriorRing = rings.getGeometry(i);
                    exteriorRingIndex = i;
                    break;
                }
            }

            if (exteriorRing == null) {
                // Could not find an outer ring that is not contained by another ring
                return null;
            }

            if (!exteriorRingRole.equals(OUTER_ROLE)) {
                // The outer ring is marked with something other than outer, which could indicate an error
                return null;
            }

            // Add the parent as the first element
            ringIsUsed[exteriorRingIndex] = true;

            // Get inner rings
            List<WayGeometry> interiorRings = new ArrayList<>();
            for (int i = 0; i < rings.size(); ++i) {
                if (ringIsUsed[i] || !contains[exteriorRingIndex][i]) { continue; }
                if (!isContainedByRing(contains, ringIsUsed, i)) {
                    // This ring is a hole
                    if (rings.getRole(i).equals(INNER_ROLE)) {
                        // An inner ring is marked with something other than inner, which could indicate an error
                        return null;
                    }

                    // Add the hole
                    interiorRings.add(rings.getGeometry(i));
                    ringIsUsed[i] = true;
                }
            }

            // Combine holes that touch
            combineInnerHoles(interiorRings);

            PolygonGeometry finalizedPolygon = new PolygonGeometry(exteriorRing, interiorRings);

            if (!finalizedPolygon.isValid()) {
                // Invalid polygon
                return null;
            }

            result.add(finalizedPolygon);
        }

        return result;
    }

    private static boolean isContainedByRing(boolean[][] contains, Boolean[] ringIsUsed, int i) {
        for (int j = 0; j < contains.length; ++j) {
            if (i == j || ringIsUsed[j]) { continue; }
            if (contains[j][i]) {
                return true;
            }
        }
        return false;
    }

    private static void combineInnerHoles(List<WayGeometry> polygon) {

    }

    public boolean isValid() {
        if (!exteriorRing.isValid()) {
            return false;
        }

        for (WayGeometry geometry : interiorRings) {
            if (!geometry.isValid()) {
                return false;
            }
        }

        // TODO: More validity checks.
        // It is an open question whether more validity checks are needed since we are only rendering the multipolygon,
        // and not doing any complicated GIS things.
        return true;
    }

    public WayGeometry getExteriorRing() {
        return exteriorRing;
    }

    public List<WayGeometry> getInteriorRings() {
        return interiorRings;
    }

    private static class WayGeometryList {
        private final List<RoleWayGeometryPair> wayGeometries;

        public WayGeometryList() {
            wayGeometries = new ArrayList<>();
        }

        public WayGeometryList(List<RoleWayGeometryPair> wayGeometries) {
            this.wayGeometries = List.copyOf(wayGeometries);
        }

        public WayGeometryList(WayGeometryList list) {
            this(list.wayGeometries);
        }

        public List<RoleWayGeometryPair> getWayGeometryPairs() {
            return List.copyOf(wayGeometries);
        }

        public List<WayGeometry> getWayGeometries() {
            return wayGeometries.stream().map(RoleWayGeometryPair::wayGeometry).toList();
        }

        public int size() {
            return wayGeometries.size();
        }

        public void add(RoleWayGeometryPair roleWayGeometryPair) {
            wayGeometries.add(roleWayGeometryPair);
        }

        public void add(String role, WayGeometry wayGeometry) {
            add(new RoleWayGeometryPair(role, wayGeometry));
        }

        public WayGeometry getGeometry(int i) {
            return wayGeometries.get(i).wayGeometry();
        }

        public String getRole(int i) {
            return wayGeometries.get(i).role();
        }

        public boolean remove(RoleWayGeometryPair roleWayGeometryPair) {
            return wayGeometries.remove(roleWayGeometryPair);
        }

        public boolean isEmpty() {
            return wayGeometries.isEmpty();
        }

        public WayGeometryList copy() {
            return new WayGeometryList(this);
        }

        public RoleWayGeometryPair popFirst() {
            if (wayGeometries.isEmpty()) {
                return null;
            }

            RoleWayGeometryPair result = wayGeometries.get(0);
            wayGeometries.remove(0);
            return result;
        }

        public record RoleWayGeometryPair(String role, WayGeometry wayGeometry) {}
    }
}
