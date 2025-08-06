package io.github.mrmaxguns.freepapermaps.geometry;

import io.github.mrmaxguns.freepapermaps.osm.TagList;


public abstract class Geometry {
    private final TagList tags = new TagList();

    public TagList getTags() {
        return tags;
    }

    public abstract boolean isValid();
}
