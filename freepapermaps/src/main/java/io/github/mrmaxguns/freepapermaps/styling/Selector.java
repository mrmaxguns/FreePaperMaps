package io.github.mrmaxguns.freepapermaps.styling;

import io.github.mrmaxguns.freepapermaps.osm.TagList;

public abstract class Selector<T> {
    private String id;
    private final TagList tags;

    public Selector(String id) {
        this.id = id;
        this.tags = new TagList();
    }

    public abstract boolean matches(T val);

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TagList getTags() {
        return tags;
    }
}
