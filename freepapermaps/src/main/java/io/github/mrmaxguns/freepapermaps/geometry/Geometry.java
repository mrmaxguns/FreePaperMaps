package io.github.mrmaxguns.freepapermaps.geometry;

import io.github.mrmaxguns.freepapermaps.osm.TagList;


/** Represents a geometry loosely based on both the OSM and SimpleFeatures specifications. */
public abstract class Geometry {
    /** A list of OSM tags associated with this <code>Geometry</code>. */
    private final TagList tags = new TagList();

    public TagList getTags() {
        return tags;
    }

    /** Returns true if the <code>Geometry</code> passes basic validation checks that are essential for proper
     * rendering. */
    public abstract boolean isValid();

    /**
     * Returns true if the <code>Geometry</code> passes more computationally expensive validation checks that may not
     * be necessary for rendering and/or handle rare edge cases.
     */
    public abstract boolean isCompletelyValid();
}
