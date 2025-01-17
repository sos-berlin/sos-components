package com.sos.commons.vfs.common;

import com.sos.commons.util.common.SOSCommandResult;
import com.sos.commons.util.common.SOSEnv;
import com.sos.commons.util.common.SOSTimeout;
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

    /** Tests whether a file is a regular file.
     * 
     * @param path
     * @return */
    public boolean isRegularFile(String path);

    /** Regular or Symbolic link file
     * 
     * @param path
     * @return
     * @throws SOSProviderException */
    public ProviderFile getFileIfExists(String path) throws SOSProviderException;

    public boolean isDirectory(String path);

    /** Determines the size of a file or directory.
     * 
     * @param path
     * @return
     * @throws SOSProviderException */
    public Long getSize(String path) throws SOSProviderException;

    /** Returns the modification time in milliseconds or NULL<br/>
     * (the time cannot be evaluated, e.g. because the file does not exist, ...)<br/>
     * Does not throw exceptions, but should report errors at warning level</br>
     * 
     * @param path
     * @return */
    public Long getLastModifiedMillis(String path);

    /** Sets modification time from milliseconds.<br/>
     * Does not throw exceptions, but should report errors at warning level</br>
     * 
     * @param path
     * @param milliseconds
     * @return */
    public boolean setLastModifiedFromMillis(String path, Long milliseconds);

    public boolean isAbsolutePath(String path);

    public SOSCommandResult executeCommand(String command);

    public SOSCommandResult executeCommand(String command, SOSTimeout timeout);

    public SOSCommandResult executeCommand(String command, SOSEnv env);

    public SOSCommandResult executeCommand(String command, SOSTimeout timeout, SOSEnv env);

    public SOSCommandResult cancelCommands();

}
