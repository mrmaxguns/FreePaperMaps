package io.github.mrmaxguns.freepapermaps.styling.language;


import io.github.mrmaxguns.freepapermaps.rendering.Scaler;


public class UnitManager {
    public static final double IN_TO_MM = 25.4;
    public static final double FT_TO_MM = IN_TO_MM * 12;
    public static final double MI_TO_MM = 1.609344e+6;

    public static final double CM_TO_MM = 10;
    public static final double M_TO_MM = 1000;
    public static final double KM_TO_MM = 1e+6;

    public static final double PX_TO_MM = IN_TO_MM / 96;
    public static final double PT_TO_MM = 0.3527777778;

    public static final double RAD_TO_DEG = 57.29578;

    private final Scaler scaler;

    public UnitManager(Scaler scaler) {
        this.scaler = scaler;
    }

    public double parseDistanceWithUnit(double number, String unit) {
        switch (unit.strip()) {
            case "IN" -> { return scaler.scale(number * IN_TO_MM); }
            case "in" -> { return number * IN_TO_MM; }
            case "FT" -> { return scaler.scale(number * FT_TO_MM); }
            case "ft" -> { return number * FT_TO_MM; }
            case "MI" -> { return scaler.scale(number * MI_TO_MM); }
            case "mi" -> { return number * MI_TO_MM; }
            case "MM" -> { return scaler.scale(number); }
            case "mm", "" -> { return number; }
            case "CM" -> { return scaler.scale(number * CM_TO_MM); }
            case "cm" -> { return number * CM_TO_MM; }
            case "M" -> { return scaler.scale(number * M_TO_MM); }
            case "m" -> { return number * M_TO_MM; }
            case "KM" -> { return scaler.scale(number * KM_TO_MM); }
            case "km" -> { return number * KM_TO_MM; }
            case "PX" -> { return scaler.scale(number * PX_TO_MM); }
            case "px" -> { return number * PX_TO_MM; }
            case "PT" -> { return scaler.scale(number * PT_TO_MM); }
            case "pt" -> { return number * PT_TO_MM; }
            default -> {
                throw new IllegalArgumentException("Invalid distance unit '" + unit + "'.");
            }
        }
    }

    public boolean isDistanceUnit(String unit) {
        return unit.isBlank() || unit.toLowerCase().matches("in|ft|mi|mm|cm|m|km|px|pt");
    }

    public double parseAngleWithUnit(double number, String unit) {
        switch (unit.strip()) {
            case "deg", "" -> { return number; }
            case "rad" -> { return number * RAD_TO_DEG; }
            default -> {
                throw new IllegalArgumentException("Invalid angular unit '" + unit + "'.");
            }
        }
    }

    public boolean isAngleUnit(String unit) {
        return unit.isBlank() || unit.toLowerCase().matches("deg|rad");
    }
}
