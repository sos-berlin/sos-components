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
import com.sos.commons.vfs.ssh.SSHProvider;
import com.sos.yade.commons.Yade.TransferEntryState;
import com.sos.yade.commons.Yade.TransferOperation;
import com.sos.yade.engine.commons.YADEProviderFile;
import com.sos.yade.engine.commons.arguments.YADEArguments;
import com.sos.yade.engine.commons.arguments.YADEClientArguments;
import com.sos.yade.engine.commons.arguments.YADEJumpHostArguments;
import com.sos.yade.engine.commons.arguments.YADESourceArguments;
import com.sos.yade.engine.commons.arguments.YADESourceArguments.ZeroByteTransfer;
import com.sos.yade.engine.commons.arguments.YADETargetArguments;
import com.sos.yade.engine.commons.arguments.loaders.AYADEArgumentsLoader;

public class YADEClientBannerWriter {

    private static final String SEPARATOR_LINE = "**************************************************************";

    public static void writeHeader(ISOSLogger logger, AYADEArgumentsLoader argsLoader, boolean writeYADEBanner) {
        if (writeYADEBanner) {
            logger.info(SEPARATOR_LINE);
            logger.info(String.format("*    YADE    - %-46s%s", "Managed File Transfer (www.sos-berlin.com)", "*"));
            logger.info(String.format("*    Version - %-46s%s", SOSVersionInfo.VERSION_STRING, "*"));
            logger.info(SEPARATOR_LINE);
        }
        writeTransferHeader(logger, argsLoader.getArgs(), argsLoader.getTargetArgs());
        writeClientHeader(logger, argsLoader.getClientArgs());
        writeSourceHeader(logger, argsLoader.getSourceArgs());
        writeJumpHostHeader(logger, argsLoader.getJumpHostArgs());
        writeTargetHeader(logger, argsLoader.getTargetArgs());

        logger.info(SEPARATOR_LINE);
    }

