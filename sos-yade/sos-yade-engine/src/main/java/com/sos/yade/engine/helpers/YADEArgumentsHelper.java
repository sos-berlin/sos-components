package com.sos.yade.engine.helpers;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.SOSArgument;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.yade.engine.arguments.YADEArguments;
import com.sos.yade.engine.arguments.YADESourceArguments;
import com.sos.yade.engine.arguments.YADESourceArguments.ZeroByteTransfer;
import com.sos.yade.engine.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.delegators.YADETargetProviderDelegator;
import com.sos.yade.engine.exceptions.YADEEngineInitializationException;
import com.sos.yade.engine.handlers.source.YADESourcePollingHandler;

public class YADEArgumentsHelper {

    /** Check on Start
     * 
     * @param args
     * @throws SOSYADEEngineException */
    public static void checkCommonConfiguration(ISOSLogger logger, YADEArguments args, YADESourceArguments sourceArgs)
            throws YADEEngineInitializationException {
        if (args == null) {
            throw new YADEEngineInitializationException(new SOSMissingDataException("YADEArguments"));
        }
        if (args.getOperation().getValue() == null) {
            throw new YADEEngineInitializationException(new SOSMissingDataException(args.getOperation().getName()));
        }
        sourceArguments(logger, sourceArgs);
    }

    public static void checkConfiguration(YADEArguments args, YADESourceProviderDelegator sourceDelegator,
            YADETargetProviderDelegator targetDelegator, YADESourcePollingHandler sourcePolling) throws YADEEngineInitializationException {
        // Source: Polling
        if (sourcePolling.enabled()) {
            if (sourceDelegator.getArgs().getPolling().getPollingWait4SourceFolder().getValue() && sourceDelegator.getDirectory() == null) {
                throw new YADEEngineInitializationException(sourceDelegator.getArgs().getPolling().getPollingWait4SourceFolder().getName()
                        + "=true, but source_dir is not set");
            }
        }

        // Source/Target: check IntegrityHashAlgorithm before operatiob
        if (isIntegrityHashEnabled(sourceDelegator, targetDelegator)) {
            try {
                MessageDigest.getInstance(args.getIntegrityHashAlgorithm().getValue());
            } catch (NoSuchAlgorithmException e) {
                throw new YADEEngineInitializationException(e);
            }
        }
    }

    public static String getIntegrityHashAlgorithm(YADEArguments args, YADESourceProviderDelegator sourceDelegator,
            YADETargetProviderDelegator targetDelegator) {
        if (!isIntegrityHashEnabled(sourceDelegator, targetDelegator)) {
            return null;
        }
        return args.getIntegrityHashAlgorithm().getValue();
    }

    public static boolean isIntegrityHashEnabled(YADESourceProviderDelegator sourceDelegator, YADETargetProviderDelegator targetDelegator) {
        if (targetDelegator == null) {
            return false;
        }
        return sourceDelegator.getArgs().getCheckIntegrityHash().isTrue() || targetDelegator.getArgs().getCreateIntegrityHashFile().isTrue();
    }

    private static void sourceArguments(ISOSLogger logger, YADESourceArguments args) {
        if (ZeroByteTransfer.RELAXED.equals(args.getZeroByteTransfer().getValue())) {
            if (args.getMinFileSize().getValue() == null || args.getMinFileSize().getValue().longValue() <= 0L) {
                logger.info("minFileSize is set to 1 due to ZeroByteTransfer.RELAXED");

                args.getMinFileSize().setValue(Long.valueOf(1L));
            }
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
