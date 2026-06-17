package com.sos.yade.engine.commons.helpers;

import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSPath;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.commons.AProvider;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.yade.commons.Yade.TransferOperation;
import com.sos.yade.engine.commons.arguments.YADEClientArguments;
import com.sos.yade.engine.exceptions.YADEEngineException;

public class YADEClientHelper {

    private static String NEW_LINE = "\n"; // System.lineSeparator();

    public static void setSystemPropertiesFromFiles(ISOSLogger logger, YADEClientArguments args) {
        Properties p = AProvider.loadSystemProperties(logger, args.getSystemPropertyFiles());
        if (p == null || p.isEmpty()) {
            return;
        }

        String method = "set system properties";
        for (String n : p.stringPropertyNames()) {
            String v = p.getProperty(n);
            if (logger.isDebugEnabled()) {
                logger.debug("[%s]%s=%s", method, n, v);
            }
            System.setProperty(n, v);
        }
    }

    public static void writeResultSet(ISOSLogger logger, TransferOperation operation, YADEClientArguments clientArgs, List<ProviderFile> sourceFiles)
            throws Exception {
        if (clientArgs.getResultSetFile().getValue() == null) {
            return;
        }
        Path file = SOSPath.toAbsoluteNormalizedPath(clientArgs.getResultSetFile().getValue());
        boolean logEntries = TransferOperation.GETLIST.equals(operation);
        StringBuilder sb = new StringBuilder();
        if (SOSCollection.isEmpty(sourceFiles)) {
            if (logEntries) {
                logger.info("[%s][%s=%s]write a new line (because of 0 entries)", YADEClientArguments.LABEL, clientArgs.getResultSetFile().getName(),
                        file);
            }
            sb.append(NEW_LINE);
        } else {
            if (logEntries) {
                logger.info("[%s][%s=%s]write %s entries:", YADEClientArguments.LABEL, clientArgs.getResultSetFile().getName(), file, sourceFiles
                        .size());
            }
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
        } catch (Exception e) {
            throw new YADEEngineException("[writeResultSet][" + file + "]" + e, e);
        }
        if (!logEntries) {
            logger.info("[%s][%s=%s]%s entries written", YADEClientArguments.LABEL, clientArgs.getResultSetFile().getName(), file, sourceFiles
                    .size());
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
