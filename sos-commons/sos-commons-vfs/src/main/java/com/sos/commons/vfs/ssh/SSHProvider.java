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
import com.sos.commons.vfs.commons.IProvider;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.commons.vfs.commons.file.files.DeleteFilesResult;
import com.sos.commons.vfs.commons.file.files.RenameFilesResult;
import com.sos.commons.vfs.commons.file.selection.ProviderFileSelection;
import com.sos.commons.vfs.exceptions.SOSProviderConnectException;
import com.sos.commons.vfs.exceptions.SOSProviderException;
import com.sos.commons.vfs.ssh.commons.ASSHProvider;
import com.sos.commons.vfs.ssh.commons.SSHProviderArguments;
import com.sos.commons.vfs.ssh.sshj.SSHJProviderImpl;

public class SSHProvider extends ASSHProvider {

    private final ASSHProvider impl;

    public SSHProvider(ISOSLogger logger, SSHProviderArguments args) throws SOSProviderException {
        impl = initialize(logger, args);
    }

    private static ASSHProvider initialize(ISOSLogger logger, SSHProviderArguments args) throws SOSProviderException {
        return new SSHJProviderImpl(logger, args);
    }

    /** Overrides {@link IProvider#connect()} */
    @Override
    public void connect() throws SOSProviderConnectException {
        impl.connect();
    }

    /** Overrides {@link IProvider#isConnected()} */
    @Override
    public boolean isConnected() {
        return impl.isConnected();
    }

    /** Overrides {@link IProvider#disconnect()} */
    @Override
    public void disconnect() {
        impl.disconnect();
    }

    /** Overrides {@link IProvider#createDirectoriesIfNotExists(String)} */
    @Override
    public boolean createDirectoriesIfNotExists(String path) throws SOSProviderException {
        return impl.createDirectoriesIfNotExists(path);
    }

    /** Overrides {@link IProvider#deleteIfExists(String)} */
    @Override
    public boolean deleteIfExists(String path) throws SOSProviderException {
        return impl.deleteIfExists(path);
    }

    /** Overrides {@link IProvider#exists(String)} */
    @Override
    public boolean exists(String path) {
        return impl.exists(path);
    }

    /** Overrides {@link IProvider#selectFiles(ProviderFileSelection)} */
    @Override
    public List<ProviderFile> selectFiles(ProviderFileSelection selection) throws SOSProviderException {
        return impl.selectFiles(selection);
    }

    /** Overrides {@link IProvider#getFileIfExists(String)} */
    @Override
    public ProviderFile getFileIfExists(String path) throws SOSProviderException {
        return impl.getFileIfExists(path);
    }

    /** Overrides {@link IProvider#rereadFileIfExists(ProviderFile)} */
    @Override
    public ProviderFile rereadFileIfExists(ProviderFile file) throws SOSProviderException {
        return impl.rereadFileIfExists(file);
    }

    /** Overrides {@link IProvider#setFileLastModifiedFromMillis(String, long)} */
    @Override
    public void setFileLastModifiedFromMillis(String path, long milliseconds) throws SOSProviderException {
        impl.setFileLastModifiedFromMillis(path, milliseconds);
    }

    /** Overrides {@link IProvider#executeCommand(String, SOSTimeout, SOSEnv)} */
    @Override
    public SOSCommandResult executeCommand(String command, SOSTimeout timeout, SOSEnv env) {
        return impl.executeCommand(command, timeout, env);
    }

    /** Overrides {@link IProvider#cancelCommands())} */
    @Override
    public SOSCommandResult cancelCommands() {
        return impl.cancelCommands();
    }

    /** Overrides {@link IProvider#getInputStream(String)} */
    @Override
    public InputStream getInputStream(String path) throws SOSProviderException {
        return impl.getInputStream(path);
    }

    /** Overrides {@link IProvider#getOutputStream(String, boolean)} */
    @Override
    public OutputStream getOutputStream(String path, boolean append) throws SOSProviderException {
        return impl.getOutputStream(path, append);
    }

    /** Overrides {@link IProvider#getFileContentIfExists(String)} */
    @Override
    public String getFileContentIfExists(String path) throws SOSProviderException {
        return impl.getFileContentIfExists(path);
    }

    /** Overrides {@link IProvider#writeFile(String, String)} */
    @Override
    public void writeFile(String path, String content) throws SOSProviderException {
        impl.writeFile(path, content);
    }

    /** Overrides {@link IProvider#deleteFilesIfExists(Collection, boolean)} */
    @Override
    public DeleteFilesResult deleteFilesIfExists(Collection<String> paths, boolean stopOnSingleFileError) throws SOSProviderException {
        return impl.deleteFilesIfExists(paths, stopOnSingleFileError);
    }

    /** Overrides {@link IProvider#renameFilesIfSourceExists(String, String)} */
    @Override
    public RenameFilesResult renameFilesIfSourceExists(Map<String, String> paths, boolean stopOnSingleFileError) throws SOSProviderException {
        return impl.renameFilesIfSourceExists(paths, stopOnSingleFileError);
    }

    /* -- Additional SSH Provider specific methods ------------------------- */
    /** Overrides {@link ASSHProvider#put(String, String)} */
    @Override
    public void put(String source, String target) throws SOSProviderException {
        impl.put(source, target);
    }

    /** Overrides {@link ASSHProvider#put(String, String, int)} */
    @Override
    public void put(String source, String target, int perm) throws SOSProviderException {
        impl.put(source, target, perm);
    }

    /** Overrides {@link ASSHProvider#get(String, String)} */
    @Override
    public void get(String source, String target) throws SOSProviderException {
        impl.get(source, target);
    }

}
