package com.sos.yade.engine.common.helper;

import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.util.common.SOSArgument;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.AProviderArguments;
import com.sos.commons.vfs.common.AProviderArguments.Protocol;
import com.sos.commons.vfs.common.IProvider;
import com.sos.commons.vfs.common.file.ProviderDirectoryPath;
import com.sos.commons.vfs.exception.SOSProviderException;
import com.sos.commons.vfs.local.LocalProvider;
import com.sos.commons.vfs.local.common.LocalProviderArguments;
import com.sos.commons.vfs.ssh.SSHProvider;
import com.sos.commons.vfs.ssh.common.SSHProviderArguments;
import com.sos.yade.engine.common.YADEProviderContext;
import com.sos.yade.engine.common.arguments.YADEArguments;
import com.sos.yade.engine.common.arguments.YADESourceTargetArguments;
import com.sos.yade.engine.common.arguments.YADETargetArguments;
import com.sos.yade.engine.exception.SOSYADEEngineConnectionException;
import com.sos.yade.engine.exception.SOSYADEEngineException;

public class YADEProviderHelper {

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

    // TODO alternate connections ... + see YADEEngineSourcePollingHandler.ensureConnected
    public static void connect(ISOSLogger logger, IProvider provider, YADESourceTargetArguments args) throws SOSYADEEngineConnectionException {
        if (provider == null) {
            return;
        }

        // without retry
        if (!args.isRetryOnConnectionErrorEnabled()) {
            try {
                provider.connect();
            } catch (Throwable e) {
                YADEHelper.throwConnectionException(provider, e);
            }
            return;
        }

        // with retry
        int maxRetries = args.getConnectionErrorRetryCountMax().getValue().intValue();
        long retryInterval = YADEArgumentsHelper.getIntervalInSeconds(args.getConnectionErrorRetryInterval(), 0);
        for (int retryCounter = 0; retryCounter <= maxRetries; retryCounter++) {
            try {
                provider.connect();
                return;
            } catch (Throwable e) {
                if (retryCounter == maxRetries) {
                    YADEHelper.throwConnectionException(provider, e);
                }
                logger.info("%s[retry=%s in %ss]%s", provider.getContext().getLogPrefix(), retryCounter + 1, retryInterval, e.toString(), e);
                YADEHelper.waitFor(retryInterval);
            }
        }
    }

    /** Provider Disconnect does not throw exceptions - but logs with the type (source/destination) when occurred/disconnect executed
     * 
     * @param source
     * @param target */
    public static void disconnect(IProvider... providers) {
        for (IProvider p : providers) {
            if (p != null) {
                p.disconnect();
            }
        }
    }

    public static void createDirectoriesOnTarget(ISOSLogger logger, IProvider targetProvider, YADETargetArguments args,
            ProviderDirectoryPath targetDir) throws SOSYADEEngineException {
        if (targetProvider == null || targetDir == null || !args.getMakeDirs().isTrue()) {
            return;
        }
        try {
            if (targetProvider.createDirectoriesIfNotExist(targetDir.getPath())) {
                logger.info("%s[%s=true][%s]created", targetProvider.getContext().getLogPrefix(), args.getMakeDirs().getName(), targetDir.getPath());
            } else {
                logger.info("%s[%s=true][%s][skip]already exists", targetProvider.getContext().getLogPrefix(), args.getMakeDirs().getName(), targetDir
                        .getPath());
            }
        } catch (SOSProviderException e) {
            throw new SOSYADEEngineException(e);
        }
    }

    private static void setProviderContext(IProvider provider, boolean isSource) {
        if (provider == null) {
            return;
        }
        provider.setContext(new YADEProviderContext(isSource));
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
