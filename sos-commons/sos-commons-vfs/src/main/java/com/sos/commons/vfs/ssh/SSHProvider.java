package com.sos.commons.vfs.ssh;

import java.nio.file.Path;
import java.util.List;

import com.google.common.base.Joiner;
import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSPathUtils;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.commons.AProvider;
import com.sos.commons.vfs.commons.IProvider;
import com.sos.commons.vfs.commons.ProviderCredentialStoreResolver;
import com.sos.commons.vfs.exceptions.ProviderException;
import com.sos.commons.vfs.exceptions.ProviderInitializationException;
import com.sos.commons.vfs.ssh.commons.SSHProviderArguments;
import com.sos.commons.vfs.ssh.commons.SSHServerInfo;

public abstract class SSHProvider extends AProvider<SSHProviderArguments> {

    /** e.g. "OpenSSH_$version" -> OpenSSH_for_Windows_8.1. Can be null. */
    private SSHServerInfo serverInfo;
    private String serverVersion;

    public static SSHProvider createInstance(ISOSLogger logger, SSHProviderArguments args) throws ProviderInitializationException {
        return new com.sos.commons.vfs.ssh.sshj.SSHJProviderImpl(logger, args);
    }

    protected SSHProvider(ISOSLogger logger, SSHProviderArguments args) throws ProviderInitializationException {
        super(logger, args, args == null ? null : args.getPassphrase());
        setAccessInfo(getArguments() == null ? null : getArguments().getAccessInfo());
    }

    /** SSH Provider specific method */
    public abstract void put(String source, String target, int perm) throws ProviderException;

    /** SSH Provider specific method */
    public abstract void put(String source, String target) throws ProviderException;

    /** SSH Provider specific method */
    public abstract void get(String source, String target) throws ProviderException;

    /** SSH Provider specific method */
    public abstract boolean deleteDirectory(String directory) throws ProviderException;

    /** SSH Provider specific method */
    public abstract boolean deleteWindowsDirectory(String directory) throws ProviderException;

    /** SSH Provider specific method */
    public abstract boolean deleteUnixDirectory(String directory) throws ProviderException;

    /** Overrides {@link AProvider#onCredentialStoreResolved()} */
    @Override
    public void onCredentialStoreResolved() throws Exception {
        ProviderCredentialStoreResolver.resolveAttachment(getArguments(), getArguments().getAuthFile());
    }

    /** Overrides {@link IProvider#getPathSeparator()} */
    @Override
    public String getPathSeparator() {
        return SOSPathUtils.PATH_SEPARATOR_UNIX;
    }

    /** Overrides {@link IProvider#isAbsolutePath(String)} */
    @Override
    public boolean isAbsolutePath(String path) {
        return SOSPathUtils.isAbsoluteFileSystemPath(path);
    }

    /** Overrides {@link IProvider#normalizePath(String)} */
    @Override
    public String normalizePath(String path) {
        if (SOSPathUtils.isAbsoluteWindowsOpenSSHPath(path)) {
            String n = getPathSeparator() + Path.of(path.substring(1)).normalize().toString();
            return toPathStyle(n);
        }
        return SOSPathUtils.toAbsoluteUnixPath(path);
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
}
