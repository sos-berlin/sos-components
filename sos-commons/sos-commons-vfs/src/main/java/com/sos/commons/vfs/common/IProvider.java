package com.sos.commons.vfs.common;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.sos.commons.util.common.SOSCommandResult;
import com.sos.commons.util.common.SOSEnv;
import com.sos.commons.util.common.SOSTimeout;
import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.commons.vfs.common.file.ProviderFileBuilder;
import com.sos.commons.vfs.common.file.files.DeleteFilesResult;
import com.sos.commons.vfs.common.file.files.RenameFilesResult;
import com.sos.commons.vfs.common.file.selection.ProviderFileSelection;
import com.sos.commons.vfs.exception.SOSProviderConnectException;
import com.sos.commons.vfs.exception.SOSProviderException;

public interface IProvider {

    public void connect() throws SOSProviderConnectException;

    public void ensureConnected() throws SOSProviderConnectException;

    public boolean isConnected();

    public void disconnect();

    /** Creates a directory by creating all nonexistent parent directories first.<br/>
     * Unlike the {@link createDirectory} method, an exception is not thrown if the directory could not be created because it already exists.
     * 
     * @param path
     * @return false - already exist, true - created
     * @throws SOSProviderException */
    public boolean createDirectoriesIfNotExist(String path) throws SOSProviderException;

    public boolean createDirectoriesIfNotExist(Collection<String> paths) throws SOSProviderException;

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

    public DeleteFilesResult deleteFilesIfExist(Collection<String> files, boolean stopOnSingleFileError) throws SOSProviderException;

    /** Move/rename a file to a target file.<br/>
     * If the target file exists, then the target file is replaced.
     *
     * @param source
     * @param target
     * @throws SOSProviderException */
    public RenameFilesResult renameFileIfExists(String sourcePath, String targetPath) throws SOSProviderException;

    /** Move/rename multiple files
     * 
     * @param files Map.key=source,Map.value=target
     * @param stopOnSingleFileError
     * @return
     * @throws SOSProviderException */
    public RenameFilesResult renameFilesIfExist(Map<String, String> files, boolean stopOnSingleFileError) throws SOSProviderException;

    /** Tests whether a file/directory exists. **/
    public boolean exists(String path);

    public boolean isDirectory(String path);

    public boolean isAbsolutePath(String path);

    /** Directory path without trailing path separator */
    public String getDirectoryPath(String path);

    /** Directory path with trailing path separator */
    public String getDirectoryPathWithTrailingPathSeparator(String path);

    public String toPathStyle(String path);

    public String getPathSeparator();

    public ProviderFile getFileIfExists(String path) throws SOSProviderException;

    public ProviderFile rereadFileIfExists(ProviderFile file) throws SOSProviderException;

    public List<ProviderFile> selectFiles(ProviderFileSelection selection) throws SOSProviderException;

    public String getFileContentIfExists(String path) throws SOSProviderException;

    /** Sets modification time from milliseconds.<br/>
     * 
     * @param path
     * @param milliseconds
     * @return */
    public void setFileLastModifiedFromMillis(String path, long milliseconds) throws SOSProviderException;

    public InputStream getInputStream(String path) throws SOSProviderException;

    public OutputStream getOutputStream(String path, boolean append) throws SOSProviderException;

    public void setProviderFileCreator(Function<ProviderFileBuilder, ProviderFile> creator);

    /** Context e.g. for YADE to determinate Source/Target */
    public void setContext(AProviderContext context);

    public AProviderContext getContext();

    public SOSCommandResult executeCommand(String command);

    public SOSCommandResult executeCommand(String command, SOSTimeout timeout);

    public SOSCommandResult executeCommand(String command, SOSEnv env);

    public SOSCommandResult executeCommand(String command, SOSTimeout timeout, SOSEnv env);

    public SOSCommandResult cancelCommands();

}