    private static void writeTransferHeader(ISOSLogger logger, YADEArguments args, YADETargetArguments targetArgs) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(YADEArguments.LABEL).append("]");
        sb.append(YADEArgumentsHelper.toString(args.getOperation()));
        if (targetArgs != null) {
            sb.append(",").append(YADEArgumentsHelper.toString(args.getTransactional()));
        }
        if (!args.getSettings().isEmpty()) {
            sb.append(",").append(YADEArgumentsHelper.toString(args.getSettings()));
        }
        if (!args.getProfile().isEmpty()) {
            sb.append(",").append(YADEArgumentsHelper.toString(args.getProfile()));
        }
        if (args.isParallelismEnabled()) {
            sb.append(",").append(YADEArgumentsHelper.toString(args.getParallelism()));
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
        if (!clientArgs.getResultSetFileName().isEmpty()) {
            l.add(YADEArgumentsHelper.toString(clientArgs.getResultSetFileName()));
        }
        if (!clientArgs.getExpectedSizeOfResultSet().isEmpty()) {
            l.add(YADEArgumentsHelper.toString(clientArgs.getExpectedSizeOfResultSet()));
        }
        if (!clientArgs.getRaiseErrorIfResultSetIs().isEmpty()) {
            l.add(YADEArgumentsHelper.toString(clientArgs.getRaiseErrorIfResultSetIs()));
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
        sb.append("[").append(YADESourceArguments.LABEL).append("]");
        sb.append(YADEArgumentsHelper.toString(sourceArgs.getProvider().getProtocol()));
        sb.append(",source_dir=").append(sourceArgs.getDirectory().getDisplayValue());

        // File selection
        sb.append(",").append(YADEArgumentsHelper.toString(sourceArgs.getForceFiles()));
        sb.append(",").append(YADEArgumentsHelper.toString(sourceArgs.getRecursive()));
        if (sourceArgs.isSingleFilesSelection()) {
            if (sourceArgs.isFileListEnabled()) {
                sb.append(",").append(YADEArgumentsHelper.toString(sourceArgs.getFileList()));
            } else if (sourceArgs.isFilePathEnabled()) {
                sb.append(",").append(YADEArgumentsHelper.toString(sourceArgs.getFilePath()));
            }
        } else {
            sb.append(",").append(YADEArgumentsHelper.toString(sourceArgs.getFileSpec()));
        }
        if (!sourceArgs.getExcludedDirectories().isEmpty()) {
            sb.append(",").append(YADEArgumentsHelper.toString(sourceArgs.getExcludedDirectories()));
        }
        if (!ZeroByteTransfer.YES.equals(sourceArgs.getZeroByteTransfer().getValue())) {
            sb.append(",").append(YADEArgumentsHelper.toString(sourceArgs.getZeroByteTransfer()));
        }
        if (!sourceArgs.getMaxFiles().isEmpty()) {
            sb.append(",").append(YADEArgumentsHelper.toString(sourceArgs.getMaxFiles()));
        }
        if (!sourceArgs.getMaxFileSize().isEmpty()) {
            sb.append(",").append(YADEArgumentsHelper.toString(sourceArgs.getMaxFileSize()));
        }
        if (!sourceArgs.getMinFileSize().isEmpty()) {
            sb.append(",").append(YADEArgumentsHelper.toString(sourceArgs.getMinFileSize()));
        }
        if (sourceArgs.getPolling() != null) {
            if (sourceArgs.getPolling().getPollingServer().isTrue()) {
                sb.append(",").append(YADEArgumentsHelper.toString(sourceArgs.getPolling().getPollingServer()));
            }
            if (!sourceArgs.getPolling().getPollingServerDuration().isEmpty()) {
                sb.append(",").append(YADEArgumentsHelper.toString(sourceArgs.getPolling().getPollingServerDuration()));
            }
            if (sourceArgs.getPolling().getPollingServerPollForever().isTrue()) {
                sb.append(",").append(YADEArgumentsHelper.toString(sourceArgs.getPolling().getPollingServerPollForever()));
            }
            if (sourceArgs.getPolling().getPollingWait4SourceFolder().isTrue()) {
                sb.append(",").append(YADEArgumentsHelper.toString(sourceArgs.getPolling().getPollingWait4SourceFolder()));
            }
            if (sourceArgs.getPolling().getWaitingForLateComers().isTrue()) {
                sb.append(",").append(YADEArgumentsHelper.toString(sourceArgs.getPolling().getWaitingForLateComers()));
            }

            if (!sourceArgs.getPolling().getPollInterval().isEmpty()) {
                sb.append(",").append(YADEArgumentsHelper.toString(sourceArgs.getPolling().getPollInterval()));
            }
            if (!sourceArgs.getPolling().getPollMinFiles().isEmpty()) {
                sb.append(",").append(YADEArgumentsHelper.toString(sourceArgs.getPolling().getPollMinFiles()));
            }
            if (!sourceArgs.getPolling().getPollTimeout().isEmpty()) {
                sb.append(",").append(YADEArgumentsHelper.toString(sourceArgs.getPolling().getPollTimeout()));
            }
        }

        // Replacement
        if (sourceArgs.isReplacementEnabled()) {
            sb.append(",").append(YADEArgumentsHelper.toString(sourceArgs.getReplacing()));
            sb.append(",").append(YADEArgumentsHelper.toString(sourceArgs.getReplacement()));
        }

        // Integrity Hash
        if (sourceArgs.getCheckIntegrityHash().isTrue()) {
            sb.append(",").append(YADEArgumentsHelper.toString(sourceArgs.getCheckIntegrityHash()));
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
        sb.append("[").append(YADEJumpHostArguments.LABEL).append("]");
        sb.append(YADEArgumentsHelper.toString(jumpHostArgs.getProvider().getProtocol()));
        sb.append(",").append(SSHProvider.getAccessInfo(jumpHostArgs.getProvider()));
        sb.append(",command=").append(jumpHostArgs.getYADEClientCommand().getDisplayValue());

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
        sb.append("[").append(YADETargetArguments.LABEL).append("]");
        sb.append(YADEArgumentsHelper.toString(targetArgs.getProvider().getProtocol()));
        sb.append(",target_dir=").append(targetArgs.getDirectory().getDisplayValue());
        sb.append(",").append(YADEArgumentsHelper.toString(targetArgs.getCreateDirectories()));
        sb.append(",").append(YADEArgumentsHelper.toString(targetArgs.getCheckSize()));
        sb.append(",").append(YADEArgumentsHelper.toString(targetArgs.getOverwriteFiles()));
        if (targetArgs.getAppendFiles().isTrue()) {
            sb.append(",").append(YADEArgumentsHelper.toString(targetArgs.getAppendFiles()));
        }
        if (!targetArgs.getAtomicPrefix().isEmpty()) {
            sb.append(",").append(YADEArgumentsHelper.toString(targetArgs.getAtomicPrefix()));
        }
        if (!targetArgs.getAtomicSuffix().isEmpty()) {
            sb.append(",").append(YADEArgumentsHelper.toString(targetArgs.getAtomicSuffix()));
        }
        if (targetArgs.isReplacementEnabled()) {
            sb.append(",").append(YADEArgumentsHelper.toString(targetArgs.getReplacing()));
            sb.append(",").append(YADEArgumentsHelper.toString(targetArgs.getReplacement()));
        }
        if (targetArgs.getKeepModificationDate().isTrue()) {
            sb.append(",").append(YADEArgumentsHelper.toString(targetArgs.getKeepModificationDate()));
        }
        if (targetArgs.getCreateIntegrityHashFile().isTrue()) {
            sb.append(",").append(YADEArgumentsHelper.toString(targetArgs.getCreateIntegrityHashFile()));
        }
        if (!targetArgs.getCumulativeFileName().isEmpty()) {
            sb.append(",").append(YADEArgumentsHelper.toString(targetArgs.getCumulativeFileName()));
            sb.append(",").append(YADEArgumentsHelper.toString(targetArgs.getCumulativeFileDelete()));
            sb.append(",").append(YADEArgumentsHelper.toString(targetArgs.getCumulativeFileSeparator()));
        }

        logger.info(sb);
        if (logger.isDebugEnabled()) {
            logger.debug(YADEArgumentsHelper.toString(logger, YADETargetArguments.LABEL, targetArgs));
            logger.debug(YADEArgumentsHelper.toString(logger, YADETargetArguments.LABEL, targetArgs.getProvider()));
        }
    }

    public static void writeSummary(ISOSLogger logger, YADEArguments args, Duration operationDuration, YADETargetArguments targetArgs,
            List<ProviderFile> files, Throwable error) {
        logger.info(SEPARATOR_LINE);

        args.getEnd().setValue(Instant.now());
        int totalFiles = files == null ? 0 : files.size();

        StringBuilder sb = new StringBuilder("[Summary]");
        sb.append("total_duration=").append(SOSDate.getDuration(args.getStart().getValue(), args.getEnd().getValue()));
        if (operationDuration != null) {
            sb.append("(operation=").append(SOSDate.getDuration(operationDuration)).append(")");
        }
        sb.append(",total_files=").append(totalFiles);

        if (totalFiles > 0 && !TransferOperation.GETLIST.equals(args.getOperation().getValue())) {
            List<String> l = new ArrayList<>();

            Map<String, List<YADEProviderFile>> groupedByState = FileStateUtils.getGroupedByState(targetArgs, files, getDefaultState(args));
            boolean needsDetails = groupedByState.keySet().stream().anyMatch(k -> k.contains(TransferEntryState.ROLLED_BACK.name()) || k.contains(
                    TransferEntryState.FAILED.name()));

            FileStateUtils.getGroupedByState(targetArgs, files, getDefaultState(args)).forEach((state, fileList) -> {
                l.add(state.toLowerCase() + "=" + fileList.size());

                if (needsDetails) {
                    logger.info(state.toLowerCase() + ":");
                    for (YADEProviderFile file : fileList) {
                        logger.info(" Source " + file);
                    }
                }

            });
            int size = l.size();
            if (size > 0) {
                // TODO e.g. detect if cumulative file...
                if (size == 1 && l.get(0).startsWith(getDefaultState(args).toString().toLowerCase())) {

                } else {
                    sb.append("(").append(String.join(",", l)).append(")");
                }
            }
        }
        logger.info(sb);

        if (error != null) {
            logger.error("[Error]%s", error.toString());
        }
    }

    private static TransferEntryState getDefaultState(YADEArguments args) {
        return TransferEntryState.UNKNOWN;
    }

    private class FileStateUtils {

        private static Map<String, List<YADEProviderFile>> getGroupedByState(YADETargetArguments targetArgs, List<ProviderFile> files,
                TransferEntryState defaultState) {
            // skipping the RENAMED subState if the renaming was due to an atomic transfer and not due to a defined replacement
            final boolean skipRenameSubStateForTarget = targetArgs != null && !targetArgs.isReplacementEnabled();
            return files.stream().map(f -> (YADEProviderFile) f).collect(Collectors.groupingBy(f -> getState(f, defaultState,
                    skipRenameSubStateForTarget)));
        }

        private static String getState(YADEProviderFile f, TransferEntryState defaultState, boolean skipRenameSubStateForTarget) {
            if (f.getTarget() != null) {
                return resolve(f.getTarget(), defaultState, skipRenameSubStateForTarget);
            }
            return resolve(f, defaultState, false);
        }

        private static String resolve(YADEProviderFile f, TransferEntryState defaultState, boolean skipRenameSubStateForTarget) {
            String state = f.getState() == null ? defaultState.toString() : f.getState().toString();
            String subState = getSubState(f, skipRenameSubStateForTarget);
            return state + subState;
        }

        private static String getSubState(YADEProviderFile target, boolean skipRenameSubState) {
            if (target.getSubState() == null || (skipRenameSubState && TransferEntryState.RENAMED.equals(target.getSubState()))) {
                return "";
            }
            return "(" + target.getSubState() + ")";
        }
    }

}
