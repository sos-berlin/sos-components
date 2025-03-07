package com.sos.yade.engine.commons.delegators;

import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.util.common.SOSArgument;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.commons.AProvider;
import com.sos.commons.vfs.commons.AProviderArguments;
import com.sos.commons.vfs.commons.IProvider;
import com.sos.commons.vfs.commons.AProviderArguments.Protocol;
import com.sos.commons.vfs.exceptions.SOSProviderException;
import com.sos.commons.vfs.ftp.FTPProvider;
import com.sos.commons.vfs.ftp.commons.FTPProviderArguments;
import com.sos.commons.vfs.http.HTTPProvider;
import com.sos.commons.vfs.http.commons.HTTPProviderArguments;
import com.sos.commons.vfs.local.LocalProvider;
import com.sos.commons.vfs.local.commons.LocalProviderArguments;
import com.sos.commons.vfs.ssh.SSHProvider;
import com.sos.commons.vfs.ssh.commons.SSHProviderArguments;
import com.sos.yade.engine.commons.arguments.YADEArguments;
import com.sos.yade.engine.commons.arguments.YADESourceArguments;
import com.sos.yade.engine.commons.arguments.YADETargetArguments;
import com.sos.yade.engine.commons.helpers.YADEArgumentsHelper;
import com.sos.yade.engine.exceptions.YADEEngineInitializationException;

public class YADEProviderDelegatorFactory {

    // TODO alternate connections ... + see YADEEngineSourcePollingHandler.ensureConnected
    public static YADESourceProviderDelegator createSourceDelegator(ISOSLogger logger, YADEArguments args, YADESourceArguments sourceArgs)
            throws YADEEngineInitializationException {
        return new YADESourceProviderDelegator(initializeProvider(logger, "Source", args, sourceArgs.getProvider()), sourceArgs);
    }

    // TODO alternate connections ... + see YADEEngineSourcePollingHandler.ensureConnected
    public static YADETargetProviderDelegator createTargetDelegator(ISOSLogger logger, YADEArguments args, YADETargetArguments targetArgs)
            throws YADEEngineInitializationException {
        if (!YADEArgumentsHelper.needTargetProvider(args)) {
            return null;
        }
        return new YADETargetProviderDelegator(initializeProvider(logger, "Target", args, targetArgs.getProvider()), targetArgs);
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
                p = new HTTPProvider(logger, (HTTPProviderArguments) providerArgs);
                break;
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
}
