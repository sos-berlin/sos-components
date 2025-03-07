package com.sos.yade.engine.commons.helpers;

import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.util.common.SOSArgument;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.commons.AProvider;
import com.sos.commons.vfs.commons.AProviderArguments;
import com.sos.commons.vfs.commons.AProviderArguments.Protocol;
import com.sos.commons.vfs.commons.IProvider;
import com.sos.commons.vfs.exceptions.SOSProviderException;
import com.sos.commons.vfs.ftp.FTPProvider;
import com.sos.commons.vfs.ftp.commons.FTPProviderArguments;
import com.sos.commons.vfs.local.LocalProvider;
import com.sos.commons.vfs.local.commons.LocalProviderArguments;
import com.sos.commons.vfs.ssh.SSHProvider;
import com.sos.commons.vfs.ssh.commons.SSHProviderArguments;
import com.sos.yade.engine.commons.arguments.YADEArguments;
import com.sos.yade.engine.commons.arguments.YADESourceArguments;
import com.sos.yade.engine.commons.arguments.YADESourceTargetArguments;
import com.sos.yade.engine.commons.arguments.YADETargetArguments;
import com.sos.yade.engine.commons.delegators.IYADEProviderDelegator;
import com.sos.yade.engine.commons.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.commons.delegators.YADETargetProviderDelegator;
import com.sos.yade.engine.exceptions.YADEEngineConnectionException;
import com.sos.yade.engine.exceptions.YADEEngineInitializationException;
import com.sos.yade.engine.exceptions.YADEEngineSourceConnectionException;
import com.sos.yade.engine.exceptions.YADEEngineTargetConnectionException;

public class YADEProviderDelegatorHelper {

    // TODO alternate connections ... + see YADEEngineSourcePollingHandler.ensureConnected
    public static YADESourceProviderDelegator initializeSourceDelegator(ISOSLogger logger, YADEArguments args, YADESourceArguments sourceArgs)
            throws YADEEngineInitializationException {
        return new YADESourceProviderDelegator(initializeProvider(logger, "Source", args, sourceArgs.getProvider()), sourceArgs);
    }

    // TODO alternate connections ... + see YADEEngineSourcePollingHandler.ensureConnected
    public static YADETargetProviderDelegator initializeTargetDelegator(ISOSLogger logger, YADEArguments args, YADETargetArguments targetArgs)
            throws YADEEngineInitializationException {
        if (!YADEArgumentsHelper.needTargetProvider(args)) {
            return null;
        }
        return new YADETargetProviderDelegator(initializeProvider(logger, "Target", args, targetArgs.getProvider()), targetArgs);
    }

    public static void ensureConnected(ISOSLogger logger, IYADEProviderDelegator delegator) throws YADEEngineConnectionException {
        if (delegator == null) {
            return;
        }

        YADESourceTargetArguments args = delegator.getArgs();
        // without retry
        if (!args.isRetryOnConnectionErrorEnabled()) {
            try {
                delegator.getProvider().ensureConnected();
            } catch (Throwable e) {
                throwConnectionException(delegator, e);
            }
            return;
        }

        // with retry
        int maxRetries = args.getConnectionErrorRetryCountMax().getValue().intValue();
        long retryInterval = YADEArgumentsHelper.getIntervalInSeconds(args.getConnectionErrorRetryInterval(), 0);
        for (int retryCounter = 0; retryCounter <= maxRetries; retryCounter++) {
            try {
                delegator.getProvider().ensureConnected();
                return;
            } catch (Throwable e) {
                if (retryCounter == maxRetries) {
                    throwConnectionException(delegator, e);
                }
                logger.info("%s[retry=%s in %ss]%s", delegator.getLogPrefix(), retryCounter + 1, retryInterval, e.toString(), e);
                YADEClientHelper.waitFor(retryInterval);
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

    private static IProvider initializeProvider(ISOSLogger logger, String identifier, YADEArguments args, AProviderArguments providerArgs)
            throws YADEEngineInitializationException {
        if (providerArgs == null) {
            throw new YADEEngineInitializationException(new SOSMissingDataException("[" + identifier + "]YADEProviderArguments"));
        }

        SOSArgument<Protocol> protocol = providerArgs.getProtocol();
        if (protocol.getValue() == null) {
            throw new YADEEngineInitializationException(new SOSMissingDataException(protocol.getName()));
        }
        IProvider p = null;
        try {
            switch (protocol.getValue()) {
            case FTP:
            case FTPS:
                p = new FTPProvider(logger, (FTPProviderArguments) providerArgs);
                args.getParallelism().setValue(1);
                break;
            case HTTP:
            case HTTPS:
                throw new YADEEngineInitializationException("[not implemented yet]" + protocol.getName() + "=" + protocol.getValue());
            // break;
            case LOCAL:
                p = new LocalProvider(logger, (LocalProviderArguments) providerArgs);
                break;
            case SFTP:
            case SSH:
                p = new SSHProvider(logger, (SSHProviderArguments) providerArgs);
                break;
            case SMB:
                throw new YADEEngineInitializationException("[not implemented yet]" + protocol.getName() + "=" + protocol.getValue());
            // break;
            case WEBDAV:
            case WEBDAVS:
                throw new YADEEngineInitializationException("[not implemented yet]" + protocol.getName() + "=" + protocol.getValue());
            // break;
            case UNKNOWN:
            default:
                throw new YADEEngineInitializationException(new SOSInvalidDataException(protocol.getName() + "=" + protocol.getValue()));
            }
        } catch (SOSProviderException e) {
            throw new YADEEngineInitializationException(e);
        }
        ((AProvider<?>) p).setSystemProperties();
        return p;
    }

    private static void throwConnectionException(IYADEProviderDelegator delegator, Throwable e) throws YADEEngineConnectionException {
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

}
