package io.github.mrmaxguns.freepapermaps.styling;

import io.github.mrmaxguns.freepapermaps.osm.OSM;
import io.github.mrmaxguns.freepapermaps.projections.Projection;
import io.github.mrmaxguns.freepapermaps.rendering.CompiledGeometry;

public abstract class Layer<T> {
    protected String ref;

    public Layer(String ref) {
        this.ref = ref;
    }

    public abstract CompiledGeometry compile(T item, OSM mapData, Projection projection);

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }
}
