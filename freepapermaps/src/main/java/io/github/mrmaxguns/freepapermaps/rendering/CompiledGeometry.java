package io.github.mrmaxguns.freepapermaps.rendering;

import java.awt.*;

public abstract class CompiledGeometry {
    public abstract void render(Graphics2D g2d, Scaler scaler);
}
