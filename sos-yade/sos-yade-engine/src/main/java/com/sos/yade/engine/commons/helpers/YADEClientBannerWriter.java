package com.sos.yade.engine.commons.helpers;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSVersionInfo;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.commons.vfs.exceptions.ProviderInitializationException;
import com.sos.yade.commons.Yade.TransferEntryState;
import com.sos.yade.commons.Yade.TransferOperation;
import com.sos.yade.engine.addons.YADEEngineJumpHostAddon;
import com.sos.yade.engine.commons.YADEProviderFile;
import com.sos.yade.engine.commons.arguments.YADEArguments;
import com.sos.yade.engine.commons.arguments.YADEClientArguments;
import com.sos.yade.engine.commons.arguments.YADEJumpHostArguments;
import com.sos.yade.engine.commons.arguments.YADESourceArguments;
import com.sos.yade.engine.commons.arguments.YADESourceArguments.ZeroByteTransfer;
import com.sos.yade.engine.commons.arguments.YADETargetArguments;
import com.sos.yade.engine.commons.arguments.loaders.AYADEArgumentsLoader;
import com.sos.yade.engine.commons.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.commons.delegators.YADETargetProviderDelegator;
import com.sos.yade.engine.handlers.operations.copymove.file.commons.YADETargetProviderFile;

public class YADEClientBannerWriter {

    public static final String SEPARATOR_LINE = "****************************************************************************************";

    private static final String NEW_LINE = "\n";
    private static final boolean STATE_TO_LOWERCASE = true;

    public static String formatState(TransferEntryState state) {
        if (state == null) {
            return "";
        }
        return formatState(state.name());
    }

    public static void writeHeader(ISOSLogger logger, AYADEArgumentsLoader argsLoader, boolean writeYADEBanner) {
        if (writeYADEBanner) {
            logger.info(SEPARATOR_LINE);
            logger.info(String.format("*    YADE    - %-72s%s", "Managed File Transfer (www.sos-berlin.com)", "*"));
            logger.info(String.format("*    Version - %-72s%s", SOSVersionInfo.VERSION_BUILD_DATE_AND_NUMBER, "*"));
            logger.info(SEPARATOR_LINE);
        }
        writeTransferHeader(logger, argsLoader.getArgs());
        writeClientHeader(logger, argsLoader.getClientArgs());
        writeSourceHeader(logger, argsLoader.getSourceArgs());
        writeJumpHostHeader(logger, argsLoader.getJumpHostArgs());
        writeTargetHeader(logger, argsLoader.getTargetArgs());

        logger.info(SEPARATOR_LINE);
    }

    public static void writeSummary(ISOSLogger logger, YADEArguments args, Duration operationDuration, YADESourceProviderDelegator sourceDelegator,
            YADETargetProviderDelegator targetDelegator, YADEEngineJumpHostAddon jumpHostAddon, List<ProviderFile> files, Throwable error) {

        args.getEnd().setValue(Instant.now());
        int totalFiles = files == null ? 0 : files.size();

        // Start Calculation
        String summaryLineFilesSummary = null;
        StringBuilder detailsLines = new StringBuilder();
        if (totalFiles > 0 && !TransferOperation.GETLIST.equals(args.getOperation().getValue())) {
            List<String> l = new ArrayList<>();

            Map<String, List<YADEProviderFile>> groupedByState = FileStateUtils.getGroupedByState(targetDelegator, files, getDefaultState(args));
            boolean needsDetails = groupedByState.keySet().stream().anyMatch(k -> k.contains(TransferEntryState.ROLLED_BACK.name()) || k.contains(
                    TransferEntryState.FAILED.name()));

            groupedByState.forEach((state, fileList) -> {
                l.add(formatState(state) + "=" + fileList.size());

                if (needsDetails) {
                    detailsLines.append(formatState(state) + ":").append(NEW_LINE);
                    for (YADEProviderFile file : fileList) {
                        detailsLines.append(formatFile(sourceDelegator, targetDelegator, file));
                    }
                }

            });
            int size = l.size();
            if (size > 0) {
                // TODO e.g. detect if cumulative file...
                if (size == 1 && l.get(0).startsWith(formatState(getDefaultState(args)))) {

                } else {
                    summaryLineFilesSummary = "(" + String.join(",", l) + ")";
                }
            }
        }
        StringBuilder summaryLine = new StringBuilder();
        summaryLine.append("Files=").append(totalFiles);
        if (summaryLineFilesSummary != null) {
            summaryLine.append(summaryLineFilesSummary);
        }
        summaryLine.append(", Duration=").append(SOSDate.getDuration(args.getStart().getValue(), args.getEnd().getValue()));
        if (operationDuration != null) {
            summaryLine.append("(Operation=").append(SOSDate.getDuration(operationDuration)).append(")");
        }

        // Start Log Output
        logger.info(SEPARATOR_LINE);
        logger.info("[Summary]" + summaryLine);
        if (detailsLines.length() > 0) {
            logger.info("[Details]" + detailsLines);
        }
        // Error
        if (error != null) {
            logger.error("[Error]" + error.toString());
        }
    }

