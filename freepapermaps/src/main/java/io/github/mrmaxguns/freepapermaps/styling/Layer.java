package io.github.mrmaxguns.freepapermaps.styling;

import io.github.mrmaxguns.freepapermaps.UserInputException;
import io.github.mrmaxguns.freepapermaps.osm.OSM;
import io.github.mrmaxguns.freepapermaps.projections.Projection;
import io.github.mrmaxguns.freepapermaps.rendering.CompiledGeometry;

import java.util.Objects;


/**
 * A Layer applies to a certain type of OSM geometry: a Node or Way, and this relationship is enforced with type
 * parameter T. A layer is associated with a selector of the same type (Node or Way). While a selector describes the
 * geometry which the layer uses, the layer itself encodes styling information used to actually render the geometry.
 */
public abstract class Layer<T> {
    /** A reference to this layer's selector. Never <code>null</code>. */
    protected String ref;

    public Layer(String ref) {
        this.ref = Objects.requireNonNull(ref);
    }

    /**
     * Compiles this layer, which means creating a new CompiledGeometry type that contains both position and styling
     * information.
     */
    public abstract CompiledGeometry compile(T item, OSM mapData, Projection projection) throws UserInputException;

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = Objects.requireNonNull(ref);
    }
}
