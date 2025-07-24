package io.github.mrmaxguns.freepapermaps.rendering;


public class DistanceUnitManager extends UnitManager {
    public static final double IN_TO_MM = 25.4;
    public static final double FT_TO_MM = IN_TO_MM * 12;
    public static final double CM_TO_MM = 10;
    public static final double M_TO_MM = 1000;
    public static final double PX_TO_MM = IN_TO_MM / 96;
    public static final double PT_TO_MM = 0.3527777778;

    public DistanceUnitManager() {
        super();
        addUnitMapping("", 1);
        addUnitMapping("mm", 1);
        addUnitMapping("in", IN_TO_MM);
        addUnitMapping("ft", FT_TO_MM);
        addUnitMapping("cm", CM_TO_MM);
        addUnitMapping("m", M_TO_MM);
        addUnitMapping("px", PX_TO_MM);
        addUnitMapping("pt", PT_TO_MM);
    }
}
