package com.sos.yade.engine.commons.helpers;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.yade.commons.Yade.TransferOperation;
import com.sos.yade.engine.commons.arguments.YADEArguments;
import com.sos.yade.engine.commons.arguments.YADEClientArguments;
import com.sos.yade.engine.commons.arguments.YADESourceArguments;
import com.sos.yade.engine.commons.arguments.YADESourceArguments.ZeroByteTransfer;
import com.sos.yade.engine.commons.arguments.YADETargetArguments;
import com.sos.yade.engine.commons.arguments.loaders.AYADEArgumentsLoader;
import com.sos.yade.engine.exceptions.YADEEngineInitializationException;

public class YADEArgumentsChecker {

    public static void validateOrExit(ISOSLogger logger, AYADEArgumentsLoader argsLoader) throws YADEEngineInitializationException {
        validateCommonArguments(argsLoader.getArgs());

        boolean needTargetProvider = YADEArgumentsHelper.needTargetProvider(argsLoader.getArgs());
        validateAndAdjustClientArguments(logger, argsLoader.getArgs(), argsLoader.getClientArgs(), needTargetProvider);
        validateAndAdjustSourceArguments(logger, argsLoader.getSourceArgs());
        validateAndAdjustTargetArguments(logger, argsLoader.getArgs(), argsLoader.getSourceArgs(), argsLoader.getTargetArgs(), needTargetProvider);
        validateSourceTargetArguments(argsLoader.getArgs(), argsLoader.getSourceArgs(), argsLoader.getTargetArgs(), needTargetProvider);
    }

    private static void validateCommonArguments(YADEArguments args) throws YADEEngineInitializationException {
        if (args == null) {
            throw new YADEEngineInitializationException("Missing YADEArguments");
        }
        if (args.getOperation().getValue() == null) {
            throw new YADEEngineInitializationException("Missing \"" + args.getOperation().getName() + "\" argument");
        }
    }

    private static void validateAndAdjustClientArguments(ISOSLogger logger, YADEArguments args, YADEClientArguments clientArgs,
            boolean needTargetProvider) throws YADEEngineInitializationException {
        if (!needTargetProvider && TransferOperation.GETLIST.equals(args.getOperation().getValue())) {
            if (clientArgs == null || clientArgs.getResultSetFile().isEmpty()) {
                throw new YADEEngineInitializationException("[" + YADEClientArguments.LABEL + "][" + TransferOperation.GETLIST
                        + "]Missing \"SourceFileOptions/ResultSet/" + clientArgs.getResultSetFile().getName() + "\" argument");
            }
        }
        if (clientArgs != null && !clientArgs.getResultSetFile().isEmpty()) {
            replaceExpressions(logger, YADEClientArguments.LABEL, clientArgs.getResultSetFile());
        }
    }

    private static void validateAndAdjustSourceArguments(ISOSLogger logger, YADESourceArguments sourceArgs) throws YADEEngineInitializationException {
        // validate
        if (sourceArgs == null) {
            throw new YADEEngineInitializationException("Missing Source Arguments");
        }
        if (sourceArgs.isPollingEnabled()) {
            if (sourceArgs.getPolling().getPollingWait4SourceFolder().getValue() && sourceArgs.getDirectory() == null) {
                throw new YADEEngineInitializationException("[" + YADESourceArguments.LABEL + "]" + sourceArgs.getPolling()
                        .getPollingWait4SourceFolder().getName() + "=true, but \"" + sourceArgs.getDirectory().getName() + "\" is not set");
            }
        }
        // adjust
        if (sourceArgs.isSingleFilesSelection()) {
            if (sourceArgs.isFilePathEnabled()) {
                replaceExpressionsForListArg(null, YADESourceArguments.LABEL, sourceArgs.getFilePath());
            }
        } else {
            if (sourceArgs.getDirectory().isEmpty()) {
                throw new YADEEngineInitializationException(String.format("[%s]The \"%s\" argument is missing but is required if %s is set",
                        YADESourceArguments.LABEL, sourceArgs.getDirectory().getName(), YADEArgumentsHelper.toString(sourceArgs.getFileSpec())));
            }
            replaceExpressions(null, YADESourceArguments.LABEL, sourceArgs.getFileSpec());
        }

        if (ZeroByteTransfer.RELAXED.equals(sourceArgs.getZeroByteTransfer().getValue())) {
            if (sourceArgs.getMinFileSize().getValue() == null || sourceArgs.getMinFileSize().getValue().longValue() <= 0L) {
                logger.info("[%s][%s=1]ZeroByteTransfer.RELAXED is active, setting %s=1", YADESourceArguments.LABEL, sourceArgs.getMinFileSize()
                        .getName(), sourceArgs.getMinFileSize().getName());
                sourceArgs.getMinFileSize().setValue(Long.valueOf(1L));
            }
        }

    }

