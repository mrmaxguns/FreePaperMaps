package io.github.mrmaxguns.freepapermaps.styling;

import java.util.Objects;


public class Selector {
    private final TagQuery query;
    private String id;

    public Selector(String id, TagQuery query) {
        this.id = Objects.requireNonNull(id);
        this.query = query;
    }

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
