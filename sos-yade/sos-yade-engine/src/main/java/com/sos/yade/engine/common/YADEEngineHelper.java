package com.sos.yade.engine.common;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.AProviderArguments;
import com.sos.commons.vfs.common.IProvider;
import com.sos.commons.vfs.exception.SOSProviderException;
import com.sos.commons.vfs.local.LocalProvider;
import com.sos.commons.vfs.local.common.LocalProviderArguments;
import com.sos.commons.vfs.ssh.SSHProvider;
import com.sos.commons.vfs.ssh.common.SSHProviderArguments;
import com.sos.yade.engine.exception.SOSYADEEngineException;

public class YADEEngineHelper {

    public static void checkArguments(TransferArguments args) throws SOSYADEEngineException {
        if (args == null) {
            throw new SOSYADEEngineException(new SOSMissingDataException("TransferArguments"));
        }
        if (args.getOperation().getValue() == null) {
            throw new SOSYADEEngineException(new SOSMissingDataException(args.getOperation().getName()));
        }
    }

    public static IProvider getProvider(ISOSLogger logger, TransferArguments args, boolean isSource) throws SOSYADEEngineException {
        IProvider p = null;
        if (isSource) {
            p = getProvider(logger, args.getSource());
        } else {
            if (needTargetProvider(args)) {
                p = getProvider(logger, args.getTarget());
            }
        }
        return p;
    }

    public static List<String> getSourceSingleFiles(ISOSLogger logger, TransferArguments args, IProvider source, String sourceDir,
            boolean isPolling) {
        List<String> entries = new ArrayList<>();
        if (!args.getFilePath().isEmpty()) {
            for (String p : args.getFilePath().getValue()) {
                if (SOSString.isEmpty(p)) {
                    continue;
                }
            }
        }

        return entries;
    }

    public static void printBanner(ISOSLogger logger, TransferArguments args) {
        logger.info("[printBanner]...");
    }

    public static void printSummary(ISOSLogger logger, TransferArguments args) {
        logger.info("[printSummary]...");
    }

    public static void setConfiguredSystemProperties(ISOSLogger logger, TransferArguments args) {
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

    private static IProvider getProvider(ISOSLogger logger, AProviderArguments args) throws SOSYADEEngineException {
        if (args == null) {
            throw new SOSYADEEngineException(new SOSMissingDataException("AProviderArguments"));
        }
        if (args.getProtocol().getValue() == null) {
            throw new SOSYADEEngineException(new SOSMissingDataException(args.getProtocol().getName()));
        }
        IProvider p = null;
        try {
            switch (args.getProtocol().getValue()) {
            case FTP:
            case FTPS:
                throw new SOSYADEEngineException("[not implemented yet]" + args.getProtocol().getName() + "=" + args.getProtocol().getValue());
            // break;
            case HTTP:
            case HTTPS:
                throw new SOSYADEEngineException("[not implemented yet]" + args.getProtocol().getName() + "=" + args.getProtocol().getValue());
            // break;
            case LOCAL:
                p = new LocalProvider(logger, (LocalProviderArguments) args);
                break;
            case SFTP:
            case SSH:
                p = new SSHProvider(logger, (SSHProviderArguments) args);
                break;
            case SMB:
                throw new SOSYADEEngineException("[not implemented yet]" + args.getProtocol().getName() + "=" + args.getProtocol().getValue());
            // break;
            case WEBDAV:
            case WEBDAVS:
                throw new SOSYADEEngineException("[not implemented yet]" + args.getProtocol().getName() + "=" + args.getProtocol().getValue());
            // break;
            case UNKNOWN:
            default:
                throw new SOSYADEEngineException(new SOSInvalidDataException(args.getProtocol().getName() + "=" + args.getProtocol().getValue()));
            }
        } catch (SOSProviderException e) {
            throw new SOSYADEEngineException(e);
        }
        return p;
    }

    private static boolean needTargetProvider(TransferArguments args) throws SOSYADEEngineException {
        switch (args.getOperation().getValue()) {
        case GETLIST:
        case REMOVE:
        case RENAME:
            return false;
        case UNKNOWN:
            throw new SOSYADEEngineException(new SOSInvalidDataException(args.getOperation().getName() + "=" + args.getOperation().getValue()));
        default:
            return true;
        }
    }

}
