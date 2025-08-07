package io.github.mrmaxguns.freepapermaps.styling.language;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;


public class CustomFunction implements
        BiFunction<Interpreter.Context, List<Interpreter.Primitive>, Interpreter.Primitive> {

    private final String name;
    private final Interpreter.Primitive.Type variableParameterType;
    private final List<String> parameterNames;
    private final List<Interpreter.Primitive.Type> parameterTypes;
    private final BiFunction<Interpreter.Context, List<Interpreter.Primitive>, Interpreter.Primitive> implementation;

    public CustomFunction(Builder builder) {
        this.name = Objects.requireNonNull(builder.name);
        this.variableParameterType = builder.variableParameterType;

        if (builder.parameterNames.size() != builder.parameterTypes.size()) {
            throw new IllegalStateException("There should be the same amount of parameter names as there are types.");
        }

        this.parameterNames = builder.parameterNames;
        this.parameterTypes = builder.parameterTypes;
        this.implementation = Objects.requireNonNull(builder.implementation);
    }

    @Override
    public Interpreter.Primitive apply(Interpreter.Context context, List<Interpreter.Primitive> args) {
        // Validate arguments
        boolean visitingVariableParameters = false;
        int i = 0;
        for (i = 0; i < args.size(); ++i) {
            if (i >= parameterTypes.size()) {
                if (variableParameterType == null) {
                    throw new IllegalArgumentException("Function '" + name + "' does not support optional arguments.");
                }
                visitingVariableParameters = true;
            }

            Interpreter.Primitive arg = args.get(i);
            Interpreter.Primitive.Type expectedType;
            String parameterName;

            if (visitingVariableParameters) {
                expectedType = variableParameterType;
                parameterName = "variable parameter";
            } else {
                expectedType = parameterTypes.get(i);
                parameterName = parameterNames.get(i);
            }

            if (arg.type != expectedType) {
                throw new IllegalArgumentException(
                        "Function '" + name + " parameter '" + parameterName + "' expected type " +
                        expectedType.name() + "but got type " + arg.type + ".");
            }
        }

        if (i < parameterTypes.size()) {
            throw new IllegalArgumentException("Too few arguments passed to function '" + name + "'.");
        }

        return implementation.apply(context, args);
    }

    public static class Builder {
        private final List<String> parameterNames = new ArrayList<>();
        private final List<Interpreter.Primitive.Type> parameterTypes = new ArrayList<>();
        private String name;
        private Interpreter.Primitive.Type variableParameterType;
        private BiFunction<Interpreter.Context, List<Interpreter.Primitive>, Interpreter.Primitive> implementation;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder parameter(String name, Interpreter.Primitive.Type type) {
            parameterNames.add(name);
            parameterTypes.add(type);
            return this;
        }

        public Builder enableVariableParams(Interpreter.Primitive.Type type) {
            variableParameterType = type;
            return this;
        }

        public Builder implementation(
                BiFunction<Interpreter.Context, List<Interpreter.Primitive>, Interpreter.Primitive> implementation) {
            this.implementation = implementation;
            return this;
        }

        public CustomFunction build() {
            return new CustomFunction(this);
        }
    }
}
