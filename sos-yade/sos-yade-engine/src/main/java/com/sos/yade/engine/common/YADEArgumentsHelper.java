package com.sos.yade.engine.common;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sos.commons.util.SOSString;

public class YADEArgumentsHelper {

    public static List<String> stringListValue(String val, String listValueDelimiter) {
        if (val == null || val.trim().length() == 0) {
            return null;
        }
        return Stream.of(val.split(listValueDelimiter)).filter(e -> !SOSString.isEmpty(e)).collect(Collectors.toList());
    }
}
