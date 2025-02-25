package com.sos.yade.engine.handlers.operations;

import java.nio.file.Path;
import java.util.List;

import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSPath;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.yade.commons.Yade.TransferOperation;
import com.sos.yade.engine.arguments.YADEArguments;
import com.sos.yade.engine.arguments.YADEClientArguments;
import com.sos.yade.engine.arguments.YADEProviderCommandArguments;
import com.sos.yade.engine.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.delegators.YADETargetProviderDelegator;
import com.sos.yade.engine.exceptions.YADEEngineOperationException;
import com.sos.yade.engine.handlers.operations.copymove.CopyMoveOperationsHandler;
import com.sos.yade.engine.handlers.operations.getlist.GetListOperationHandler;
import com.sos.yade.engine.handlers.operations.remove.RemoveOperationHandler;
import com.sos.yade.engine.helpers.YADEParallelProcessingConfig;

public class YADEOperationsManager {

    private static String NEW_LINE = "\n"; // System.lineSeparator();

    /** XML Schema allows 4 YADE "standard" operation:<br/>
     * - Copy<br/>
     * - Move<br/>
     * - Remove<br/>
     * - GetList<br/>
     */
    /** TODO not use "YADEArguments args" ... */
    public static void process(ISOSLogger logger, YADEParallelProcessingConfig parallelProcessingConfig, YADEArguments args,
            YADEClientArguments clientArgs, YADESourceProviderDelegator sourceDelegator, List<ProviderFile> sourceFiles,
            YADETargetProviderDelegator targetDelegator) throws YADEEngineOperationException {

        TransferOperation operation = args.getOperation().getValue();
        switch (operation) {
        case COPY:
        case MOVE:
            if (doNotPerformOperationDueToEmptyFiles(operation, logger, sourceFiles)) {
                // TODO check YADE1 on 0 files ...
                createResultSetFileFromSourceFiles(logger, clientArgs, sourceFiles);
                return;
            }

            CopyMoveOperationsHandler.process(operation, logger, parallelProcessingConfig, args, sourceDelegator, targetDelegator, sourceFiles);
            // TODO after operation?
            createResultSetFileFromSourceFiles(null, clientArgs, sourceFiles);
            break;
        case REMOVE: // Remove file on Source
            if (doNotPerformOperationDueToEmptyFiles(operation, logger, sourceFiles)) {
                // TODO check YADE1 on 0 files ...
                createResultSetFileFromSourceFiles(logger, clientArgs, sourceFiles);
                return;
            }
            RemoveOperationHandler.process(operation, logger, sourceDelegator, sourceFiles, getTransactionalIgoredMessage(args)
                    + getCommandsAfterFileIgoredMessage(sourceDelegator.getArgs().getCommands()));
            // TODO after operation?
            createResultSetFileFromSourceFiles(null, clientArgs, sourceFiles);
            break;
        case GETLIST:
            // Get list of files from Source.
            // Special processing without doNotPerformOperationDueToEmptyFiles() due to the assumption of 0 source files
            GetListOperationHandler.process(operation, logger, getTransactionalIgoredMessage(args));
            createResultSetFileFromSourceFiles(logger, clientArgs, sourceFiles);
            break;

        // All others are documented here, but cannot be set when YADEEngine is used
        case COPYFROMINTERNET: // handled by YADEDMZEngine
        case COPYTOINTERNET:
        case RENAME: // ??? - to remove, not supported
        case UNKNOWN: // ???
        default:
            logger.info("[%s]ignored", operation);
            break;
        }

    }

    /** All Operations */
    private static boolean doNotPerformOperationDueToEmptyFiles(TransferOperation operation, ISOSLogger logger, List<ProviderFile> sourceFiles) {
        if (SOSCollection.isEmpty(sourceFiles)) {
            logger.info("[%s]The operation cannot be performed due to empty files.", operation);
            return true;
        }
        return false;
    }

    /** All Operations - TODO see YADEEngine TODOs */
    private static void createResultSetFileFromSourceFiles(ISOSLogger logger, YADEClientArguments args, List<ProviderFile> sourceFiles)
            throws YADEEngineOperationException {
        Path resultSetFile = args.getResultListFile().getValue();
        if (resultSetFile == null) {
            if (logger != null) {
                logger.info("Skip creating the result set file because %s is not specified.", args.getResultListFile().getName());
            }
            return;
        }
        StringBuilder sb = new StringBuilder();
        if (SOSCollection.isEmpty(sourceFiles)) {
            sb.append(NEW_LINE);
        } else {
            sourceFiles.stream().forEach(f -> {
                String entry = f.getFullPath();
                if (logger != null) {
                    logger.info(entry);
                }
                sb.append(entry).append(NEW_LINE);
            });
        }
        try {
            SOSPath.overwrite(resultSetFile, sb.toString());
            // SOSPath.append(resultSetFile, sb.toString());
        } catch (Throwable e) {
            throw new YADEEngineOperationException(e);
        }
    }

    // TODO should be placed in BANNER – also other arguments that are enabled/activated but ignored during a particular operation (e.g. replace, etc.).
    private static String getTransactionalIgoredMessage(YADEArguments args) {
        String msg = "";
        // if (args.getTransactional().isTrue()) {
        // msg = args.getTransactional().getName() + "=true ignored.";
        // }
        return msg;
    }

    // TODO set in BANNER
    private static String getCommandsAfterFileIgoredMessage(YADEProviderCommandArguments args) {
        String msg = "";
        if (args != null && !args.getCommandsAfterFile().isEmpty()) {
            msg = args.getCommandsAfterFile().getName() + " ignored.";
        }
        return msg;
    }

}
