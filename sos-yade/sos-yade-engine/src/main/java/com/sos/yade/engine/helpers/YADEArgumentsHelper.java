package com.sos.yade.engine.helpers;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.SOSArgument;
import com.sos.yade.engine.arguments.YADEArguments;
import com.sos.yade.engine.exceptions.SOSYADEEngineException;

public class YADEArgumentsHelper {

    /** Check on Start
     * 
     * @param args
     * @throws SOSYADEEngineException */
    public static void checkOnStart(YADEArguments args) throws SOSYADEEngineException {
        if (args == null) {
            throw new SOSYADEEngineException(new SOSMissingDataException("YADEArguments"));
        }
        if (args.getOperation().getValue() == null) {
            throw new SOSYADEEngineException(new SOSMissingDataException(args.getOperation().getName()));
        }
    }

    public static List<String> stringListValue(String val, String listValueDelimiter) {
        if (val == null || val.trim().length() == 0) {
            return null;
        }
        return Stream.of(val.split(listValueDelimiter)).filter(e -> !SOSString.isEmpty(e)).collect(Collectors.toList());
    }

    public static String toString(SOSArgument<List<String>> arg, String listValueDelimiter) {
        if (arg == null || arg.getValue() == null) {
            return null;
        }
        return String.join(listValueDelimiter, arg.getValue());
    }

    public static long getIntervalInSeconds(SOSArgument<String> arg, long defaultValue) {
        try {
            return SOSDate.resolveAge("s", arg.getValue()).longValue();
        } catch (Throwable e) {
            return defaultValue;
        }
    }
}
