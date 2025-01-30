package com.sos.yade.engine.handlers.operations;

import java.nio.file.Path;
import java.util.List;

import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSPath;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.commons.vfs.exception.SOSProviderException;
import com.sos.yade.commons.Yade.TransferOperation;
import com.sos.yade.engine.arguments.YADEArguments;
import com.sos.yade.engine.arguments.YADEClientArguments;
import com.sos.yade.engine.arguments.YADEProviderCommandArguments;
import com.sos.yade.engine.delegators.YADEProviderFile;
import com.sos.yade.engine.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.delegators.YADETargetProviderDelegator;
import com.sos.yade.engine.exceptions.SOSYADEEngineOperationException;

public class YADECommonOperationHandler {

    private static String NEW_LINE = "\n"; // System.lineSeparator();

    /** XML Schema allows 4 YADE "standard" operation:<br/>
     * - Copy<br/>
     * - Move<br/>
     * - Remove<br/>
     * - GetList<br/>
     * 
     * @param logger
     * @param args
     * @param sourceProvider
     * @param sourceDir
     * @param sourceFiles
     * @param targetProvider
     * @param targetDir
     * @throws SOSYADEEngineOperationException */
    public static void execute(ISOSLogger logger, YADEArguments args, YADESourceProviderDelegator sourceDelegator, List<ProviderFile> sourceFiles,
            YADETargetProviderDelegator targetDelegator) throws SOSYADEEngineOperationException {

        TransferOperation operation = args.getOperation().getValue();
        switch (operation) {
        case COPY:
        case MOVE:
            if (doNotPerformOperationDueToEmptyFiles(operation, logger, sourceFiles)) {
                // TODO check YADE1 on 0 files ...
                createResultSetFileFromSourceFiles(logger, args.getClient(), sourceFiles);
                return;
            }

            YADECopyOrMoveOperationHandler.execute(operation, logger, args, sourceDelegator, targetDelegator, sourceFiles);
            // TODO after operation?
            createResultSetFileFromSourceFiles(null, args.getClient(), sourceFiles);
            break;
        case REMOVE:
            if (doNotPerformOperationDueToEmptyFiles(operation, logger, sourceFiles)) {
                // TODO check YADE1 on 0 files ...
                createResultSetFileFromSourceFiles(logger, args.getClient(), sourceFiles);
                return;
            }
            YADESourceOperationRemoveHandler.execute(operation, logger, sourceDelegator, sourceFiles, getTransactionalIgoredMessage(args)
                    + getCommandAfterFileIgoredMessage(args.getSource().getCommands()));
            // TODO after operation?
            createResultSetFileFromSourceFiles(null, args.getClient(), sourceFiles);
            break;
        case GETLIST: // Special processing without doNotPerformOperationDueToEmptyFiles() due to the assumption of 0 source files
            YADESourceOperationGetListHandler.execute(operation, logger, getTransactionalIgoredMessage(args));
            createResultSetFileFromSourceFiles(logger, args.getClient(), sourceFiles);
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

    private static boolean doNotPerformOperationDueToEmptyFiles(TransferOperation operation, ISOSLogger logger, List<ProviderFile> sourceFiles) {
        if (SOSCollection.isEmpty(sourceFiles)) {
            logger.info("[%s]The operation cannot be performed due to empty files.", operation);
            return true;
        }
        return false;
    }

    // TODO should be placed in BANNER – also other arguments that are enabled/activated but ignored during a particular operation (e.g. replace, etc.).
    private static String getTransactionalIgoredMessage(YADEArguments args) {
        String msg = "";
        if (args.getTransactional().isTrue()) {
            msg = args.getTransactional().getName() + "=true ignored.";
        }
        return msg;
    }

    // TODO set in BANNER
    private static String getCommandAfterFileIgoredMessage(YADEProviderCommandArguments args) {
        String msg = "";
        if (args != null && !args.getCommandAfterFile().isEmpty()) {
            msg = args.getCommandAfterFile().getName() + " ignored.";
        }
        return msg;
    }

    /** TODO see YADEEngine TODOs
     * 
     * @param logger log if GetList operation, otherwise the logger should be null
     * @param args
     * @param sourceFiles
     * @throws SOSYADEEngineOperationException */
    private static void createResultSetFileFromSourceFiles(ISOSLogger logger, YADEClientArguments args, List<ProviderFile> sourceFiles)
            throws SOSYADEEngineOperationException {
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
            throw new SOSYADEEngineOperationException(e);
        }
    }

    private static void deleteSourceFiles(ISOSLogger logger, YADESourceProviderDelegator sourceDelegator, List<ProviderFile> files)
            throws SOSYADEEngineOperationException {
        if (!sourceDelegator.getArgs().getRemoveFiles().isTrue()) {
            return;
        }

        files.stream().forEach(file -> {
            YADEProviderFile f = (YADEProviderFile) file;
            if (f.transferred()) {
                try {
                    sourceDelegator.getProvider().delete(f.getFullPath());
                    logger.info("%s[nr=TODO][%s]deleted", sourceDelegator.getLogPrefix(), f.getFullPath());
                } catch (SOSProviderException e) {
                    // TODO throw IllegalStateException???
                    // set new state???
                    logger.error("%s[nr=TODO][%s]%s", sourceDelegator.getLogPrefix(), f.getFullPath(), e.toString());
                }
            }
        });

    }
}
