package com.sos.commons.vfs.ssh;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.sos.commons.util.common.SOSCommandResult;
import com.sos.commons.util.common.SOSEnv;
import com.sos.commons.util.common.SOSTimeout;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.IProvider;
import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.commons.vfs.common.file.files.DeleteFilesResult;
import com.sos.commons.vfs.common.file.files.RenameFilesResult;
import com.sos.commons.vfs.common.file.selection.ProviderFileSelection;
import com.sos.commons.vfs.exceptions.SOSProviderConnectException;
import com.sos.commons.vfs.exceptions.SOSProviderException;
import com.sos.commons.vfs.ssh.common.ASSHProvider;
import com.sos.commons.vfs.ssh.common.SSHProviderArguments;
import com.sos.commons.vfs.ssh.sshj.SSHJProvider;

public class SSHProvider extends ASSHProvider {

    private final ASSHProvider provider;

    public SSHProvider(ISOSLogger logger, SSHProviderArguments args) throws SOSProviderException {
        provider = initializeProvider(logger, args);
    }

    private static ASSHProvider initializeProvider(ISOSLogger logger, SSHProviderArguments args) throws SOSProviderException {
        return new SSHJProvider(logger, args);
    }

    /** Overrides {@link IProvider#connect()} */
    @Override
    public void connect() throws SOSProviderConnectException {
        provider.connect();
    }

    /** Overrides {@link IProvider#isConnected()} */
    @Override
    public boolean isConnected() {
        return provider.isConnected();
    }

    /** Overrides {@link IProvider#disconnect()} */
    @Override
    public void disconnect() {
        provider.disconnect();
    }

    /** Overrides {@link IProvider#createDirectoriesIfNotExists(String)} */
    @Override
    public boolean createDirectoriesIfNotExists(String path) throws SOSProviderException {
        return provider.createDirectoriesIfNotExists(path);
    }

    /** Overrides {@link IProvider#deleteIfExists(String)} */
    @Override
    public boolean deleteIfExists(String path) throws SOSProviderException {
        return provider.deleteIfExists(path);
    }

    /** Overrides {@link IProvider#exists(String)} */
    @Override
    public boolean exists(String path) {
        return provider.exists(path);
    }

    /** Overrides {@link IProvider#selectFiles(ProviderFileSelection)} */
    @Override
    public List<ProviderFile> selectFiles(ProviderFileSelection selection) throws SOSProviderException {
        return provider.selectFiles(selection);
    }

    /** Overrides {@link IProvider#getFileIfExists(String)} */
    @Override
    public ProviderFile getFileIfExists(String path) throws SOSProviderException {
        return provider.getFileIfExists(path);
    }

    /** Overrides {@link IProvider#rereadFileIfExists(ProviderFile)} */
    @Override
    public ProviderFile rereadFileIfExists(ProviderFile file) throws SOSProviderException {
        return provider.rereadFileIfExists(file);
    }

    /** Overrides {@link IProvider#setFileLastModifiedFromMillis(String, long)} */
    @Override
    public void setFileLastModifiedFromMillis(String path, long milliseconds) throws SOSProviderException {
        provider.setFileLastModifiedFromMillis(path, milliseconds);
    }

    /** Overrides {@link IProvider#executeCommand(String, SOSTimeout, SOSEnv)} */
    @Override
    public SOSCommandResult executeCommand(String command, SOSTimeout timeout, SOSEnv env) {
        return provider.executeCommand(command, timeout, env);
    }

    /** Overrides {@link IProvider#cancelCommands())} */
    @Override
    public SOSCommandResult cancelCommands() {
        return provider.cancelCommands();
    }

    /** Overrides {@link IProvider#getInputStream(String)} */
    @Override
    public InputStream getInputStream(String path) throws SOSProviderException {
        return provider.getInputStream(path);
    }

    /** Overrides {@link IProvider#getOutputStream(String, boolean)} */
    @Override
    public OutputStream getOutputStream(String path, boolean append) throws SOSProviderException {
        return provider.getOutputStream(path, append);
    }

    /** Overrides {@link IProvider#getFileContentIfExists(String)} */
    @Override
    public String getFileContentIfExists(String path) throws SOSProviderException {
        return provider.getFileContentIfExists(path);
    }

    /** Overrides {@link IProvider#writeFile(String, String)} */
    @Override
    public void writeFile(String path, String content) throws SOSProviderException {
        provider.writeFile(path, content);
    }

    /** Overrides {@link IProvider#deleteFilesIfExists(Collection, boolean)} */
    @Override
    public DeleteFilesResult deleteFilesIfExists(Collection<String> paths, boolean stopOnSingleFileError) throws SOSProviderException {
        return provider.deleteFilesIfExists(paths, stopOnSingleFileError);
    }

    /** Overrides {@link IProvider#renameFilesIfSourceExists(String, String)} */
    @Override
    public RenameFilesResult renameFilesIfSourceExists(Map<String, String> paths, boolean stopOnSingleFileError) throws SOSProviderException {
        return provider.renameFilesIfSourceExists(paths, stopOnSingleFileError);
    }

    /* -- Additional SSH Provider specific methods ------------------------- */
    /** Overrides {@link ASSHProvider#put(String, String)} */
    @Override
    public void put(String source, String target) throws SOSProviderException {
        provider.put(source, target);
    }

    /** Overrides {@link ASSHProvider#put(String, String, int)} */
    @Override
    public void put(String source, String target, int perm) throws SOSProviderException {
        provider.put(source, target, perm);
    }

    /** Overrides {@link ASSHProvider#get(String, String)} */
    @Override
    public void get(String source, String target) throws SOSProviderException {
        provider.get(source, target);
    }

}
