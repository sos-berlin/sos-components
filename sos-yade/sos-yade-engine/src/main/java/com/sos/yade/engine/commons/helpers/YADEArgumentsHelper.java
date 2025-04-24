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

    public static List<String> toList(String val, String listValueDelimiter) {
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

    public static String toString(String name, String value) {
        return name + "=" + (value == null ? "" : value);
    }

    public static String toString(SOSArgument<?> arg) {
        return toString(arg.getName(), arg);
    }

    public static String toString(String name, SOSArgument<?> arg) {
        return toString(name, arg.getDisplayValue());
    }

    public static String comparisonOperatorToString(SOSArgument<SOSComparisonOperator> arg) {
        return toString(arg.getName(), (arg.getValue() == null ? null : arg.getValue().getFirstAlias()));
    }

    public static String toStringAsOppositeValue(SOSArgument<Boolean> arg) {
        return toString("Disable" + arg.getName(), String.valueOf((!arg.isTrue())));
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
                    sb.append(", ");
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
                    String name = include.getClass().getSimpleName();
                    // if (excluded.equals(name)) {
                    // continue;
                    // }
                    sb.append(", ").append(toString(logger, name, include));
                }
            }
        } catch (Exception e) {
            sb.append(e.toString());
        }

        return sb.toString();
    }
}
