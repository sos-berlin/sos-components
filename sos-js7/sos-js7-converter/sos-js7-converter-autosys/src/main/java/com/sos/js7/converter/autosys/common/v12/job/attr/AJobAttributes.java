package com.sos.js7.converter.autosys.common.v12.job.attr;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

public abstract class AJobAttributes {

    private static final String LIST_VALUE_DELIMITER = ",";

    public static String stringValue(String val) {
        return val == null ? null : StringUtils.strip(val.trim(), "\"");
    }

    public static Integer integerValue(String val) {
        return val == null ? null : Integer.parseInt(val.trim());
    }

    public static Long longValue(String val) {
        return val == null ? null : Long.parseLong(val.trim());
    }

    public static boolean booleanValue(String val, boolean defaultValue) {
        boolean v = defaultValue;
        if (val != null) {
            switch (val.trim().toLowerCase()) {
            case "true":
            case "y":
            case "1":
                v = true;
                break;
            case "false":
            case "n":
            case "0":
                v = false;
                break;
            }
        }
        return v;
    }

    public static List<String> stringListValue(String val) {
        if (val == null || val.trim().length() == 0) {
            return null;
        }
        return Stream.of(val.split(LIST_VALUE_DELIMITER)).map(e -> {
            return new String(stringValue(e));
        }).collect(Collectors.toList());
    }

    public static List<Integer> integerListValue(String val) {
        if (val == null || val.trim().length() == 0) {
            return null;
        }
        return Stream.of(val.split(LIST_VALUE_DELIMITER)).map(e -> {
            return integerValue(e);
        }).collect(Collectors.toList());
    }

}
