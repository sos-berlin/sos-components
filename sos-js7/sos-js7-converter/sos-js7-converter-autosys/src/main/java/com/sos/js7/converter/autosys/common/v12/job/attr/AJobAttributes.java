package com.sos.js7.converter.autosys.common.v12.job.attr;

import java.util.ArrayList;
import java.util.List;

import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.js7.converter.commons.JS7ConverterHelper;

//not serializable because of SOSArgument
public abstract class AJobAttributes {

  
    private static final String LIST_VALUE_DELIMITER = ",";

    public static List<String> stringListValue(String val) {
        return JS7ConverterHelper.stringListValue(val, LIST_VALUE_DELIMITER);
    }

    public static List<Integer> integerListValue(String val) {
        return JS7ConverterHelper.integerListValue(val, LIST_VALUE_DELIMITER);
    }

    public static String toString(SOSArgument<?>... args) {
        if (args == null || args.length == 0) {
            return "";
        }
        List<String> l = new ArrayList<>();
        for (SOSArgument<?> a : args) {
            if (a.getValue() == null) {
                continue;
            }
            l.add(a.getName() + "=" + a.getValue());
        }
        return String.join(",", l);
    }

}
