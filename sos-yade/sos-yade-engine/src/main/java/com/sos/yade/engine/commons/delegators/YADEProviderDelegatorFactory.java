package com.sos.yade.engine.commons.delegators;

import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.commons.AProvider;
import com.sos.commons.vfs.commons.AProviderArguments;
import com.sos.commons.vfs.commons.AProviderArguments.Protocol;
import com.sos.commons.vfs.commons.IProvider;
import com.sos.commons.vfs.exceptions.ProviderException;
import com.sos.commons.vfs.ftp.FTPProvider;
import com.sos.commons.vfs.ftp.commons.FTPProviderArguments;
import com.sos.commons.vfs.http.HTTPProvider;
import com.sos.commons.vfs.http.commons.HTTPProviderArguments;
import com.sos.commons.vfs.local.LocalProvider;
import com.sos.commons.vfs.local.commons.LocalProviderArguments;
import com.sos.commons.vfs.smb.SMBProvider;
import com.sos.commons.vfs.smb.commons.SMBProviderArguments;
import com.sos.commons.vfs.ssh.SSHProvider;
import com.sos.commons.vfs.ssh.commons.SSHProviderArguments;
import com.sos.commons.vfs.webdav.WebDAVProvider;
import com.sos.commons.vfs.webdav.commons.WebDAVProviderArguments;
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
        // <XXX>Provider.createInstance - multiple provider implementations available/possible
        IProvider p = null;
        try {
            switch (protocol.getValue()) {
            case FTP:
            case FTPS:
                p = new FTPProvider(logger, (FTPProviderArguments) providerArgs);
                args.getParallelism().setValue(1);
                break;
            case LOCAL:
                p = new LocalProvider(logger, (LocalProviderArguments) providerArgs);
                break;
            case HTTP:
            case HTTPS:
                p = HTTPProvider.createInstance(logger, (HTTPProviderArguments) providerArgs);
                args.getParallelism().setValue(1);
                break;
            case SFTP:
            case SSH:
                p = SSHProvider.createInstance(logger, (SSHProviderArguments) providerArgs);
                break;
            case SMB:
                p = SMBProvider.createInstance(logger, (SMBProviderArguments) providerArgs);
                break;
            case WEBDAV:
            case WEBDAVS:
                p = WebDAVProvider.createInstance(logger, (WebDAVProviderArguments) providerArgs);
                break;
            case UNKNOWN:
            default:
                throw new YADEEngineInitializationException(new SOSInvalidDataException(protocol.getName() + "=" + protocol.getValue()));
            }
        } catch (ProviderException e) {
            throw new YADEEngineInitializationException(e);
        }
        ((AProvider<?>) p).setSystemPropertiesFromFiles();
        return p;
    }
}
