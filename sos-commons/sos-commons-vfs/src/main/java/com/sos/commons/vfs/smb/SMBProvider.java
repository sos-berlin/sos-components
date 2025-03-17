package com.sos.commons.vfs.smb;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.sos.commons.util.beans.SOSCommandResult;
import com.sos.commons.util.beans.SOSEnv;
import com.sos.commons.util.beans.SOSTimeout;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.commons.AProvider;
import com.sos.commons.vfs.commons.IProvider;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.commons.vfs.commons.file.files.DeleteFilesResult;
import com.sos.commons.vfs.commons.file.files.RenameFilesResult;
import com.sos.commons.vfs.commons.file.selection.ProviderFileSelection;
import com.sos.commons.vfs.exceptions.ProviderConnectException;
import com.sos.commons.vfs.exceptions.ProviderException;
import com.sos.commons.vfs.smb.commons.ASMBProvider;
import com.sos.commons.vfs.smb.commons.SMBProviderArguments;

public class SMBProvider extends ASMBProvider {

    private final ASMBProvider impl;

    public SMBProvider(ISOSLogger logger, SMBProviderArguments args) throws ProviderException {
        impl = initialize(logger, args);
    }

    private static ASMBProvider initialize(ISOSLogger logger, SMBProviderArguments args) throws ProviderException {
        return new com.sos.commons.vfs.smb.smbj.ProviderImpl(logger, args);
    }

    /** Overrides {@link IProvider#connect()} */
    @Override
    public void connect() throws ProviderConnectException {
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
    public boolean createDirectoriesIfNotExists(String path) throws ProviderException {
        return impl.createDirectoriesIfNotExists(path);
    }

    /** Overrides {@link IProvider#deleteIfExists(String)} */
    @Override
    public boolean deleteIfExists(String path) throws ProviderException {
        return impl.deleteIfExists(path);
    }

    /** Overrides {@link IProvider#exists(String)} */
    @Override
    public boolean exists(String path) {
        return impl.exists(path);
    }

    /** Overrides {@link IProvider#selectFiles(ProviderFileSelection)} */
    @Override
    public List<ProviderFile> selectFiles(ProviderFileSelection selection) throws ProviderException {
        return impl.selectFiles(selection);
    }

    /** Overrides {@link IProvider#getFileIfExists(String)} */
    @Override
    public ProviderFile getFileIfExists(String path) throws ProviderException {
        return impl.getFileIfExists(path);
    }

    /** Overrides {@link IProvider#rereadFileIfExists(ProviderFile)} */
    @Override
    public ProviderFile rereadFileIfExists(ProviderFile file) throws ProviderException {
        return impl.rereadFileIfExists(file);
    }

    /** Overrides {@link IProvider#setFileLastModifiedFromMillis(String, long)} */
    @Override
    public void setFileLastModifiedFromMillis(String path, long milliseconds) throws ProviderException {
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
    public InputStream getInputStream(String path) throws ProviderException {
        return impl.getInputStream(path);
    }

    /** Overrides {@link IProvider#getOutputStream(String, boolean)} */
    @Override
    public OutputStream getOutputStream(String path, boolean append) throws ProviderException {
        return impl.getOutputStream(path, append);
    }

    /** Overrides {@link IProvider#getFileContentIfExists(String)} */
    @Override
    public String getFileContentIfExists(String path) throws ProviderException {
        return impl.getFileContentIfExists(path);
    }

    /** Overrides {@link IProvider#writeFile(String, String)} */
    @Override
    public void writeFile(String path, String content) throws ProviderException {
        impl.writeFile(path, content);
    }

    /** Overrides {@link IProvider#deleteFilesIfExists(Collection, boolean)} */
    @Override
    public DeleteFilesResult deleteFilesIfExists(Collection<String> paths, boolean stopOnSingleFileError) throws ProviderException {
        return impl.deleteFilesIfExists(paths, stopOnSingleFileError);
    }

    /** Overrides {@link IProvider#renameFilesIfSourceExists(String, String)} */
    @Override
    public RenameFilesResult renameFilesIfSourceExists(Map<String, String> paths, boolean stopOnSingleFileError) throws ProviderException {
        return impl.renameFilesIfSourceExists(paths, stopOnSingleFileError);
    }

    /** Overrides {@link AProvider#validatePrerequisites(String)} */
    @Override
    public void validatePrerequisites(String method) throws ProviderException {
        impl.validatePrerequisites(method);
    }

    /* -- Additional SMB Provider specific methods ------------------------- */

}
