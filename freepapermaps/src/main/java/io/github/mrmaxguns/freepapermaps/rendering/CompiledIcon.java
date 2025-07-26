package io.github.mrmaxguns.freepapermaps.rendering;

import io.github.mrmaxguns.freepapermaps.projections.ProjectedCoordinate;
import io.github.mrmaxguns.freepapermaps.styling.IconLayer;

import java.awt.*;


public class CompiledIcon extends CompiledGeometry {
    private final ProjectedCoordinate position;
    private final IconLayer style;

    public CompiledIcon(ProjectedCoordinate position, IconLayer style) {
        this.position = position;
        this.style = style;
    }

    @Override
    public void render(Graphics2D g2d, Scaler scaler) {
        style.getIcon().renderCentered(g2d, scaler.scale(position), style.getTargetWidth(), style.getTargetHeight());
    }
}
