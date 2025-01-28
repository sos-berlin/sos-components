package com.sos.commons.vfs.ssh;

import java.util.List;

import com.sos.commons.util.common.SOSCommandResult;
import com.sos.commons.util.common.SOSEnv;
import com.sos.commons.util.common.SOSTimeout;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.commons.vfs.common.file.selection.ProviderFileSelection;
import com.sos.commons.vfs.exception.SOSProviderConnectException;
import com.sos.commons.vfs.exception.SOSProviderException;
import com.sos.commons.vfs.ssh.common.ASSHProvider;
import com.sos.commons.vfs.ssh.common.SSHProviderArguments;
import com.sos.commons.vfs.ssh.common.SSHServerInfo;
import com.sos.commons.vfs.ssh.sshj.SSHJProvider;

public class SSHProvider extends ASSHProvider {

    private final ASSHProvider provider;

    public SSHProvider(ISOSLogger logger, SSHProviderArguments args) throws SOSProviderException {
        provider = getProviderInstance(logger, args);
    }

    private static ASSHProvider getProviderInstance(ISOSLogger logger, SSHProviderArguments args) throws SOSProviderException {
        // switch (args.getSSHProviderType().getValue()) {
        // case JSCH:
        // new JschProvider(logger, args);
        // default:
        // new SSHJProvider(logger, args);
        // }
        return new SSHJProvider(logger, args);
    }

    @Override
    public void connect() throws SOSProviderConnectException {
        provider.connect();
    }

    @Override
    public boolean isConnected() {
        return provider.isConnected();
    }

    @Override
    public void disconnect() {
        provider.disconnect();
    }

    @Override
    public void createDirectory(String path) throws SOSProviderException {
        provider.createDirectory(path);
    }

    @Override
    public void createDirectories(String path) throws SOSProviderException {
        provider.createDirectories(path);
    }

    @Override
    public void delete(String path) throws SOSProviderException {
        provider.delete(path);
    }

    @Override
    public boolean deleteIfExists(String path) throws SOSProviderException {
        return provider.deleteIfExists(path);
    }

    @Override
    public void rename(String source, String target) throws SOSProviderException {
        provider.rename(source, target);
    }

    @Override
    public boolean exists(String path) {
        return provider.exists(path);
    }

    @Override
    public boolean isDirectory(String path) {
        return provider.isDirectory(path);
    }

    // @Override
    // public long getFileSize(String path) throws SOSProviderException {
    // return provider.getFileSize(path);
    // }

    // @Override
    // public long getFileLastModifiedMillis(String path) {
    // return provider.getFileLastModifiedMillis(path);
    // }

    @Override
    public List<ProviderFile> selectFiles(ProviderFileSelection selection) throws SOSProviderException {
        return provider.selectFiles(selection);
    }

    @Override
    public ProviderFile getFileIfExists(String path) throws SOSProviderException {
        return provider.getFileIfExists(path);
    }

    @Override
    public ProviderFile rereadFileIfExists(ProviderFile file) throws SOSProviderException {
        return provider.rereadFileIfExists(file);
    }

    @Override
    public boolean setFileLastModifiedFromMillis(String path, long milliseconds) {
        return provider.setFileLastModifiedFromMillis(path, milliseconds);
    }

    @Override
    public SOSCommandResult executeCommand(String command, SOSTimeout timeout, SOSEnv env) {
        return provider.executeCommand(command, timeout, env);
    }

    @Override
    public SOSCommandResult cancelCommands() {
        return provider.cancelCommands();
    }

    /* -- Additional SSH Provider specific methods ------------------------- */
    public SSHServerInfo getServerInfo() {
        return provider.getServerInfo();
    }

    public void put(String source, String target, int perm) throws SOSProviderException {
        provider.put(source, target, perm);
    }

    public void put(String source, String target) throws SOSProviderException {
        provider.put(source, target);
    }

    public void get(String source, String target) throws SOSProviderException {
        provider.get(source, target);
    }

}
