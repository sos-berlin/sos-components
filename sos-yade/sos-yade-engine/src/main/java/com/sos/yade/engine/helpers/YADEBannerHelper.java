package com.sos.yade.engine.helpers;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.sos.commons.util.SOSDate;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.yade.engine.arguments.YADEArguments;
import com.sos.yade.engine.arguments.YADEClientArguments;
import com.sos.yade.engine.arguments.YADESourceArguments;
import com.sos.yade.engine.arguments.YADETargetArguments;

public class YADEBannerHelper {

    private static final String SEPARATOR_LINE = "**************************************************************";

    // TODO use String.format
    public static void printBanner(ISOSLogger logger, YADEParallelProcessingConfig parallelProcessingConfig, YADEArguments args,
            YADEClientArguments clientArgs, YADESourceArguments sourcesArgs, YADETargetArguments targetArgs) {
        logger.info(SEPARATOR_LINE);
        logger.info("*    YADE    - Managed File Transfer (www.sos-berlin.com)    *");
        logger.info("*    Version - xyz                                           *");
        logger.info(SEPARATOR_LINE);

        printTransferBanner(logger, parallelProcessingConfig, args, targetArgs);
        printClientBanner(logger, clientArgs);
        printSourceBanner(logger, sourcesArgs);
        printTargetBanner(logger, targetArgs);

        logger.info(SEPARATOR_LINE);
    }

    private static void printTransferBanner(ISOSLogger logger, YADEParallelProcessingConfig parallelProcessingConfig, YADEArguments args,
            YADETargetArguments targetArgs) {
        StringBuilder sb = new StringBuilder("[Transfer]");
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
        if (parallelProcessingConfig != null) {
            String add = parallelProcessingConfig.isAuto() ? "AUTO" : String.valueOf(parallelProcessingConfig.getMaxThreads());
            sb.append(",").append(args.getParallelMaxThreads().getName()).append("=").append(add);
        }
        logger.info(sb);
    }

    private static void printClientBanner(ISOSLogger logger, YADEClientArguments clientArgs) {
        if (clientArgs == null) {
            return;
        }
        List<String> l = new ArrayList<>();
        // System properties
        if (!clientArgs.getSystemPropertyFiles().isEmpty()) {
            l.add(YADEArgumentsHelper.toString(clientArgs.getSystemPropertyFiles()));
        }

        // ResultSet
        if (!clientArgs.getExpectedSizeOfResultSet().isEmpty()) {
            l.add(YADEArgumentsHelper.toString(clientArgs.getExpectedSizeOfResultSet()));
        }
        if (!clientArgs.getRaiseErrorIfResultSetIs().isEmpty()) {
            l.add(YADEArgumentsHelper.toString(clientArgs.getRaiseErrorIfResultSetIs()));
        }
        if (!clientArgs.getResultListFile().isEmpty()) {
            l.add(YADEArgumentsHelper.toString(clientArgs.getResultListFile()));
        }
        if (!clientArgs.getResultSetFileName().isEmpty()) {
            l.add(YADEArgumentsHelper.toString(clientArgs.getResultSetFileName()));
        }

        // E-Mail
        if (!clientArgs.getMailOnEmptyFiles().isEmpty()) {
            l.add(YADEArgumentsHelper.toString(clientArgs.getMailOnEmptyFiles()));
        }
        if (!clientArgs.getMailOnError().isEmpty()) {
            l.add(YADEArgumentsHelper.toString(clientArgs.getMailOnError()));
        }
        if (!clientArgs.getMailOnSuccess().isEmpty()) {
            l.add(YADEArgumentsHelper.toString(clientArgs.getMailOnSuccess()));
        }

        if (l.size() > 0) {
            logger.info("[Client]" + String.join(",", l));
        }
    }

