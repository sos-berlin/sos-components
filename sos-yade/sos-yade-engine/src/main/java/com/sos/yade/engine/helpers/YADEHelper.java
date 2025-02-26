package com.sos.yade.engine.helpers;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.yade.engine.arguments.YADEClientArguments;
import com.sos.yade.engine.delegators.IYADEProviderDelegator;
import com.sos.yade.engine.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.exceptions.YADEEngineConnectionException;
import com.sos.yade.engine.exceptions.YADEEngineSourceConnectionException;
import com.sos.yade.engine.exceptions.YADEEngineTargetConnectionException;

public class YADEHelper {


    public static void throwConnectionException(IYADEProviderDelegator delegator, Throwable e) throws YADEEngineConnectionException {
        YADEEngineConnectionException ex = getConnectionException(delegator, e);
        if (ex != null) {
            throw ex;
        }
    }

    public static YADEEngineConnectionException getConnectionException(IYADEProviderDelegator delegator, Throwable ex) {
        if (delegator == null) {
            return null;
        }
        if (delegator instanceof YADESourceProviderDelegator) {
            return new YADEEngineSourceConnectionException(ex.getCause());
        }
        return new YADEEngineTargetConnectionException(ex.getCause());
    }

    public static boolean isConnectionException(Throwable cause) {
        if (cause == null) {
            return false;
        }
        Throwable e = cause;
        while (e != null) {
            if (e instanceof YADEEngineConnectionException) {
                return true;
            }
            e = e.getCause();
        }
        return false;
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
