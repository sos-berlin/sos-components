package com.sos.commons.util.common;

import java.util.List;
import java.util.stream.Collectors;

public class SOSArgumentHelper {

    public static final String LIST_VALUE_DELIMITER = ";";

    public enum DisplayMode {
        MASKED("********"), UNMASKED, UNKNOWN("<hidden>");

        private final String value;

        private DisplayMode() {
            this(null);
        }

        private DisplayMode(String val) {
            value = val;
        }

        public String getValue() {
            return value;
        }
    }

    private static final String DISPLAY_VALUE_TRUNCATING_SUFFIX = "<truncated>";
    private static final int DISPLAY_VALUE_MAX_LENGTH = 255;
    private static final int DISPLAY_VALUE_USED_LENGTH = DISPLAY_VALUE_MAX_LENGTH - DISPLAY_VALUE_TRUNCATING_SUFFIX.length();

    public static String getDisplayValue(Object value, DisplayMode mode) {
        if (value == null) {
            return null;
        }
        switch (mode) {
        case UNMASKED:
            if (value instanceof List) {
                List<?> l = (List<?>) value;
                String s = (String) l.stream().map(e -> {
                    return e.toString();
                }).collect(Collectors.joining(LIST_VALUE_DELIMITER));
                return truncatingIfNeeded(s);
            }
            return truncatingIfNeeded(value.toString());
        case MASKED:
            return DisplayMode.MASKED.getValue();
        default:
            return DisplayMode.UNKNOWN.getValue();
        }
    }

    private static String truncatingIfNeeded(final String val) {
        if (val == null) {
            return val;
        }
        String v = val;
        if (v.length() > DISPLAY_VALUE_MAX_LENGTH) {
            v = v.substring(0, DISPLAY_VALUE_USED_LENGTH) + DISPLAY_VALUE_TRUNCATING_SUFFIX;
        }
        return v;
    }
}
