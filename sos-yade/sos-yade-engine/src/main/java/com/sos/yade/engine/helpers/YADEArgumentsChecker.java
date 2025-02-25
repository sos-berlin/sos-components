package com.sos.yade.engine.helpers;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.yade.engine.arguments.YADEArguments;
import com.sos.yade.engine.arguments.YADESourceArguments;
import com.sos.yade.engine.arguments.YADESourceArguments.ZeroByteTransfer;
import com.sos.yade.engine.arguments.YADETargetArguments;
import com.sos.yade.engine.exceptions.YADEEngineInitializationException;

public class YADEArgumentsChecker {

    public static void validateOrExit(ISOSLogger logger, YADEArguments args, YADESourceArguments sourceArgs, YADETargetArguments targetArgs)
            throws YADEEngineInitializationException {

        checkCommonArguments(args);
        checkSourceArguments(sourceArgs);
        checkTargetArguments(args, targetArgs);
        checkSourceTargetArguments(args, sourceArgs, targetArgs);

        adjustSourceArguments(logger, sourceArgs);
        adjustTargetArguments(logger, sourceArgs, targetArgs);
    }

    private static void checkCommonArguments(YADEArguments args) throws YADEEngineInitializationException {
        if (args == null) {
            throw new YADEEngineInitializationException("Missing YADEArguments");
        }
        if (args.getOperation().getValue() == null) {
            throw new YADEEngineInitializationException("Missing \"" + args.getOperation().getName() + "\" argument");
        }
    }

    private static void checkSourceArguments(YADESourceArguments sourceArgs) throws YADEEngineInitializationException {
        if (sourceArgs == null) {
            throw new YADEEngineInitializationException("Missing Source Arguments");
        }
        if (!sourceArgs.isSingleFilesSelection() && sourceArgs.getDirectory().isEmpty()) {
            throw new YADEEngineInitializationException(String.format("The \"source_dir\" argument is missing but is required if %s is set",
                    YADEArgumentsHelper.toString(sourceArgs.getFileSpec())));
        }
        if (sourceArgs.isPollingEnabled()) {
            if (sourceArgs.getPolling().getPollingWait4SourceFolder().getValue() && sourceArgs.getDirectory() == null) {
                throw new YADEEngineInitializationException(sourceArgs.getPolling().getPollingWait4SourceFolder().getName()
                        + "=true, but source_dir is not set");
            }
        }

    }

    private static void checkTargetArguments(YADEArguments args, YADETargetArguments targetArgs) throws YADEEngineInitializationException {
        if (YADEArgumentsHelper.needTargetProvider(args) && targetArgs == null) {
            throw new YADEEngineInitializationException("Missing Target Arguments");
        }
    }

    private static void checkSourceTargetArguments(YADEArguments args, YADESourceArguments sourceArgs, YADETargetArguments targetArgs)
            throws YADEEngineInitializationException {
        if (sourceArgs.getCheckIntegrityHash().isTrue() || (targetArgs != null && targetArgs.getCreateIntegrityHashFile().isTrue())) {
            try {
                MessageDigest.getInstance(args.getIntegrityHashAlgorithm().getValue());
            } catch (NoSuchAlgorithmException e) {
                throw new YADEEngineInitializationException(e);
            }
        }
    }

    private static void adjustSourceArguments(ISOSLogger logger, YADESourceArguments args) {
        if (ZeroByteTransfer.RELAXED.equals(args.getZeroByteTransfer().getValue())) {
            if (args.getMinFileSize().getValue() == null || args.getMinFileSize().getValue().longValue() <= 0L) {
                logger.info("[%s=1]ZeroByteTransfer.RELAXED is active, setting %s=1", args.getMinFileSize().getName(), args.getMinFileSize()
                        .getName());
                args.getMinFileSize().setValue(Long.valueOf(1L));
            }
        }
    }

    private static void adjustTargetArguments(ISOSLogger logger, YADESourceArguments sourceArgs, YADETargetArguments targetArgs) {
        if (targetArgs == null) {
            return;
        }
        if (!sourceArgs.isSingleFilesSelection() && targetArgs.getDirectory().isEmpty()) {
            logger.info("[target_dir=.]configured target_dir is missing, using '.' as the default");
            targetArgs.getDirectory().setValue(".");
        }
    }
}
