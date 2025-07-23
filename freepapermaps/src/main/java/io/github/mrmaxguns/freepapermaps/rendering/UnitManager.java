package io.github.mrmaxguns.freepapermaps.rendering;

import io.github.mrmaxguns.freepapermaps.UserInputException;

import java.util.HashMap;


public class UnitManager {
    private final HashMap<String, Double> unitMapping;

    public UnitManager() {
        unitMapping = new HashMap<>();
    }

    public void addUnitMapping(String unit, double conversionFactor) {
        unitMapping.put(unit, conversionFactor);
    }

    public double parseNumberWithUnit(String input) throws UserInputException {
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

        if (rawNumber < 0) {
            throw new UserInputException("Distances must be positive, but got '" + rawNumber + "'.");
        }

        String unit = unitBuilder.toString().toLowerCase().strip();

        if (unitMapping.containsKey(unit)) {
            return rawNumber * unitMapping.get(unit);
        }

        throw new UserInputException("Invalid unit '" + unitBuilder + "'.");
    }
}
