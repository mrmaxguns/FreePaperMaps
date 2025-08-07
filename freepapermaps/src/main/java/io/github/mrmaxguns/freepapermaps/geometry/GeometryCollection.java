package io.github.mrmaxguns.freepapermaps.geometry;

import io.github.mrmaxguns.freepapermaps.UserInputException;
import io.github.mrmaxguns.freepapermaps.osm.Node;
import io.github.mrmaxguns.freepapermaps.osm.OSM;
import io.github.mrmaxguns.freepapermaps.osm.Relation;
import io.github.mrmaxguns.freepapermaps.osm.Way;
import io.github.mrmaxguns.freepapermaps.projections.Projection;
import io.github.mrmaxguns.freepapermaps.rendering.Scaler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class GeometryCollection {
    public static final String[] DEFAULT_MULTIPOLYGON_TYPES = { "multipolygon", "boundary" };

    private final Map<Long, NodeGeometry> nodesById;
    private final Map<Long, WayGeometry> waysById;
    private final Map<Long, PolygonGeometry> polygonsById;
    private final Map<Long, RelationGeometry> relationsById;

    public GeometryCollection(Map<Long, NodeGeometry> nodesById, Map<Long, WayGeometry> waysById,
                              Map<Long, PolygonGeometry> polygonsById, Map<Long, RelationGeometry> relationsById) {
        this.nodesById = Collections.unmodifiableMap(nodesById);
        this.waysById = Collections.unmodifiableMap(waysById);
        this.polygonsById = Collections.unmodifiableMap(polygonsById);
        this.relationsById = Collections.unmodifiableMap(relationsById);
    }

    public static GeometryCollection fromOSM(OSM osm, Projection projection, Scaler scaler) throws UserInputException {
        Map<Long, NodeGeometry> nodesById = new HashMap<>();
        for (Node n : osm.getNodes()) {
            nodesById.put(n.getId(), NodeGeometry.fromOSM(n, projection, scaler));
        }

        Map<Long, WayGeometry> waysById = new HashMap<>();
        Map<Long, PolygonGeometry> polygonsById = new HashMap<>();
        for (Way w : osm.getWays()) {
            WayGeometry geometry = WayGeometry.fromOSM(osm, w, projection, scaler);
            waysById.put(w.getId(), geometry);

            if (geometry.isClosed()) {
                polygonsById.put(w.getId(), PolygonGeometry.fromOSM(osm, w, projection, scaler));
            }
        }

        Map<Long, RelationGeometry> relationsById = new HashMap<>();
        for (Relation r : osm.getRelations()) {
            relationsById.put(r.getId(), RelationGeometry.fromOSMUnresolved(r));
        }

        // Resolve relations
        RelationGeometry.Registry registry = new RelationGeometry.Registry(nodesById, waysById, polygonsById,
                                                                           relationsById);
        for (RelationGeometry r : relationsById.values()) {
            r.resolve(registry);
        }

        return new GeometryCollection(nodesById, waysById, polygonsById, relationsById);
    }

    public Map<Long, NodeGeometry> getNodesById() {
        return nodesById;
    }

    public Map<Long, WayGeometry> getWaysById() {
        return waysById;
    }

    public Map<Long, PolygonGeometry> getPolygonsById() {
        return polygonsById;
    }

    public Map<Long, RelationGeometry> getRelationsById() {
        return relationsById;
    }
}
