package com.sos.commons.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum SOSComparisonOperator {

    EQUAL("eq", "equal", "==", "=") {

        @Override
        public boolean compare(int actualValue, int expectedValue) {
            return actualValue == expectedValue;
        }
    },
    NOT_EQUAL("ne", "not equal", "!=", "<>") {

        @Override
        public boolean compare(int actualValue, int expectedValue) {
            return actualValue != expectedValue;
        }
    },
    LESS_THAN("lt", "less than", "<") {

        @Override
        public boolean compare(int actualValue, int expectedValue) {
            return actualValue < expectedValue;
        }
    },
    LESS_OR_EQUAL("le", "less or equal", "<=") {

        @Override
        public boolean compare(int actualValue, int expectedValue) {
            return actualValue <= expectedValue;
        }
    },
    GREATER_OR_EQUAL("ge", "greater or equal", ">=") {

        @Override
        public boolean compare(int actualValue, int expectedValue) {
            return actualValue >= expectedValue;
        }
    },
    GREATER_THAN("gt", "greater than", ">") {

        @Override
        public boolean compare(int actualValue, int expectedValue) {
            return actualValue > expectedValue;
        }
    };

    private final Set<String> aliases;

    SOSComparisonOperator(String... aliases) {
        this.aliases = new HashSet<>(Arrays.asList(aliases));
    }

    public abstract boolean compare(int actualValue, int expectedValue);

    public static SOSComparisonOperator fromString(String operator) {
        return Arrays.stream(values()).filter(op -> op.aliases.contains(operator.toLowerCase())).findFirst().orElse(null);
    }
}
