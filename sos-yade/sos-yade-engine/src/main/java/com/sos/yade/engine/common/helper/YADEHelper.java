package com.sos.yade.engine.common.helper;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSComparisonOperator;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.IProvider;
import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.yade.engine.common.YADEProviderContext;
import com.sos.yade.engine.common.arguments.YADEArguments;
import com.sos.yade.engine.common.arguments.YADEClientArguments;
import com.sos.yade.engine.exception.SOSYADEEngineConnectionException;
import com.sos.yade.engine.exception.SOSYADEEngineException;
import com.sos.yade.engine.exception.SOSYADEEngineSourceConnectionException;
import com.sos.yade.engine.exception.SOSYADEEngineTargetConnectionException;

public class YADEHelper {

    /** Check on Start
     * 
     * @param args
     * @throws SOSYADEEngineException */
    public static void checkArguments(YADEArguments args) throws SOSYADEEngineException {
        if (args == null) {
            throw new SOSYADEEngineException(new SOSMissingDataException("YADEArguments"));
        }
        if (args.getOperation().getValue() == null) {
            throw new SOSYADEEngineException(new SOSMissingDataException(args.getOperation().getName()));
        }
    }

    /** Check before execute Operation
     * 
     * @param sourceProvider
     * @param args
     * @param sourceFiles
     * @return
     * @throws Exception */
    public static int checkSourceFiles(IProvider sourceProvider, YADEArguments args, List<ProviderFile> sourceFiles) throws Exception {
        int size = sourceFiles == null ? 0 : sourceFiles.size();

        if (size == 0 && args.getSource().getForceFiles().getValue()) {
            throw new Exception(String.format("%s[%s=true]No files found", sourceProvider.getContext().getLogPrefix(), args.getSource()
                    .getForceFiles().getName()));
        }

        // ResultSet
        SOSComparisonOperator op = args.getClient().getRaiseErrorIfResultSetIs().getValue();
        if (op != null) {
            int expectedSize = args.getClient().getExpectedSizeOfResultSet().getValue();
            if (op.compare(size, expectedSize)) {
                throw new Exception(String.format("%s[files found=%s][RaiseErrorIfResultSetIs]%s %s", sourceProvider.getContext().getLogPrefix(),
                        size, op, expectedSize));
            }
        }
        return size;
    }

    public static void throwConnectionException(IProvider provider, Throwable ex) throws SOSYADEEngineConnectionException {
        SOSYADEEngineConnectionException yex = getConnectionException(provider, ex);
        if (yex != null) {
            throw yex;
        }
    }

    public static SOSYADEEngineConnectionException getConnectionException(IProvider provider, Throwable ex) {
        if (provider == null) {
            return null;
        }
        YADEProviderContext c = (YADEProviderContext) provider.getContext();
        if (c == null) {
            return null;
        }
        if (c.isSource()) {
            return new SOSYADEEngineSourceConnectionException(ex);
        }
        return new SOSYADEEngineTargetConnectionException(ex);
    }

    public static boolean isConnectionException(Throwable cause) {
        if (cause == null) {
            return false;
        }
        Throwable e = cause;
        while (e != null) {
            if (e instanceof SOSYADEEngineConnectionException) {
                return true;
            }
            e = e.getCause();
        }
        return false;
    }

    // public static List<String> getSourceSingleFiles(ISOSLogger logger, YADEArguments args, IProvider source, String sourceDir, boolean isPolling) {
    // List<String> entries = new ArrayList<>();
    // if (!args.getFilePath().isEmpty()) {
    // for (String p : args.getFilePath().getValue()) {
    // if (SOSString.isEmpty(p)) {
    // continue;
    // }
    // }
    // }
    //
    // return entries;
    // }

    public static void printBanner(ISOSLogger logger, YADEArguments args) {
        logger.info("[printBanner]...");
    }

    public static void printSummary(ISOSLogger logger, YADEArguments args) {
        logger.info("[printSummary]...");
    }

    public static void setConfiguredSystemProperties(ISOSLogger logger, YADEClientArguments args) {
        if (SOSCollection.isEmpty(args.getSystemPropertyFiles().getValue())) {
            return;
        }
        String method = "setConfiguredSystemProperties";
        logger.info("[%s][files]", method, SOSString.join(args.getSystemPropertyFiles().getValue(), ",", f -> f.toString()));
        Properties p = new Properties();
        for (Path file : args.getSystemPropertyFiles().getValue()) {
            if (Files.exists(file) && Files.isRegularFile(file)) {
                try (BufferedReader reader = Files.newBufferedReader(file)) {
                    p.load(reader);
                    logger.info("[%s][%s]loaded", method, file);
                } catch (Throwable e) {
                    logger.warn("[%s][%s][failed]%s", method, file, e.toString());
                }
            } else {
                logger.warn("[%s][%s]does not exist or is not a regular file", method, file);
            }
        }

        for (String n : p.stringPropertyNames()) {
            String v = p.getProperty(n);
            if (logger.isDebugEnabled()) {
                logger.debug("[%s]%s=%s", method, n, v);
            }
            System.setProperty(n, v);
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
