package com.sos.yade.engine.commons.delegators;

import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.commons.AProvider;
import com.sos.commons.vfs.commons.AProviderArguments;
import com.sos.commons.vfs.commons.AProviderArguments.Protocol;
import com.sos.commons.vfs.exceptions.ProviderException;
import com.sos.commons.vfs.ftp.FTPProvider;
import com.sos.commons.vfs.ftp.commons.FTPProviderArguments;
import com.sos.commons.vfs.ftp.commons.FTPSProviderArguments;
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

    private static final String UNTRUSTER_SSL = "UntrustedSSL";
    private static final String UNTRUSTER_SSL_VERIFY_CERTIFICATE_HOSTNAME_OPPOSITE_NAME = "DisableCertificateHostnameVerification";

    // TODO alternate connections ... + see YADEEngineSourcePollingHandler.ensureConnected
    public static YADESourceProviderDelegator createSourceDelegator(ISOSLogger logger, YADEArguments args, YADESourceArguments sourceArgs)
            throws YADEEngineInitializationException {
        return new YADESourceProviderDelegator(initializeProvider(logger, args, sourceArgs.getProvider(), sourceArgs.getLabel().getValue(), false),
                sourceArgs);
    }

    // TODO alternate connections ... + see YADEEngineSourcePollingHandler.ensureConnected
    public static YADETargetProviderDelegator createTargetDelegator(ISOSLogger logger, YADEArguments args, YADETargetArguments targetArgs)
            throws YADEEngineInitializationException {
        if (!YADEArgumentsHelper.needTargetProvider(args)) {
            return null;
        }
        return new YADETargetProviderDelegator(initializeProvider(logger, args, targetArgs.getProvider(), targetArgs.getLabel().getValue(), true),
                targetArgs);
    }

    private static AProvider<?> initializeProvider(ISOSLogger logger, YADEArguments args, AProviderArguments providerArgs, String delegatorLabel,
            boolean isTarget) throws YADEEngineInitializationException {

        if (providerArgs == null) {
            throw new YADEEngineInitializationException(new SOSMissingDataException("[" + delegatorLabel + "]YADEProviderArguments"));
        }

        SOSArgument<Protocol> protocol = providerArgs.getProtocol();
        if (protocol.getValue() == null) {
            throw new YADEEngineInitializationException(new SOSMissingDataException(protocol.getName()));
        }
        // <XXX>Provider.createInstance - multiple provider implementations available/possible
        AProvider<?> p = null;
        try {
            switch (protocol.getValue()) {
            case FTP:
                p = new FTPProvider(logger, (FTPProviderArguments) providerArgs);
                args.getParallelism().setValue(1);
                break;
            case FTPS:
                FTPSProviderArguments fa = (FTPSProviderArguments) providerArgs;
                if (fa.getSsl() != null) {
                    fa.getSsl().setUntrustedSslVerifyCertificateHostnameOppositeName(UNTRUSTER_SSL_VERIFY_CERTIFICATE_HOSTNAME_OPPOSITE_NAME);
                    fa.getSsl().setUntrustedSslNameAlias(UNTRUSTER_SSL);
                }
                p = new FTPProvider(logger, fa);
                args.getParallelism().setValue(1);
                break;
            case LOCAL:
                p = new LocalProvider(logger, (LocalProviderArguments) providerArgs);
                break;
            case HTTP:
            case HTTPS:
                HTTPProviderArguments ha = (HTTPProviderArguments) providerArgs;
                if (ha.getSsl() != null) {
                    ha.getSsl().setUntrustedSslVerifyCertificateHostnameOppositeName(UNTRUSTER_SSL_VERIFY_CERTIFICATE_HOSTNAME_OPPOSITE_NAME);
                    ha.getSsl().setUntrustedSslNameAlias(UNTRUSTER_SSL);
                }
                p = new HTTPProvider(logger, ha);
                if (isTarget) {
                    args.getParallelism().setValue(1);
                }
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
                WebDAVProviderArguments wa = (WebDAVProviderArguments) providerArgs;
                if (wa.getSsl() != null) {
                    wa.getSsl().setUntrustedSslVerifyCertificateHostnameOppositeName(UNTRUSTER_SSL_VERIFY_CERTIFICATE_HOSTNAME_OPPOSITE_NAME);
                    wa.getSsl().setUntrustedSslNameAlias(UNTRUSTER_SSL);
                }
                p = new WebDAVProvider(logger, wa);
                if (isTarget) {
                    args.getParallelism().setValue(1);
                }
                break;
            case UNKNOWN:
            default:
                throw new YADEEngineInitializationException(new SOSInvalidDataException(protocol.getName() + "=" + protocol.getValue()));
            }
        } catch (ProviderException e) {
            throw new YADEEngineInitializationException(e);
        }
        return p;
    }
}
