package com.sos.commons.vfs.ssh.common;

import java.util.List;

import com.google.common.base.Joiner;
import com.sos.commons.credentialstore.CredentialStoreArguments;
import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.AProvider;
import com.sos.commons.vfs.common.CredentialStoreResolver;
import com.sos.commons.vfs.exception.SOSProviderException;

public abstract class ASSHProvider extends AProvider<SSHProviderArguments> {

    private final String mainInfo;

    /** e.g. "OpenSSH_$version" -> OpenSSH_for_Windows_8.1. Can be null. */
    private SSHServerInfo serverInfo;
    private String serverVersion;

    public ASSHProvider() {
        super(null, null, null);
        mainInfo = null;
    }

    public ASSHProvider(ISOSLogger logger, SSHProviderArguments args, CredentialStoreArguments csArgs) throws Exception {
        super(logger, args, csArgs);
        if (csArgs != null) {
            if (CredentialStoreResolver.resolve(getArguments(), getArguments().getPassphrase())) {
                CredentialStoreResolver.resolveAttachment(getArguments(), getArguments().getAuthFile());
            }
        }
        mainInfo = String.format("%s:%s", getArguments().getHost().getDisplayValue(), getArguments().getPort().getDisplayValue());
    }

    public abstract void put(String source, String target, int perm) throws SOSProviderException;

    public abstract void put(String source, String target) throws SOSProviderException;

    public abstract void get(String source, String target) throws SOSProviderException;

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
        return String.format("[connect]%s ...", mainInfo);
    }

    public String getConnectedMsg(List<String> additionalInfos) {
        String r = String.format("[connected][%s]", mainInfo);
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
        return String.format("[disconnected]%s", mainInfo);
    }

}
