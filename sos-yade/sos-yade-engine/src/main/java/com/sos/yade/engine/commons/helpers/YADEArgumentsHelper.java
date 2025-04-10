package com.sos.yade.engine.commons.helpers;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.util.SOSComparisonOperator;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.arguments.base.ASOSArguments;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.yade.engine.commons.arguments.YADEArguments;
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

    public static boolean needTargetProvider(YADEArguments args) throws YADEEngineInitializationException {
        switch (args.getOperation().getValue()) {
        case GETLIST:
        case REMOVE:
            return false;
        case RENAME:
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

    public static String comparisonOperatorToString(SOSArgument<SOSComparisonOperator> arg) {
        return arg.getName() + "=" + (arg.getValue() == null ? "" : arg.getValue().getFirstAlias());
    }

    public static <T> String toString(String name, SOSArgument<T> arg) {
        return name + "=" + arg.getDisplayValue();
    }

    public static String toStringAsOppositeValue(SOSArgument<Boolean> arg) {
        return "Disable" + arg.getName() + "=" + (!arg.isTrue());
    }

    public static String toString(ISOSLogger logger, String label, ASOSArguments args) {
        if (args == null) {
            return "[" + label + "]null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(label).append("]");
        sb.append("[").append(args.getClass().getSimpleName()).append("]");
        boolean add = false;
        try {
            for (SOSArgument<?> arg : args.getArguments()) {
                if (add) {
                    sb.append(",");
                }
                sb.append(toString(arg));
                add = true;
            }
        } catch (Throwable e) {
            logger.warn(sb + e.toString());
        }
        try {
            List<ASOSArguments> included = args.getIncludedArgumentsIfNotNull();
            if (included != null) {
                for (ASOSArguments include : included) {
                    sb.append(",").append(toString(logger, include.getClass().getSimpleName(), include));
                }
            }

        } catch (Exception e) {
            sb.append(e.toString());
        }

        return sb.toString();
    }
}
