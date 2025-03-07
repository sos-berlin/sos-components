package com.sos.yade.engine.commons.helpers;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sos.commons.util.SOSDate;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.yade.commons.Yade.TransferEntryState;
import com.sos.yade.commons.Yade.TransferOperation;
import com.sos.yade.engine.commons.YADEProviderFile;
import com.sos.yade.engine.commons.arguments.YADEArguments;
import com.sos.yade.engine.commons.arguments.YADEClientArguments;
import com.sos.yade.engine.commons.arguments.YADESourceArguments;
import com.sos.yade.engine.commons.arguments.YADESourceArguments.ZeroByteTransfer;
import com.sos.yade.engine.commons.arguments.YADETargetArguments;
import com.sos.yade.engine.commons.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.commons.delegators.YADETargetProviderDelegator;

public class YADEClientBannerWriter {

    private static final String SEPARATOR_LINE = "**************************************************************";

    private static final String LOG_PREFIX_TRANSFER_ARGUMENS = "[Transfer]";
    private static final String LOG_PREFIX_CLIENT_ARGUMENS = "[Client]";

    // TODO use String.format
    public static void writeHeader(ISOSLogger logger, YADEArguments args, YADEClientArguments clientArgs, YADESourceArguments sourcesArgs,
            YADETargetArguments targetArgs, boolean writeYADEBanner) {
        if (writeYADEBanner) {
            logger.info(SEPARATOR_LINE);
            logger.info("*    YADE    - Managed File Transfer (www.sos-berlin.com)    *");
            logger.info("*    Version - xyz                                           *");
            logger.info(SEPARATOR_LINE);
        }
        writeTransferHeader(logger, args, targetArgs);
        writeClientHeader(logger, clientArgs);
        writeSourceHeader(logger, sourcesArgs);
        writeTargetHeader(logger, targetArgs);

        logger.info(SEPARATOR_LINE);
    }

    private static void writeTransferHeader(ISOSLogger logger, YADEArguments args, YADETargetArguments targetArgs) {
        StringBuilder sb = new StringBuilder(LOG_PREFIX_TRANSFER_ARGUMENS);
        sb.append(YADEArgumentsHelper.toString(args.getOperation()));
        if (targetArgs != null) {
            sb.append(",").append(YADEArgumentsHelper.toString(targetArgs.getTransactional()));
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
            logger.debug(YADEArgumentsHelper.toString(logger, LOG_PREFIX_TRANSFER_ARGUMENS, args));
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
            logger.info(LOG_PREFIX_CLIENT_ARGUMENS + String.join(",", l));
        }
        if (logger.isDebugEnabled()) {
            logger.debug(YADEArgumentsHelper.toString(logger, LOG_PREFIX_CLIENT_ARGUMENS, clientArgs));
        }
    }

    private static void writeSourceHeader(ISOSLogger logger, YADESourceArguments sourceArgs) {
        StringBuilder sb = new StringBuilder(YADESourceProviderDelegator.LOG_PREFIX);
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
            logger.debug(YADEArgumentsHelper.toString(logger, YADESourceProviderDelegator.LOG_PREFIX, sourceArgs));
            logger.debug(YADEArgumentsHelper.toString(logger, YADESourceProviderDelegator.LOG_PREFIX, sourceArgs.getProvider()));
        }
    }

    private static void writeTargetHeader(ISOSLogger logger, YADETargetArguments targetArgs) {
        if (targetArgs == null) {
            return;
        }

        StringBuilder sb = new StringBuilder(YADETargetProviderDelegator.LOG_PREFIX);
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
            logger.debug(YADEArgumentsHelper.toString(logger, YADETargetProviderDelegator.LOG_PREFIX, targetArgs));
            logger.debug(YADEArgumentsHelper.toString(logger, YADETargetProviderDelegator.LOG_PREFIX, targetArgs.getProvider()));
        }
    }

    public static void writeSummary(ISOSLogger logger, Instant totalStart, Duration operationDuration, YADEArguments args,
            YADETargetArguments targetArgs, List<ProviderFile> files, Throwable error) {
        logger.info(SEPARATOR_LINE);

        int totalFiles = files == null ? 0 : files.size();

        StringBuilder sb = new StringBuilder("[Summary]");
        sb.append("total_duration=").append(SOSDate.getDuration(totalStart, Instant.now()));
        if (operationDuration != null) {
            sb.append("(operation=").append(SOSDate.getDuration(operationDuration)).append(")");
        }
        sb.append(",total_files=").append(totalFiles);

        if (totalFiles > 0 && !TransferOperation.GETLIST.equals(args.getOperation().getValue())) {
            List<String> l = new ArrayList<>();
            FileStateUtils.getGroupedByState(targetArgs, files, getDefaultState(args)).forEach((state, fileList) -> {
                l.add(state.toLowerCase() + "=" + fileList.size());
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
