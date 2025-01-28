package com.sos.commons.vfs.common;

import java.util.List;
import java.util.function.Function;

import com.sos.commons.util.common.SOSCommandResult;
import com.sos.commons.util.common.SOSEnv;
import com.sos.commons.util.common.SOSTimeout;
import com.sos.commons.vfs.common.file.ProviderDirectoryPath;
import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.commons.vfs.common.file.ProviderFileBuilder;
import com.sos.commons.vfs.common.file.selection.ProviderFileSelection;
import com.sos.commons.vfs.exception.SOSProviderConnectException;
import com.sos.commons.vfs.exception.SOSProviderException;

public interface IProvider {

    public void connect() throws SOSProviderConnectException;

    public void ensureConnected() throws SOSProviderConnectException;

    public boolean isConnected();

    public void disconnect();

    /** Creates a new directory. <br/>
     * The existence of the directory is not checked.<br/>
     * The existence of the parent directory is required.<br/>
     * void - because not all providers return Boolean/...
     * 
     * @param path
     * @throws SOSProviderException */
    public void createDirectory(String path) throws SOSProviderException;

    /** Creates a directory by creating all nonexistent parent directories first.<br/>
     * Unlike the {@link createDirectory} method, an exception is not thrown if the directory could not be created because it already exists.
     * 
     * @param path
     * @throws SOSProviderException */
    public void createDirectories(String path) throws SOSProviderException;

    /** Deletes a file/directory.
     *
     * <p>
     * If the file is a directory - deletes empty and not empty directories.
     *
     * <p>
     * throws SOSNoSuchFileException
     * 
     * if the file/directory does not exist <i>(optional specific exception)</i>
     * 
     * @param path the path to the file/directory to delete */
    public void delete(String path) throws SOSProviderException;

    /** Deletes a file/directory if it exists.
     *
     * <p>
     * If the file is a directory - deletes empty and not empty directories.
     *
     * @param path the path to the file/directory to delete
     *
     * @return {@code true} if the file/directory was deleted by this method;<br/>
     *         {@code false} if the file/directory could not be deleted because it did not exist */
    public boolean deleteIfExists(String path) throws SOSProviderException;

    /** Move or rename a file to a target file.<br/>
     * If the target file exists, then the target file is replaced.
     *
     * @param source
     * @param target
     * @throws SOSProviderException */
    public void rename(String source, String target) throws SOSProviderException;

    /** Tests whether a file/directory exists. **/
    public boolean exists(String path);

    public boolean isDirectory(String path);

    public boolean isAbsolutePath(String path);

    public ProviderDirectoryPath getDirectoryPath(String path);

    public ProviderFile getFileIfExists(String path) throws SOSProviderException;

    public ProviderFile rereadFileIfExists(ProviderFile file) throws SOSProviderException;

    public void setProviderFileCreator(Function<ProviderFileBuilder, ProviderFile> creator);

    public List<ProviderFile> selectFiles(ProviderFileSelection selection) throws SOSProviderException;

    // public long getFileSize(String path) throws SOSProviderException;

    /** Returns the modification time in milliseconds or -1<br/>
     * (the time cannot be evaluated, e.g. because the file does not exist, ...)<br/>
     * Does not throw exceptions, but should report errors at warning level</br>
     * 
     * @param path
     * @return */
    // public long getFileLastModifiedMillis(String path);

    /** Sets modification time from milliseconds.<br/>
     * Does not throw exceptions, but should report errors at warning level</br>
     * 
     * @param path
     * @param milliseconds
     * @return */
    public boolean setFileLastModifiedFromMillis(String path, long milliseconds);

    /** Context e.g. for YADE to determinate Source/Target */
    public void setContext(AProviderContext context);

    public AProviderContext getContext();

    public SOSCommandResult executeCommand(String command);

    public SOSCommandResult executeCommand(String command, SOSTimeout timeout);

    public SOSCommandResult executeCommand(String command, SOSEnv env);

    public SOSCommandResult executeCommand(String command, SOSTimeout timeout, SOSEnv env);

    public SOSCommandResult cancelCommands();

}