    private static void printSourceBanner(ISOSLogger logger, YADESourceArguments sourcesArgs) {
        StringBuilder sb = new StringBuilder("[Source]");
        sb.append(YADEArgumentsHelper.toString(sourcesArgs.getProvider().getProtocol()));
        sb.append(",source_dir=").append(sourcesArgs.getDirectory().getDisplayValue());

        // File selection
        sb.append(",").append(YADEArgumentsHelper.toString(sourcesArgs.getForceFiles()));
        sb.append(",").append(YADEArgumentsHelper.toString(sourcesArgs.getRecursive()));
        if (sourcesArgs.isSingleFilesSelection()) {
            if (sourcesArgs.isFileListEnabled()) {
                sb.append(",").append(YADEArgumentsHelper.toString(sourcesArgs.getFileList()));
            } else if (sourcesArgs.isFilePathEnabled()) {
                sb.append(",").append(YADEArgumentsHelper.toString(sourcesArgs.getFilePath()));
            }
        } else {
            sb.append(",").append(YADEArgumentsHelper.toString(sourcesArgs.getFileSpec()));
        }
        if (!sourcesArgs.getExcludedDirectories().isEmpty()) {
            sb.append(",").append(YADEArgumentsHelper.toString(sourcesArgs.getExcludedDirectories()));
        }
        sb.append(",").append(YADEArgumentsHelper.toString(sourcesArgs.getZeroByteTransfer()));
        if (!sourcesArgs.getMaxFiles().isEmpty()) {
            sb.append(",").append(YADEArgumentsHelper.toString(sourcesArgs.getMaxFiles()));
        }
        if (!sourcesArgs.getMaxFileSize().isEmpty()) {
            sb.append(",").append(YADEArgumentsHelper.toString(sourcesArgs.getMaxFileSize()));
        }
        if (!sourcesArgs.getMinFileSize().isEmpty()) {
            sb.append(",").append(YADEArgumentsHelper.toString(sourcesArgs.getMinFileSize()));
        }
        if (sourcesArgs.getPolling() != null) {
            if (sourcesArgs.getPolling().getPollingServer().isTrue()) {
                sb.append(",").append(YADEArgumentsHelper.toString(sourcesArgs.getPolling().getPollingServer()));
            }
            if (!sourcesArgs.getPolling().getPollingServerDuration().isEmpty()) {
                sb.append(",").append(YADEArgumentsHelper.toString(sourcesArgs.getPolling().getPollingServerDuration()));
            }
            if (sourcesArgs.getPolling().getPollingServerPollForever().isTrue()) {
                sb.append(",").append(YADEArgumentsHelper.toString(sourcesArgs.getPolling().getPollingServerPollForever()));
            }
            if (sourcesArgs.getPolling().getPollingWait4SourceFolder().isTrue()) {
                sb.append(",").append(YADEArgumentsHelper.toString(sourcesArgs.getPolling().getPollingWait4SourceFolder()));
            }
            if (sourcesArgs.getPolling().getWaitingForLateComers().isTrue()) {
                sb.append(",").append(YADEArgumentsHelper.toString(sourcesArgs.getPolling().getWaitingForLateComers()));
            }

            if (!sourcesArgs.getPolling().getPollInterval().isEmpty()) {
                sb.append(",").append(YADEArgumentsHelper.toString(sourcesArgs.getPolling().getPollInterval()));
            }
            if (!sourcesArgs.getPolling().getPollMinFiles().isEmpty()) {
                sb.append(",").append(YADEArgumentsHelper.toString(sourcesArgs.getPolling().getPollMinFiles()));
            }
            if (!sourcesArgs.getPolling().getPollTimeout().isEmpty()) {
                sb.append(",").append(YADEArgumentsHelper.toString(sourcesArgs.getPolling().getPollTimeout()));
            }
        }

        // Replacement
        if (sourcesArgs.isReplacementEnabled()) {
            sb.append(",").append(YADEArgumentsHelper.toString(sourcesArgs.getReplacing()));
            sb.append(",").append(YADEArgumentsHelper.toString(sourcesArgs.getReplacement()));
        }

        // Integrity Hash
        if (sourcesArgs.getCheckIntegrityHash().isTrue()) {
            sb.append(",").append(YADEArgumentsHelper.toString(sourcesArgs.getCheckIntegrityHash()));
        }

        logger.info(sb);
    }

    private static void printTargetBanner(ISOSLogger logger, YADETargetArguments targetArgs) {
        if (targetArgs == null) {
            return;
        }

        StringBuilder sb = new StringBuilder("[Target]");
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
    }

    public static void printSummary(ISOSLogger logger, Instant start, YADEArguments args, List<ProviderFile> files, Throwable exception) {
        logger.info(SEPARATOR_LINE);
        logger.info(files.size() + " file(s) " + SOSDate.getDuration(start, Instant.now()));
    }

}
