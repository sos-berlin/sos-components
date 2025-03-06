package com.sos.commons.vfs.ssh.common;

import java.nio.file.Path;
import java.util.List;

import com.google.common.base.Joiner;
import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSPathUtil;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.AProvider;
import com.sos.commons.vfs.common.CredentialStoreResolver;
import com.sos.commons.vfs.common.IProvider;
import com.sos.commons.vfs.exceptions.SOSProviderException;
import com.sos.commons.vfs.exceptions.SOSProviderInitializationException;

public abstract class ASSHProvider extends AProvider<SSHProviderArguments> {

    /** e.g. "OpenSSH_$version" -> OpenSSH_for_Windows_8.1. Can be null. */
    private SSHServerInfo serverInfo;
    private String serverVersion;

    /** Layer for instantiating a Real Provider: SSHJ or ... */
    public ASSHProvider() throws SOSProviderInitializationException {
        super(null, null);
    }

    /** Real Provider */
    public ASSHProvider(ISOSLogger logger, SSHProviderArguments args) throws SOSProviderInitializationException {
        super(logger, args);
        resolveCredentialStore();
        setAccessInfo(String.format("%s@%s:%s", getArguments().getUser().getDisplayValue(), getArguments().getHost().getDisplayValue(), getArguments()
                .getPort().getDisplayValue()));
    }

    /** SSH Provider specific method */
    public abstract void put(String source, String target, int perm) throws SOSProviderException;

    /** SSH Provider specific method */
    public abstract void put(String source, String target) throws SOSProviderException;

    /** SSH Provider specific method */
    public abstract void get(String source, String target) throws SOSProviderException;

    /** Overrides {@link IProvider#isAbsolutePath(String)} */
    @Override
    public boolean isAbsolutePath(String path) {
        return SOSPathUtil.isAbsoluteFileSystemPath(path);
    }

    /** Overrides {@link IProvider#getPathSeparator()} */
    @Override
    public String getPathSeparator() {
        return SOSPathUtil.PATH_SEPARATOR_UNIX;
    }

    /** Overrides {@link IProvider#normalizePath(String)} */
    @Override
    public String normalizePath(String path) {
        if (SOSPathUtil.isAbsoluteWindowsOpenSSHPath(path)) {
            String n = getPathSeparator() + Path.of(path.substring(1)).normalize().toString();
            return toPathStyle(n);
        }
        return toPathStyle(SOSPath.toAbsoluteNormalizedPath(path).toString());
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

    public String getConnectedMsg(List<String> additionalInfos) {
        String msg = "";
        if (SOSCollection.isEmpty(additionalInfos)) {
            if (serverInfo != null) {
                msg += serverInfo.toString();
            }
        } else {
            if (serverInfo != null) {
                msg += "[" + serverInfo.toString() + "]";
            }
            msg += Joiner.on(", ").join(additionalInfos);
        }
        return getConnectedMsg(msg);
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
