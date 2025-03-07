package com.sos.yade.engine.commons.helpers;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSPath;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.yade.commons.Yade.TransferOperation;
import com.sos.yade.engine.commons.arguments.YADEClientArguments;
import com.sos.yade.engine.exceptions.YADEEngineException;

public class YADEClientHelper {

    private static String NEW_LINE = "\n"; // System.lineSeparator();

    public static void writeResultSet(ISOSLogger logger, TransferOperation operation, YADEClientArguments clientArgs, List<ProviderFile> sourceFiles)
            throws Exception {
        if (clientArgs.getResultSetFileName().getValue() == null) {
            return;
        }
        Path file = SOSPath.toAbsoluteNormalizedPath(clientArgs.getResultSetFileName().getValue());
        logger.info("[%s]write %s entries to the result set file", file, sourceFiles.size());

        boolean logEntries = TransferOperation.GETLIST.equals(operation);
        StringBuilder sb = new StringBuilder();
        if (SOSCollection.isEmpty(sourceFiles)) {
            sb.append(NEW_LINE);
        } else {
            sourceFiles.stream().forEach(f -> {
                String entry = f.getFullPath();
                if (logEntries) {
                    logger.info(entry);
                }
                sb.append(entry).append(NEW_LINE);
            });
        }
        try {
            SOSPath.overwrite(file, sb.toString());
            // SOSPath.append(file, sb.toString());
        } catch (Throwable e) {
            throw new YADEEngineException("[writeResultSet][" + file + "]" + e, e);
        }
    }

    public static void waitFor(long interval) {
        if (interval <= 0) {
            return;
        }
        try {
            TimeUnit.SECONDS.sleep(interval);
        } catch (InterruptedException e) {
        }
    }

}
