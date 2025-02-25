package com.sos.yade.engine.helpers;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.SOSArgument;
import com.sos.yade.engine.arguments.YADEArguments;
import com.sos.yade.engine.exceptions.YADEEngineInitializationException;

public class YADEArgumentsHelper {

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

    public static boolean needTargetProvider(YADEArguments args) throws YADEEngineInitializationException {
        switch (args.getOperation().getValue()) {
        case GETLIST:
        case REMOVE:
        case RENAME:
            return false;
        case UNKNOWN:
            throw new YADEEngineInitializationException(new SOSInvalidDataException(args.getOperation().getName() + "=" + args.getOperation()
                    .getValue()));
        // case COPY:
        // case MOVE:
        // case COPYFROMINTERNET:
        // case COPYTOINTERNET:
        default:
            return true;
        }
    }

    public static <T> String toString(SOSArgument<T> arg) {
        return arg.getName() + "=" + arg.getDisplayValue();
    }
}