    private static void writeTransferHeader(ISOSLogger logger, YADEArguments args) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(YADEArguments.LABEL).append("]");
        sb.append(YADEArgumentsHelper.toString(args.getOperation()));
        sb.append(", ").append(YADEArgumentsHelper.toString(args.getTransactional()));
        if (args.isParallelismEnabled()) {
            sb.append(", ").append(YADEArgumentsHelper.toString("Parallelism", args.getParallelism()));
        }
        if (!args.getProfile().isEmpty()) {
            sb.append(", ").append(YADEArgumentsHelper.toString("Profile", args.getProfile()));
        }
        if (!args.getSettings().isEmpty()) {
            sb.append(", ").append(YADEArgumentsHelper.toString("Settings", args.getSettings()));
        }
        logger.info(sb);
        if (logger.isDebugEnabled()) {
            logger.debug(YADEArgumentsHelper.toString(logger, YADEArguments.LABEL, args));
        }
    }

    private static void writeClientHeader(ISOSLogger logger, YADEClientArguments clientArgs) {
        if (clientArgs == null) {
            return;
        }
        List<String> l = new ArrayList<>();
        // ResultSet
        if (!clientArgs.getResultSetFile().isEmpty()) {
            l.add(YADEArgumentsHelper.toString(clientArgs.getResultSetFile()));
        }
        if (!clientArgs.getRaiseErrorIfResultSetIs().isEmpty()) {
            l.add(YADEArgumentsHelper.comparisonOperatorToString(clientArgs.getRaiseErrorIfResultSetIs()));
        }
        if (!clientArgs.getExpectedResultSetCount().isEmpty()) {
            l.add(YADEArgumentsHelper.toString(clientArgs.getExpectedResultSetCount()));
        }
        // E-Mail
        if (clientArgs.getMailOnEmptyFiles().isTrue()) {
            l.add(YADEArgumentsHelper.toString(clientArgs.getMailOnEmptyFiles()));
        }
        if (clientArgs.getMailOnError().isTrue()) {
            l.add(YADEArgumentsHelper.toString(clientArgs.getMailOnError()));
        }
        if (clientArgs.getMailOnSuccess().isTrue()) {
            l.add(YADEArgumentsHelper.toString(clientArgs.getMailOnSuccess()));
        }

        if (l.size() > 0) {
            logger.info("[" + YADEClientArguments.LABEL + "]" + String.join(",", l));
        }
        if (logger.isDebugEnabled()) {
            logger.debug(YADEArgumentsHelper.toString(logger, YADEClientArguments.LABEL, clientArgs));
        }
    }

    private static void writeSourceHeader(ISOSLogger logger, YADESourceArguments sourceArgs) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(sourceArgs.getLabel().isEmpty() ? YADESourceArguments.LABEL : sourceArgs.getLabel().getValue()).append("]");
        sb.append(YADEArgumentsHelper.toString("Protocol", sourceArgs.getProvider().getProtocol()));
        try {
            sb.append("(").append(sourceArgs.getProvider().getAccessInfo()).append(")");
        } catch (ProviderInitializationException e) {
            sb.append("[getAccessInfo]" + e);
            logger.error("[getAccessInfo]" + e, e);
        }
        if (sourceArgs.getDirectory().getValue() != null) {
            sb.append(", ").append(YADEArgumentsHelper.toString(sourceArgs.getDirectory()));
        }
        sb.append(", ").append(YADEArgumentsHelper.toString(sourceArgs.getRecursive()));
        // File selection
        if (sourceArgs.isSingleFilesSelection()) {
            if (sourceArgs.isFileListEnabled()) {
                sb.append(", ").append(YADEArgumentsHelper.toString(sourceArgs.getFileList()));
            } else if (sourceArgs.isFilePathEnabled()) {
                sb.append(", ").append(YADEArgumentsHelper.toString(sourceArgs.getFilePath().getName(), sourceArgs.getFilePathAsString()));
            }
        } else {
            sb.append(", ").append(YADEArgumentsHelper.toString(sourceArgs.getFileSpec()));
        }
        // Others
        if (!sourceArgs.getExcludedDirectories().isEmpty()) {
            sb.append(", ").append(YADEArgumentsHelper.toString(sourceArgs.getExcludedDirectories()));
        }
        if (sourceArgs.getErrorOnNoFilesFound().isDirty()) {
            sb.append(", ").append(YADEArgumentsHelper.toStringAsOppositeValue(sourceArgs.getErrorOnNoFilesFound()));
        }
        if (!ZeroByteTransfer.YES.equals(sourceArgs.getZeroByteTransfer().getValue())) {
            sb.append(", ").append(YADEArgumentsHelper.toString(sourceArgs.getZeroByteTransfer()));
        }
        if (!sourceArgs.getMaxFiles().isEmpty()) {
            sb.append(", ").append(YADEArgumentsHelper.toString(sourceArgs.getMaxFiles()));
        }
        if (!sourceArgs.getMaxFileSize().isEmpty()) {
            sb.append(", ").append(YADEArgumentsHelper.toString(sourceArgs.getMaxFileSize()));
        }
        if (!sourceArgs.getMinFileSize().isEmpty()) {
            sb.append(", ").append(YADEArgumentsHelper.toString(sourceArgs.getMinFileSize()));
        }
        if (sourceArgs.getPolling() != null) {
            if (sourceArgs.getPolling().getPollingServer().isTrue()) {
                sb.append(", ").append(YADEArgumentsHelper.toString(sourceArgs.getPolling().getPollingServer()));
            }
            if (!sourceArgs.getPolling().getPollingServerDuration().isEmpty()) {
                sb.append(", ").append(YADEArgumentsHelper.toString(sourceArgs.getPolling().getPollingServerDuration()));
            }
            if (sourceArgs.getPolling().getPollingServerPollForever().isTrue()) {
                sb.append(", ").append(YADEArgumentsHelper.toString(sourceArgs.getPolling().getPollingServerPollForever()));
            }
            if (sourceArgs.getPolling().getPollingWait4SourceFolder().isTrue()) {
                sb.append(", ").append(YADEArgumentsHelper.toString(sourceArgs.getPolling().getPollingWait4SourceFolder()));
            }
            if (sourceArgs.getPolling().getWaitingForLateComers().isTrue()) {
                sb.append(", ").append(YADEArgumentsHelper.toString(sourceArgs.getPolling().getWaitingForLateComers()));
            }

            if (!sourceArgs.getPolling().getPollInterval().isEmpty()) {
                sb.append(", ").append(YADEArgumentsHelper.toString(sourceArgs.getPolling().getPollInterval()));
            }
            if (!sourceArgs.getPolling().getPollMinFiles().isEmpty()) {
                sb.append(", ").append(YADEArgumentsHelper.toString(sourceArgs.getPolling().getPollMinFiles()));
            }
            if (!sourceArgs.getPolling().getPollTimeout().isEmpty()) {
                sb.append(", ").append(YADEArgumentsHelper.toString(sourceArgs.getPolling().getPollTimeout()));
            }
        }

        // Replacement
        if (sourceArgs.isReplacementEnabled()) {
            sb.append(", ").append(YADEArgumentsHelper.toString(sourceArgs.getReplacing()));
            sb.append(", ").append(YADEArgumentsHelper.toString(sourceArgs.getReplacement()));
        }

        // Integrity Hash
        if (sourceArgs.getCheckIntegrityHash().isTrue()) {
            sb.append(", ").append(YADEArgumentsHelper.toString(sourceArgs.getCheckIntegrityHash()));
        }

        logger.info(sb);
        if (logger.isDebugEnabled()) {
            logger.debug(YADEArgumentsHelper.toString(logger, YADESourceArguments.LABEL, sourceArgs));
            logger.debug(YADEArgumentsHelper.toString(logger, YADESourceArguments.LABEL, sourceArgs.getProvider()));
        }
    }

    private static void writeJumpHostHeader(ISOSLogger logger, YADEJumpHostArguments jumpHostArgs) {
        if (jumpHostArgs == null) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[ ").append(YADEJumpHostArguments.LABEL).append(" ]");
        sb.append(YADEArgumentsHelper.toString("Protocol", jumpHostArgs.getProvider().getProtocol()));
        try {
            sb.append("(").append(jumpHostArgs.getAccessInfo()).append(")");
        } catch (ProviderInitializationException e) {
            sb.append("[getAccessInfo]" + e);
            logger.error("[getAccessInfo]" + e, e);
        }
        if (jumpHostArgs.getDirectory().getValue() != null) {
            sb.append(", ").append(YADEArgumentsHelper.toString(jumpHostArgs.getDirectory()));
        }
        sb.append(", ").append(YADEArgumentsHelper.toString(jumpHostArgs.getYADEClientCommand()));

        logger.info(sb);
        if (logger.isDebugEnabled()) {
            logger.debug(YADEArgumentsHelper.toString(logger, YADEJumpHostArguments.LABEL, jumpHostArgs));
            logger.debug(YADEArgumentsHelper.toString(logger, YADEJumpHostArguments.LABEL, jumpHostArgs.getProvider()));
        }
    }

    private static void writeTargetHeader(ISOSLogger logger, YADETargetArguments targetArgs) {
        if (targetArgs == null) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[").append(targetArgs.getLabel().isEmpty() ? YADETargetArguments.LABEL : targetArgs.getLabel().getValue()).append("]");
        sb.append(YADEArgumentsHelper.toString("Protocol", targetArgs.getProvider().getProtocol()));
        try {
            sb.append("(").append(targetArgs.getProvider().getAccessInfo()).append(")");
        } catch (ProviderInitializationException e) {
            sb.append("[getAccessInfo]" + e);
            logger.error("[getAccessInfo]" + e, e);
        }
        if (targetArgs.getDirectory().getValue() != null) {
            sb.append(", ").append(YADEArgumentsHelper.toString(targetArgs.getDirectory()));
        }
        if (targetArgs.getCreateDirectories().isDirty()) {
            sb.append(", ").append(YADEArgumentsHelper.toStringAsOppositeValue(targetArgs.getCreateDirectories()));
        }
        if (targetArgs.getCheckSize().isDirty()) {
            sb.append(", ").append(YADEArgumentsHelper.toString(targetArgs.getCheckSize()));
        }
        if (targetArgs.getOverwriteFiles().isDirty()) {
            sb.append(", ").append(YADEArgumentsHelper.toStringAsOppositeValue(targetArgs.getOverwriteFiles()));
        }
        if (targetArgs.getAppendFiles().isTrue()) {
            sb.append(", ").append(YADEArgumentsHelper.toString(targetArgs.getAppendFiles()));
        }
        if (!targetArgs.getAtomicPrefix().isEmpty()) {
            sb.append(", ").append(YADEArgumentsHelper.toString(targetArgs.getAtomicPrefix()));
        }
        if (!targetArgs.getAtomicSuffix().isEmpty()) {
            sb.append(", ").append(YADEArgumentsHelper.toString(targetArgs.getAtomicSuffix()));
        }
        if (targetArgs.isReplacementEnabled()) {
            sb.append(", ").append(YADEArgumentsHelper.toString(targetArgs.getReplacing()));
            sb.append(", ").append(YADEArgumentsHelper.toString(targetArgs.getReplacement()));
        }
        if (targetArgs.getKeepModificationDate().isTrue()) {
            sb.append(", ").append(YADEArgumentsHelper.toString(targetArgs.getKeepModificationDate()));
        }
        if (targetArgs.getCreateIntegrityHashFile().isTrue()) {
            sb.append(", ").append(YADEArgumentsHelper.toString(targetArgs.getCreateIntegrityHashFile()));
        }
        if (!targetArgs.getCumulativeFileName().isEmpty()) {
            sb.append(", ").append(YADEArgumentsHelper.toString(targetArgs.getCumulativeFileName()));
            sb.append(", ").append(YADEArgumentsHelper.toString(targetArgs.getCumulativeFileDelete()));
            sb.append(", ").append(YADEArgumentsHelper.toString(targetArgs.getCumulativeFileSeparator()));
        }

        logger.info(sb);
        if (logger.isDebugEnabled()) {
            logger.debug(YADEArgumentsHelper.toString(logger, YADETargetArguments.LABEL, targetArgs));
            logger.debug(YADEArgumentsHelper.toString(logger, YADETargetArguments.LABEL, targetArgs.getProvider()));
        }
    }

    private static String formatState(String state) {
        return STATE_TO_LOWERCASE ? state.toLowerCase() : state;
    }

    private static String formatFile(YADESourceProviderDelegator sourceDelegator, YADETargetProviderDelegator targetDelegator,
            YADEProviderFile file) {
        StringBuilder sb = new StringBuilder();
        sb.append(" [").append(sourceDelegator.getLabel()).append("]");
        sb.append(formatFile(file));
        if (file.getTarget() != null) {
            sb.append(" [").append(targetDelegator.getLabel()).append("]");
            sb.append(formatFile(file.getTarget()));
        }
        return sb.toString();
    }

    private static String formatFile(YADEProviderFile file) {
        StringBuilder sb = new StringBuilder();
        if (file.getState() != null) {
            sb.append(formatState(file.getState()));
            if (file.getSubState() != null) {
                sb.append("(").append(formatState(file.getSubState())).append(")");
            }
        }
        sb.append(file.getFinalFullPath());
        return sb.toString();
    }

    private static TransferEntryState getDefaultState(YADEArguments args) {
        return TransferEntryState.UNKNOWN;
    }

    private class FileStateUtils {

        private static Map<String, List<YADEProviderFile>> getGroupedByState(YADETargetProviderDelegator targetDelegator, List<ProviderFile> files,
                TransferEntryState defaultState) {
            return files.stream().map(f -> (YADEProviderFile) f).collect(Collectors.groupingBy(f -> getState(f, defaultState)));
        }

        private static String getState(YADEProviderFile f, TransferEntryState defaultState) {
            // skipping the RENAMED subState if the renaming was due to an atomic transfer and not due to a defined replacement
            if (f.getTarget() != null) {
                YADETargetProviderFile target = (YADETargetProviderFile) f.getTarget();
                return resolve(f.getTarget(), defaultState, !target.isNameReplaced());
            }
            return resolve(f, defaultState, false);
        }

        private static String resolve(YADEProviderFile f, TransferEntryState defaultState, boolean skipRenameSubStateForTarget) {
            String state = f.getState() == null ? defaultState.toString() : f.getState().toString();
            String subState = getSubState(f, skipRenameSubStateForTarget);
            return state + subState;
        }

        private static String getSubState(YADEProviderFile target, boolean skipRenameSubState) {
            if (target.getSubState() == null || skipRenameSubState) {
                return "";
            }
            return "(" + target.getSubState() + ")";
        }
    }

}
