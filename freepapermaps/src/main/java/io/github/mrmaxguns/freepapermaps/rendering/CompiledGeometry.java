package io.github.mrmaxguns.freepapermaps.rendering;

import java.awt.*;


/** Represents a layer of geometry and styling information which can be rendered to a Graphics2D object. */
public abstract class CompiledGeometry {
    /** Render this geometry to g2d, scaling appropriately with scaler. */
    public abstract void render(Graphics2D g2d, Scaler scaler);
}
