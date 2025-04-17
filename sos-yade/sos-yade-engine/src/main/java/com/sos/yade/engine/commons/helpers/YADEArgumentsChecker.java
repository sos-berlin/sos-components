package com.sos.yade.engine.commons.helpers;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.yade.commons.Yade.TransferOperation;
import com.sos.yade.engine.commons.arguments.YADEArguments;
import com.sos.yade.engine.commons.arguments.YADEClientArguments;
import com.sos.yade.engine.commons.arguments.YADESourceArguments;
import com.sos.yade.engine.commons.arguments.YADESourceArguments.ZeroByteTransfer;
import com.sos.yade.engine.commons.arguments.loaders.AYADEArgumentsLoader;
import com.sos.yade.engine.commons.arguments.YADETargetArguments;
import com.sos.yade.engine.exceptions.YADEEngineInitializationException;

public class YADEArgumentsChecker {

    public static void validateOrExit(ISOSLogger logger, AYADEArgumentsLoader argsLoader) throws YADEEngineInitializationException {

        boolean needTargetProvider = checkCommonArguments(argsLoader.getArgs(), argsLoader.getClientArgs());
        checkClientArguments(argsLoader.getArgs(), argsLoader.getClientArgs(), needTargetProvider);
        checkSourceArguments(argsLoader.getSourceArgs());
        checkTargetArguments(argsLoader.getArgs(), argsLoader.getTargetArgs(), needTargetProvider);
        checkSourceTargetArguments(argsLoader.getArgs(), argsLoader.getSourceArgs(), argsLoader.getTargetArgs(), needTargetProvider);

        adjustSourceArguments(logger, argsLoader.getSourceArgs());
        adjustTargetArguments(logger, argsLoader.getSourceArgs(), argsLoader.getTargetArgs(), needTargetProvider);
    }

    private static boolean checkCommonArguments(YADEArguments args, YADEClientArguments clientArgs) throws YADEEngineInitializationException {
        if (args == null) {
            throw new YADEEngineInitializationException("Missing YADEArguments");
        }
        if (args.getOperation().getValue() == null) {
            throw new YADEEngineInitializationException("Missing \"" + args.getOperation().getName() + "\" argument");
        }
        return YADEArgumentsHelper.needTargetProvider(args);
    }

    private static void checkClientArguments(YADEArguments args, YADEClientArguments clientArgs, boolean needTargetProvider)
            throws YADEEngineInitializationException {
        if (!needTargetProvider && TransferOperation.GETLIST.equals(args.getOperation().getValue())) {
            if (clientArgs == null || clientArgs.getResultSetFile().isEmpty()) {
                throw new YADEEngineInitializationException("[" + YADEClientArguments.LABEL + "][" + TransferOperation.GETLIST + "]Missing \""
                        + clientArgs.getResultSetFile().getName() + "\" argument");
            }
        }
    }

    private static void checkSourceArguments(YADESourceArguments sourceArgs) throws YADEEngineInitializationException {
        if (sourceArgs == null) {
            throw new YADEEngineInitializationException("Missing Source Arguments");
        }
        if (!sourceArgs.isSingleFilesSelection() && sourceArgs.getDirectory().isEmpty()) {
            throw new YADEEngineInitializationException(String.format("[%s]The \"%s\" argument is missing but is required if %s is set",
                    YADESourceArguments.LABEL, sourceArgs.getDirectory().getName(), YADEArgumentsHelper.toString(sourceArgs.getFileSpec())));
        }
        if (sourceArgs.isPollingEnabled()) {
            if (sourceArgs.getPolling().getPollingWait4SourceFolder().getValue() && sourceArgs.getDirectory() == null) {
                throw new YADEEngineInitializationException("[" + YADESourceArguments.LABEL + "]" + sourceArgs.getPolling()
                        .getPollingWait4SourceFolder().getName() + "=true, but \"" + sourceArgs.getDirectory().getName() + "\" is not set");
            }
        }

    }

    private static void checkTargetArguments(YADEArguments args, YADETargetArguments targetArgs, boolean needTargetProvider)
            throws YADEEngineInitializationException {
        if (needTargetProvider && targetArgs == null) {
            throw new YADEEngineInitializationException("Missing Target Arguments");
        }
    }

    private static void checkSourceTargetArguments(YADEArguments args, YADESourceArguments sourceArgs, YADETargetArguments targetArgs,
            boolean needTargetProvider) throws YADEEngineInitializationException {
        String sourceAlg = sourceArgs.getCheckIntegrityHash().isTrue() ? sourceArgs.getIntegrityHashAlgorithm().getValue() : null;
        String targetAlg = needTargetProvider && targetArgs.getCreateIntegrityHashFile().isTrue() ? targetArgs.getIntegrityHashAlgorithm().getValue()
                : null;

        if (sourceAlg != null || targetAlg != null) {
            try {
                if (sourceAlg != null && targetAlg != null) {
                    if (sourceAlg.equals(targetAlg)) {
                        MessageDigest.getInstance(sourceAlg);
                    } else {
                        MessageDigest.getInstance(sourceAlg);
                        MessageDigest.getInstance(targetAlg);
                    }
                } else if (sourceAlg != null) {
                    MessageDigest.getInstance(sourceAlg);
                } else if (targetAlg != null) {
                    MessageDigest.getInstance(targetAlg);
                }
            } catch (NoSuchAlgorithmException e) {
                throw new YADEEngineInitializationException(e);
            }
        }
    }

    private static void adjustSourceArguments(ISOSLogger logger, YADESourceArguments args) {
        if (ZeroByteTransfer.RELAXED.equals(args.getZeroByteTransfer().getValue())) {
            if (args.getMinFileSize().getValue() == null || args.getMinFileSize().getValue().longValue() <= 0L) {
                logger.info("[%s][%s=1]ZeroByteTransfer.RELAXED is active, setting %s=1", YADESourceArguments.LABEL, args.getMinFileSize().getName(),
                        args.getMinFileSize().getName());
                args.getMinFileSize().setValue(Long.valueOf(1L));
            }
        }
    }

    private static void adjustTargetArguments(ISOSLogger logger, YADESourceArguments sourceArgs, YADETargetArguments targetArgs,
            boolean needTargetProvider) {
        if (!needTargetProvider) {
            if (targetArgs != null) {
                logger.info("Target arguments are ignored");
            }
            targetArgs = null;
            return;
        }
        if (!sourceArgs.isSingleFilesSelection() && targetArgs.getDirectory().isEmpty()) {
            logger.info("[%s][configured \"%s\" is missing, using '.' as the default]%s=.", YADETargetArguments.LABEL, targetArgs.getDirectory()
                    .getName(), targetArgs.getDirectory().getName());
            targetArgs.getDirectory().setValue(".");
        }
    }
}