    private static void validateAndAdjustTargetArguments(ISOSLogger logger, YADEArguments args, YADESourceArguments sourceArgs,
            YADETargetArguments targetArgs, boolean needTargetProvider) throws YADEEngineInitializationException {
        // validate
        if (!needTargetProvider) {
            if (targetArgs != null) {
                logger.info("Target arguments are ignored");
            }
            targetArgs = null;
            return;
        }
        if (targetArgs == null) {
            throw new YADEEngineInitializationException("Missing Target Arguments");
        }
        // adjust
        if (!targetArgs.getCumulativeFileName().isEmpty()) {
            replaceExpressions(logger, YADETargetArguments.LABEL, targetArgs.getCumulativeFileName());
        }

        if (!sourceArgs.isSingleFilesSelection() && targetArgs.getDirectory().isEmpty()) {
            targetArgs.getDirectory().setValue(".");

            logger.info("[%s][configured \"%s\" is missing, using '%s' as the default]%s", YADETargetArguments.LABEL, targetArgs.getDirectory()
                    .getName(), targetArgs.getDirectory().getValue(), YADEArgumentsHelper.toString(targetArgs.getDirectory()));
        }
        if (targetArgs.getAppendFiles().isTrue()) {
            if (args.getTransactional().isTrue() || targetArgs.isAtomicityEnabled()) {
                String startPart = "[" + YADETargetArguments.LABEL + "]" + YADEArgumentsHelper.toString(targetArgs.getAppendFiles())
                        + " not compatible with ";
                String notCompatiblePart = null;
                if (args.getTransactional().isTrue()) {
                    notCompatiblePart = YADEArgumentsHelper.toString(args.getTransactional());
                } else {
                    if (targetArgs.getAtomicPrefix().isDirty()) {
                        notCompatiblePart = YADEArgumentsHelper.toString(targetArgs.getAtomicPrefix());
                    }
                    if (targetArgs.getAtomicSuffix().isDirty()) {
                        notCompatiblePart = notCompatiblePart == null ? "" : notCompatiblePart + ", ";
                        notCompatiblePart += YADEArgumentsHelper.toString(targetArgs.getAtomicSuffix());
                    }
                }
                throw new YADEEngineInitializationException(startPart + notCompatiblePart + " (temporary target files used)");
            }

            if (!targetArgs.getOverwriteFiles().isTrue()) {
                targetArgs.getOverwriteFiles().setValue(Boolean.valueOf(true));
                logger.info("[%s][%s]due to %s", YADETargetArguments.LABEL, YADEArgumentsHelper.toStringAsOppositeValue(targetArgs
                        .getOverwriteFiles()), YADEArgumentsHelper.toString(targetArgs.getAppendFiles()));
            }
        }

    }

    private static void validateSourceTargetArguments(YADEArguments args, YADESourceArguments sourceArgs, YADETargetArguments targetArgs,
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

    private static void replaceExpressions(ISOSLogger logger, String label, SOSArgument<String> arg) throws YADEEngineInitializationException {
        try {
            String val = arg.getValue();
            YADEExpressionResolver.replaceDateExpressions(arg);
            if (logger != null && !val.equals(arg.getValue())) {
                logger.info("[" + label + "]" + YADEArgumentsHelper.toString(arg));
            }
        } catch (Exception e) {
            throw new YADEEngineInitializationException("[" + label + "]" + e.toString());
        }
    }

    private static void replaceExpressionsForListArg(ISOSLogger logger, String label, SOSArgument<List<String>> arg)
            throws YADEEngineInitializationException {
        try {
            List<String> val = arg.getValue();
            YADEExpressionResolver.replaceDateExpressionsForListArg(arg);
            if (logger != null && !val.equals(arg.getValue())) {
                logger.info("[" + label + "]" + YADEArgumentsHelper.toStringFromListString(arg));
            }
        } catch (Exception e) {
            throw new YADEEngineInitializationException("[" + label + "]" + e.toString());
        }
    }
}
