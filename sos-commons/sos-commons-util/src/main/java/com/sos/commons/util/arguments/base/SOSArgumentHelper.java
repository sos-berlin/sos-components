package com.sos.commons.util.arguments.base;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.arguments.base.SOSArgument.DisplayMode;

public class SOSArgumentHelper {

    public static final String DEFAULT_LIST_VALUE_DELIMITER = ";";

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
            return formatValue(value.toString());
        case MASKED:
            return DisplayMode.MASKED.getValue();
        default:
            return DisplayMode.UNKNOWN.getValue();
        }
    }

    public static String getDisplayValueIgnoreUnknown(Object value, DisplayMode mode) {
        if (value == null) {
            return null;
        }
        switch (mode) {
        case NONE:
            return DisplayMode.NONE.getValue();
        case MASKED:
            return DisplayMode.MASKED.getValue();
        case UNMASKED:
        default:
            return formatValue(value.toString());
        }
    }

    public static String getClassName(String fullyQualifiedName) {
        if (fullyQualifiedName == null) {
            return null;
        }
        int indx = fullyQualifiedName.lastIndexOf('.');
        return indx > -1 ? fullyQualifiedName.substring(indx + 1) : fullyQualifiedName;
    }

    public static long asMillis(SOSArgument<String> arg) {
        return asSeconds(arg, 0L) * 1_000;
    }

    public static long asSeconds(SOSArgument<String> arg, long defaultValue) {
        if (arg.getValue() == null) {
            return defaultValue;
        }
        try {
            return SOSDate.resolveAge("s", arg.getValue()).longValue();
        } catch (Throwable e) {
            return defaultValue;
        }
    }

    public static void setListStringArgumentValue(SOSArgument<List<String>> arg, String value) {
        setListStringArgumentValue(arg, value, DEFAULT_LIST_VALUE_DELIMITER);
    }

    public static void setListStringArgumentValue(SOSArgument<List<String>> arg, String value, String listValueDelimiter) {
        if (arg == null) {
            return;
        }
        if (SOSString.isEmpty(value)) {
            arg.setValue(null);
        } else {
            arg.setValue(Stream.of(value.split(listValueDelimiter)).filter(e -> !SOSString.isEmpty(e)).collect(Collectors.toList()));
        }
    }

    public static void setListPathArgumentValue(SOSArgument<List<Path>> arg, String value) {
        setListPathArgumentValue(arg, value, DEFAULT_LIST_VALUE_DELIMITER);
    }

    public static void setListPathArgumentValue(SOSArgument<List<Path>> arg, String value, String listValueDelimiter) {
        if (arg == null) {
            return;
        }
        if (SOSString.isEmpty(value)) {
            arg.setValue(null);
        } else {
            arg.setValue(Stream.of(value.split(listValueDelimiter)).filter(e -> !SOSString.isEmpty(e)).map(p -> Path.of(p)).collect(Collectors
                    .toList()));
        }
    }

    public static String getListStringArgumentValueAsString(SOSArgument<List<String>> arg) {
        return getListStringArgumentValueAsString(arg, DEFAULT_LIST_VALUE_DELIMITER);
    }

    public static String getListStringArgumentValueAsString(SOSArgument<List<String>> arg, String listValueDelimiter) {
        if (arg == null || arg.getValue() == null) {
            return null;
        }
        return String.join(listValueDelimiter, arg.getValue());
    }

    private static String formatValue(final String val) {
        if (val == null) {
            return val;
        }
        String v = val;
        // 1) replace new lines
        v = SOSString.replaceNewLines(v, " ");
        // 2) truncate if needed
        if (v.length() > DISPLAY_VALUE_MAX_LENGTH) {
            v = v.substring(0, DISPLAY_VALUE_USED_LENGTH) + DISPLAY_VALUE_TRUNCATING_SUFFIX;
        }
        return v;
    }
}
