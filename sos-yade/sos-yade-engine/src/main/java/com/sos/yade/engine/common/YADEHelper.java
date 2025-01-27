package com.sos.yade.engine.common;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.SOSArgument;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.AProviderArguments;
import com.sos.commons.vfs.common.AProviderArguments.Protocol;
import com.sos.commons.vfs.common.IProvider;
import com.sos.commons.vfs.exception.SOSProviderException;
import com.sos.commons.vfs.local.LocalProvider;
import com.sos.commons.vfs.local.common.LocalProviderArguments;
import com.sos.commons.vfs.ssh.SSHProvider;
import com.sos.commons.vfs.ssh.common.SSHProviderArguments;
import com.sos.yade.engine.common.arguments.YADEArguments;
import com.sos.yade.engine.common.arguments.YADESourceArguments;
import com.sos.yade.engine.common.arguments.YADETargetArguments;
import com.sos.yade.engine.exception.SOSYADEEngineConnectionException;
import com.sos.yade.engine.exception.SOSYADEEngineException;
import com.sos.yade.engine.exception.SOSYADEEngineSourceConnectionException;
import com.sos.yade.engine.exception.SOSYADEEngineTargetConnectionException;

public class YADEHelper {

    public static void checkArguments(YADEArguments args) throws SOSYADEEngineException {
        if (args == null) {
            throw new SOSYADEEngineException(new SOSMissingDataException("YADEArguments"));
        }
        if (args.getOperation().getValue() == null) {
            throw new SOSYADEEngineException(new SOSMissingDataException(args.getOperation().getName()));
        }
    }

    public static IProvider getProvider(ISOSLogger logger, YADEArguments args, boolean isSource) throws SOSYADEEngineException {
        IProvider provider = null;
        if (isSource) {
            provider = getProvider(logger, args.getSource().getProvider());
        } else {
            if (needTargetProvider(args)) {
                provider = getProvider(logger, args.getTarget().getProvider());
            }
        }
        setProviderContext(provider, isSource);
        return provider;
    }

    private static void setProviderContext(IProvider provider, boolean isSource) {
        if (provider == null) {
            return;
        }
        provider.setContext(new YADEProviderContext(isSource));
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

    public static YADEDirectory getYADEDirectory(IProvider provider, YADESourceArguments args) {
        if (args == null) {
            return null;
        }
        return getYADEDirectory(provider, args.getSourceDir());
    }

    public static YADEDirectory getYADEDirectory(IProvider provider, YADETargetArguments args) {
        if (args == null) {
            return null;
        }
        return getYADEDirectory(provider, args.getTargetDir());
    }

    private static YADEDirectory getYADEDirectory(IProvider provider, SOSArgument<String> arg) {
        if (provider == null || SOSString.isEmpty(arg.getValue())) {
            return null;
        }
        return new YADEDirectory(provider, arg.getValue());
    }

    public static void printBanner(ISOSLogger logger, YADEArguments args) {
        logger.info("[printBanner]...");
    }

    public static void printSummary(ISOSLogger logger, YADEArguments args) {
        logger.info("[printSummary]...");
    }

    public static void setConfiguredSystemProperties(ISOSLogger logger, YADEArguments args) {
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

    public static long getIntervalInSeconds(SOSArgument<String> arg, long defaultValue) {
        try {
            return SOSDate.resolveAge("s", arg.getValue()).longValue();
        } catch (Throwable e) {
            return defaultValue;
        }
    }

    private static IProvider getProvider(ISOSLogger logger, AProviderArguments args) throws SOSYADEEngineException {
        if (args == null) {
            throw new SOSYADEEngineException(new SOSMissingDataException("YADEProviderArguments"));
        }

        SOSArgument<Protocol> protocol = args.getProtocol();
        if (protocol.getValue() == null) {
            throw new SOSYADEEngineException(new SOSMissingDataException(protocol.getName()));
        }
        IProvider p = null;
        try {
            switch (protocol.getValue()) {
            case FTP:
            case FTPS:
                throw new SOSYADEEngineException("[not implemented yet]" + protocol.getName() + "=" + protocol.getValue());
            // break;
            case HTTP:
            case HTTPS:
                throw new SOSYADEEngineException("[not implemented yet]" + protocol.getName() + "=" + protocol.getValue());
            // break;
            case LOCAL:
                p = new LocalProvider(logger, (LocalProviderArguments) args);
                break;
            case SFTP:
            case SSH:
                p = new SSHProvider(logger, (SSHProviderArguments) args);
                break;
            case SMB:
                throw new SOSYADEEngineException("[not implemented yet]" + protocol.getName() + "=" + protocol.getValue());
            // break;
            case WEBDAV:
            case WEBDAVS:
                throw new SOSYADEEngineException("[not implemented yet]" + protocol.getName() + "=" + protocol.getValue());
            // break;
            case UNKNOWN:
            default:
                throw new SOSYADEEngineException(new SOSInvalidDataException(protocol.getName() + "=" + protocol.getValue()));
            }
        } catch (SOSProviderException e) {
            throw new SOSYADEEngineException(e);
        }
        return p;
    }

    private static boolean needTargetProvider(YADEArguments args) throws SOSYADEEngineException {
        switch (args.getOperation().getValue()) {
        case GETLIST:
        case REMOVE:
        case RENAME:
            return false;
        case UNKNOWN:
            throw new SOSYADEEngineException(new SOSInvalidDataException(args.getOperation().getName() + "=" + args.getOperation().getValue()));
        // case COPY:
        // case MOVE:
        // case COPYFROMINTERNET:
        // case COPYTOINTERNET:
        default:
            return true;
        }
    }

}
