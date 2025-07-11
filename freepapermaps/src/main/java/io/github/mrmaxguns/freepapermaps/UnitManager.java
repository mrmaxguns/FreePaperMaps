package io.github.mrmaxguns.freepapermaps;


public class UnitManager {
    public static final double IN_TO_MM = 25.4;
    public static final double FT_TO_MM = IN_TO_MM * 12;
    public static final double CM_TO_MM = 10;
    public static final double M_TO_MM = 1000;
    public static final double PX_TO_MM = IN_TO_MM / 96;

    public static double parseNumberWithUnit(String input) throws UserInputException {
        StringBuilder numberBuilder = new StringBuilder();
        StringBuilder unitBuilder = new StringBuilder();

        boolean processingNumber = true;
        for (char c : input.toCharArray()) {
            if (processingNumber && (Character.isDigit(c) || c == '.')) {
                numberBuilder.append(c);
            } else {
                processingNumber = false;
                unitBuilder.append(c);
            }
        }

        double rawNumber;
        try {
            rawNumber = Double.parseDouble(numberBuilder.toString());
        } catch (NumberFormatException e) {
            throw new UserInputException("Could not parse " + numberBuilder + " as a number.");
        }

        String unit = unitBuilder.toString().toLowerCase().strip();

        switch (unit) {
            case "", "mm" -> { return rawNumber; }
            case "in" -> { return rawNumber * IN_TO_MM; }
            case "ft" -> { return rawNumber * FT_TO_MM; }
            case "cm" -> { return rawNumber * CM_TO_MM; }
            case "m" -> { return rawNumber * M_TO_MM; }
            case "px" -> { return rawNumber * PX_TO_MM; }
            default -> throw new UserInputException("Invalid unit '" + unitBuilder + "'.");
        }
    }
}
