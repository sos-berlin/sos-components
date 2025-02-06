package com.sos.yade.engine.helpers;

import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.exception.SOSMissingDataException;
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
import com.sos.yade.engine.arguments.YADEArguments;
import com.sos.yade.engine.arguments.YADESourceArguments;
import com.sos.yade.engine.arguments.YADESourceTargetArguments;
import com.sos.yade.engine.arguments.YADETargetArguments;
import com.sos.yade.engine.delegators.IYADEProviderDelegator;
import com.sos.yade.engine.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.delegators.YADETargetProviderDelegator;
import com.sos.yade.engine.exceptions.SOSYADEEngineConnectionException;
import com.sos.yade.engine.exceptions.SOSYADEEngineException;

public class YADEDelegatorHelper {

    // TODO alternate connections ... + see YADEEngineSourcePollingHandler.ensureConnected
    public static YADESourceProviderDelegator getSourceDelegator(ISOSLogger logger, YADESourceArguments sourceArgs) throws SOSYADEEngineException {
        return new YADESourceProviderDelegator(getProvider(logger, sourceArgs.getProvider()), sourceArgs);
    }

    // TODO alternate connections ... + see YADEEngineSourcePollingHandler.ensureConnected
    public static YADETargetProviderDelegator getTargetDelegator(ISOSLogger logger, YADEArguments args, YADETargetArguments targetArgs)
            throws SOSYADEEngineException {
        if (!needTargetProvider(args)) {
            return null;
        }
        return new YADETargetProviderDelegator(getProvider(logger, targetArgs.getProvider()), targetArgs);
    }

    public static void connect(ISOSLogger logger, IYADEProviderDelegator delegator) throws SOSYADEEngineConnectionException {
        if (delegator == null) {
            return;
        }

        YADESourceTargetArguments args = delegator.getArgs();
        // without retry
        if (!args.isRetryOnConnectionErrorEnabled()) {
            try {
                delegator.getProvider().connect();
            } catch (Throwable e) {
                YADEHelper.throwConnectionException(delegator, e);
            }
            return;
        }

        // with retry
        int maxRetries = args.getConnectionErrorRetryCountMax().getValue().intValue();
        long retryInterval = YADEArgumentsHelper.getIntervalInSeconds(args.getConnectionErrorRetryInterval(), 0);
        for (int retryCounter = 0; retryCounter <= maxRetries; retryCounter++) {
            try {
                delegator.getProvider().connect();
                return;
            } catch (Throwable e) {
                if (retryCounter == maxRetries) {
                    YADEHelper.throwConnectionException(delegator, e);
                }
                logger.info("%s[retry=%s in %ss]%s", delegator.getLogPrefix(), retryCounter + 1, retryInterval, e.toString(), e);
                YADEHelper.waitFor(retryInterval);
            }
        }
    }

    /** Provider Disconnect does not throw exceptions - but logs with the type (source/destination) when occurred/disconnect executed
     * 
     * @param source
     * @param target */
    public static void disconnect(IYADEProviderDelegator... delegators) {
        for (IYADEProviderDelegator d : delegators) {
            if (d != null && d.getProvider() != null) {
                d.getProvider().disconnect();
            }
        }
    }

    public static void createDirectoriesOnTarget(ISOSLogger logger, YADETargetProviderDelegator targetDelegator) throws SOSYADEEngineException {
        if (targetDelegator == null || targetDelegator.getDirectory() == null || targetDelegator.getArgs() == null) {
            return;
        }
        YADETargetArguments args = (YADETargetArguments) targetDelegator.getArgs();
        if (!args.getCreateDirectories().isTrue()) {
            return;
        }
        try {
            IProvider provider = targetDelegator.getProvider();
            if (provider.createDirectoriesIfNotExist(targetDelegator.getDirectory().getPath())) {
                logger.info("%s[%s=true][%s]created", targetDelegator.getLogPrefix(), args.getCreateDirectories().getName(), targetDelegator
                        .getDirectory().getPath());
            } else {
                logger.info("%s[%s=true][%s][skip]already exists", targetDelegator.getLogPrefix(), args.getCreateDirectories().getName(),
                        targetDelegator.getDirectory().getPath());
            }
        } catch (SOSProviderException e) {
            throw new SOSYADEEngineException(e);
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
