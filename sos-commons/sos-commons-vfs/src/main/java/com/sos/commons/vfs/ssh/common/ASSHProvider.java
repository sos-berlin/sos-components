package com.sos.commons.vfs.ssh.common;

import java.util.List;

import com.google.common.base.Joiner;
import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSPathUtil;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.AProvider;
import com.sos.commons.vfs.common.CredentialStoreResolver;
import com.sos.commons.vfs.common.file.ProviderDirectoryPath;
import com.sos.commons.vfs.exception.SOSProviderException;
import com.sos.commons.vfs.exception.SOSProviderInitializationException;

public abstract class ASSHProvider extends AProvider<SSHProviderArguments> {

    private final String mainInfo;

    /** e.g. "OpenSSH_$version" -> OpenSSH_for_Windows_8.1. Can be null. */
    private SSHServerInfo serverInfo;
    private String serverVersion;

    /** Layer for instantiating a Real Provider: SSHJ or ... */
    public ASSHProvider() throws SOSProviderInitializationException {
        super(null, null);
        mainInfo = null;
    }

    /** Real Provider */
    public ASSHProvider(ISOSLogger logger, SSHProviderArguments args) throws SOSProviderInitializationException {
        super(logger, args);
        resolveCredentialStore();
        mainInfo = String.format("%s:%s", getArguments().getHost().getDisplayValue(), getArguments().getPort().getDisplayValue());
    }

    public abstract void put(String source, String target, int perm) throws SOSProviderException;

    public abstract void put(String source, String target) throws SOSProviderException;

    public abstract void get(String source, String target) throws SOSProviderException;

    @Override
    public boolean isAbsolutePath(String path) {
        return SOSPathUtil.isAbsoluteFileSystemPath(path);
    }

    @Override
    public ProviderDirectoryPath getDirectoryPath(String path) {
        if (SOSString.isEmpty(path)) {
            return null;
        }
        return new ProviderDirectoryPath(SOSPathUtil.getUnixStyleDirectoryWithoutTrailingSeparator(path), SOSPathUtil
                .getUnixStyleDirectoryWithTrailingSeparator(path));
    }

    public SSHServerInfo getServerInfo() {
        if (serverInfo == null) {
            serverInfo = new SSHServerInfo(serverVersion, executeCommand("uname"));
        }
        return serverInfo;
    }

    public void setServerVersion(String val) {
        serverVersion = val;
    }

    public String getMainInfo() {
        return mainInfo;
    }

    public String getConnectMsg() {
        return String.format("%s[connect]%s ...", getLogPrefix(), mainInfo);
    }

    public String getConnectedMsg(List<String> additionalInfos) {
        String r = String.format("%s[connected][%s]", getLogPrefix(), mainInfo);
        if (SOSCollection.isEmpty(additionalInfos)) {
            if (serverInfo != null) {
                r += serverInfo.toString();
            }
        } else {
            if (serverInfo != null) {
                r += "[" + serverInfo.toString() + "]";
            }
            r += Joiner.on(", ").join(additionalInfos);
        }
        return r;
    }

    public String getDisconnectedMsg() {
        return String.format("%s[disconnected]%s", getLogPrefix(), mainInfo);
    }

    private void resolveCredentialStore() throws SOSProviderInitializationException {
        try {
            if (CredentialStoreResolver.resolve(getArguments(), getArguments().getPassphrase())) {
                CredentialStoreResolver.resolveAttachment(getArguments(), getArguments().getAuthFile());
            }
        } catch (Throwable e) {
            throw new SOSProviderInitializationException(e);
        }
    }

}
