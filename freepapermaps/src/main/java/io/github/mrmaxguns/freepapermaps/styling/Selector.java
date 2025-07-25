package io.github.mrmaxguns.freepapermaps.styling;

import java.util.Objects;


/**
 * A Selector is used to specify the geometry that a given layer applies to. Currently, Selectors are only capable of
 * specifying an AND combination of tags that an element must have to be part of the layer. The type parameter T is used
 * to specify whether the selector applies to Nodes or Ways.
 */
public abstract class Selector<T> {
    private final TagQuery query;
    /** A unique identifier for the selector, so that a layer can refer to it. Never <code>null</code>. */
    private String id;

    public Selector(String id, TagQuery query) {
        this.id = Objects.requireNonNull(id);
        this.query = query;
    }

    /** Returns true whether the given geometry element matches this selector's requirements. */
    public abstract boolean matches(T val);

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = Objects.requireNonNull(id);
    }

    public TagQuery getQuery() {
        return query;
    }
}
