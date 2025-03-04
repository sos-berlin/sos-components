package com.sos.commons.vfs.ssh;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.sos.commons.util.common.SOSCommandResult;
import com.sos.commons.util.common.SOSEnv;
import com.sos.commons.util.common.SOSTimeout;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.AProviderContext;
import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.commons.vfs.common.file.ProviderFileBuilder;
import com.sos.commons.vfs.common.file.files.DeleteFilesResult;
import com.sos.commons.vfs.common.file.files.RenameFilesResult;
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
        provider = initializeProvider(logger, args);
    }

    private static ASSHProvider initializeProvider(ISOSLogger logger, SSHProviderArguments args) throws SOSProviderException {
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
    public void setProviderFileCreator(Function<ProviderFileBuilder, ProviderFile> creator) {
        provider.setProviderFileCreator(creator);
    }

    @Override
    public void setContext(AProviderContext context) {
        provider.setContext(context);
    }

    @Override
    public boolean createDirectoriesIfNotExist(String path) throws SOSProviderException {
        return provider.createDirectoriesIfNotExist(path);
    }

    @Override
    public boolean deleteIfExists(String path) throws SOSProviderException {
        return provider.deleteIfExists(path);
    }

    @Override
    public boolean exists(String path) {
        return provider.exists(path);
    }

    @Override
    public boolean isDirectory(String path) {
        return provider.isDirectory(path);
    }

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
    public void setFileLastModifiedFromMillis(String path, long milliseconds) throws SOSProviderException {
        provider.setFileLastModifiedFromMillis(path, milliseconds);
    }

    @Override
    public SOSCommandResult executeCommand(String command, SOSTimeout timeout, SOSEnv env) {
        return provider.executeCommand(command, timeout, env);
    }

    @Override
    public SOSCommandResult cancelCommands() {
        return provider.cancelCommands();
    }

    @Override
    public InputStream getInputStream(String path) throws SOSProviderException {
        return provider.getInputStream(path);
    }

    @Override
    public OutputStream getOutputStream(String path, boolean append) throws SOSProviderException {
        return provider.getOutputStream(path, append);
    }

    @Override
    public String getFileContentIfExists(String path) throws SOSProviderException {
        return provider.getFileContentIfExists(path);
    }

    @Override
    public void writeFile(String path, String content) throws SOSProviderException {
        provider.writeFile(path, content);
    }

    @Override
    public DeleteFilesResult deleteFilesIfExist(Collection<String> paths, boolean stopOnSingleFileError) throws SOSProviderException {
        return provider.deleteFilesIfExist(paths, stopOnSingleFileError);
    }

    @Override
    public RenameFilesResult renameFilesIfExist(Map<String, String> paths, boolean stopOnSingleFileError) throws SOSProviderException {
        return provider.renameFilesIfExist(paths, stopOnSingleFileError);
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
