package com.sos.yade.engine.common;

public enum CompareOperator {

    // TODO job....
    EQ("==", "=", "equals"), NE("!=", "<>", "not equal"), LT("<", "less than"), LE("<=", "less or equal"), GE(">=", "greater or equal"), GT(">",
            "greater than");

    private final String[] symbols;

    CompareOperator(String... symbols) {
        this.symbols = symbols;
    }

    public String[] getSymbols() {
        return symbols;
    }

    public boolean matches(String input) {
        if (name().equalsIgnoreCase(input)) {
            return true;
        }
        for (String symbol : symbols) {
            if (symbol.equalsIgnoreCase(input)) {
                return true;
            }
        }
        return false;
    }

    public static CompareOperator fromSymbol(String input) {
        for (CompareOperator operator : CompareOperator.values()) {
            if (operator.matches(input)) {
                return operator;
            }
        }
        throw new IllegalArgumentException("Unknown symbol: " + input);
    }

    @Override
    public String toString() {
        return String.join(",", symbols);
    }
}
