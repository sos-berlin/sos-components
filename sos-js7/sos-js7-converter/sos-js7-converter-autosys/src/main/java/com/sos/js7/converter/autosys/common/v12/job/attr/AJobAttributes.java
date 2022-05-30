package com.sos.js7.converter.autosys.common.v12.job.attr;

import java.util.List;

import com.sos.js7.converter.commons.JS7ConverterHelper;

public abstract class AJobAttributes {

    private static final String LIST_VALUE_DELIMITER = ",";

    public static List<String> stringListValue(String val) {
        return JS7ConverterHelper.stringListValue(val, LIST_VALUE_DELIMITER);
    }

    public static List<Integer> integerListValue(String val) {
        return JS7ConverterHelper.integerListValue(val, LIST_VALUE_DELIMITER);
    }

}
