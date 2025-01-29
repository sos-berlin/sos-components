package com.sos.yade.engine.common.handler;

import java.nio.file.Path;
import java.util.List;

import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSPath;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.IProvider;
import com.sos.commons.vfs.common.file.ProviderDirectoryPath;
import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.commons.vfs.exception.SOSProviderException;
import com.sos.yade.engine.common.YADEProviderFile;
import com.sos.yade.engine.common.arguments.YADEArguments;
import com.sos.yade.engine.common.arguments.YADEClientArguments;
import com.sos.yade.engine.common.arguments.YADESourceArguments;
import com.sos.yade.engine.exception.SOSYADEEngineOperationException;

public class YADEOperationHandler {

    private static String NEW_LINE = "\n"; // System.lineSeparator();

    public static void execute(ISOSLogger logger, YADEArguments args, IProvider sourceProvider, ProviderDirectoryPath sourceDir,
            List<ProviderFile> sourceFiles, IProvider targetProvider, ProviderDirectoryPath targetDir) throws SOSYADEEngineOperationException {

        switch (args.getOperation().getValue()) {
        case COPY:
            break;
        case GETLIST:
            executeGetListOperation(logger, args.getClient(), sourceFiles);
            break;
        case MOVE:
            break;
        case REMOVE:
            break;
        case RENAME:
            break;
        case UNKNOWN:
            break;
        case COPYFROMINTERNET: // handled by YADEDMZ
        case COPYTOINTERNET:
        default:
            break;
        }
    }

    private static void executeGetListOperation(ISOSLogger logger, YADEClientArguments args, List<ProviderFile> sourceFiles)
            throws SOSYADEEngineOperationException {
        logger.info("Operation GETLIST is specified. No transfer will be done.");
        Path resultSetFile = args.getResultListFile().getValue();
        if (resultSetFile == null) {
            logger.info("Skip creating the result set file because %s is not specified.", args.getResultListFile().getName());
            return;
        }
        createResultSetFile(logger, args, sourceFiles, resultSetFile, true);
    }

    // TODO see YADEEngine TODOs
    private static void createResultSetFile(ISOSLogger logger, YADEClientArguments args, List<ProviderFile> files, Path resultSetFile,
            boolean logEntries) throws SOSYADEEngineOperationException {
        if (resultSetFile == null) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        if (SOSCollection.isEmpty(files)) {

            sb.append(NEW_LINE);
        } else {
            files.stream().forEach(f -> {
                String entry = f.getFullPath();
                if (logEntries) {
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

    private static void deleteSourceFiles(ISOSLogger logger, IProvider sourceProvider, YADESourceArguments args, List<ProviderFile> files)
            throws SOSYADEEngineOperationException {
        if (!args.getRemoveFiles().isTrue()) {
            return;
        }

        final String lp = sourceProvider.getContext().getLogPrefix();
        files.stream().forEach(file -> {
            YADEProviderFile f = (YADEProviderFile) file;
            if (f.transferred()) {
                try {
                    sourceProvider.delete(f.getFullPath());
                    logger.info("%s[nr=TODO][%s]deleted", lp, f.getFullPath());
                } catch (SOSProviderException e) {
                    // TODO throw IllegalStateException???
                    // set new state???
                    logger.error("%s[nr=TODO][%s]%s", lp, f.getFullPath(), e.toString());
                }
            }
        });

    }
}
