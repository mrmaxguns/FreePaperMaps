package io.github.mrmaxguns.freepapermaps.geometry;

import io.github.mrmaxguns.freepapermaps.osm.Relation;

import java.util.Map;


public class RelationGeometry extends Geometry {
    boolean resolved = false;

    public static RelationGeometry fromOSMUnresolved(Relation relation) {
        return null;
    }

    public void resolve(Registry registry) {

    }

    @Override
    public boolean isValid() {
        return true;
    }

    public record Registry(Map<Long, NodeGeometry> nodesById, Map<Long, WayGeometry> waysById,
                           Map<Long, PolygonGeometry> polygonsById, Map<Long, RelationGeometry> relationsById) {}
}
