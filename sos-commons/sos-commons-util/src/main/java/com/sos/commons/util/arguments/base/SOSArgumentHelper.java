package com.sos.commons.util.arguments.base;

import com.sos.commons.util.arguments.base.SOSArgument.DisplayMode;

public class SOSArgumentHelper {

    public static final String LIST_VALUE_DELIMITER = ";";

    private static final String DISPLAY_VALUE_TRUNCATING_SUFFIX = "<truncated>";
    private static final int DISPLAY_VALUE_MAX_LENGTH = 255;
    private static final int DISPLAY_VALUE_USED_LENGTH = DISPLAY_VALUE_MAX_LENGTH - DISPLAY_VALUE_TRUNCATING_SUFFIX.length();

    public static String getDisplayValue(Object value, DisplayMode mode) {
        if (value == null) {
            return null;
        }
        switch (mode) {
        case NONE:
            return DisplayMode.NONE.getValue();
        case UNMASKED:
            return truncatingIfNeeded(value.toString());
        case MASKED:
            return DisplayMode.MASKED.getValue();
        default:
            return DisplayMode.UNKNOWN.getValue();
        }
    }

    public static String getClassName(String fullyQualifiedName) {
        if (fullyQualifiedName == null) {
            return null;
        }
        int indx = fullyQualifiedName.lastIndexOf('.');
        return indx > -1 ? fullyQualifiedName.substring(indx + 1) : fullyQualifiedName;
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
