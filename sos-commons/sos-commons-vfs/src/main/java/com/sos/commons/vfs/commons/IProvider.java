package com.sos.commons.vfs.commons;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.sos.commons.util.beans.SOSCommandResult;
import com.sos.commons.util.beans.SOSEnv;
import com.sos.commons.util.beans.SOSTimeout;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.commons.vfs.commons.file.ProviderFileBuilder;
import com.sos.commons.vfs.commons.file.files.DeleteFilesResult;
import com.sos.commons.vfs.commons.file.files.RenameFilesResult;
import com.sos.commons.vfs.commons.file.selection.ProviderFileSelection;
import com.sos.commons.vfs.exceptions.ProviderConnectException;
import com.sos.commons.vfs.exceptions.ProviderException;

public interface IProvider {

    /** Returns the path separator used by the provider.<br/>
     * For example, it returns '/' for Unix-like systems and '\\' for Windows.
     * 
     * @return the path separator as a string */
    public String getPathSeparator();

    /** Checks if the given path is an absolute path.
     * 
     * @param path the path to check
     * @return {@code true} if the path is absolute, {@code false} otherwise */
    public boolean isAbsolutePath(String path);

    /** Normalizes the given path by resolving any relative components (e.g., '..', '.').
     * 
     * @param path the path to normalize
     * @return the normalized absolute path */
    public String normalizePath(String path);

    /** Converts the given path to the provider-specific path style by replacing path separators.<br/>
     * For example, it replaces backslashes with forward slashes or vice versa depending on the provider's conventions.
     * 
     * @param path the path to convert
     * @return the path in the provider-specific style */
    public String toPathStyle(String path);

    /** Establishes an authenticated connection to the provider.
     * 
     * @throws ProviderConnectException if an error occurs during connection or authentication */
    public void connect() throws ProviderConnectException;

    /** Checks whether a connection to the provider is currently established.
     * 
     * @return {@code true} if the provider is connected<br/>
     *         {@code false} otherwise */
    public boolean isConnected();

    /** Ensures that the connection is established.<br/>
     * If not already connected, it will initiate the connection and authentication process.
     * 
     * @throws ProviderConnectException if an error occurs during connection or authentication */
    public void ensureConnected() throws ProviderConnectException;

    /** Disconnects from the provider and performs any necessary logout or cleanup operations. */
    public void disconnect();

    /** Selects files based on the provided selection criteria.<br/>
     * The selection criteria can include options like recursion, file size limits, minimum/maximum number of files, etc.
     * 
     * @param selection the {@link ProviderFileSelection} containing the criteria for file selection
     * @return a list of {@link ProviderFile} objects that match the selection criteria
     * @throws ProviderException if an error occurs during the file selection process */
    public List<ProviderFile> selectFiles(ProviderFileSelection selection) throws ProviderException;

    /** Tests whether a file/directory exists. **/
    public boolean exists(String path) throws ProviderException;

    /** Creates a directory by creating all nonexistent parent directories first.<br/>
     * No exception is thrown if the directory could not be created because it already exists.
     * 
     * @param path
     * @return {@code true} created<br/>
     *         {@code false} already exist
     * @throws ProviderException */
    public boolean createDirectoriesIfNotExists(String path) throws ProviderException;

    /** Creates all nonexistent directories from the given collection of paths.<br/>
     * No exception is thrown if a directory already exists.
     * 
     * @param paths the collection of directory paths to create
     * @return {@code true} if at least one directory was created<br/>
     *         {@code false} if all directories already existed
     * @throws ProviderException */
    public boolean createDirectoriesIfNotExists(Collection<String> paths) throws ProviderException;

    /** Deletes a file/directory if it exists.<br/>
     * If the path is a directory - deletes empty and not empty directories.
     *
     * @param path the path to the file/directory to delete
     * @return {@code true} if the file/directory was deleted by this method<br/>
     *         {@code false} if the file/directory could not be deleted because it did not exist
     * @throws ProviderException */
    public boolean deleteIfExists(String path) throws ProviderException;

    /** Deletes the specified files if they exist.<br/>
     * 
     * @param files the collection of file paths to delete
     * @param stopOnSingleFileError {@code true} stops deleting on the first error<br/>
     *            {@code false} continues processing all files
     * @return a {@link DeleteFilesResult} containing details about deleted files, missing files, and any encountered errors
     * @throws ProviderException */
    public DeleteFilesResult deleteFilesIfExists(Collection<String> files, boolean stopOnSingleFileError) throws ProviderException;

    /** Renames a file to a target file if the source file exists.<br/>
     * If the target file exists, then the target file is replaced.
     *
     * @param source the current path of the file
     * @param target the new path for the file
     * @return {@code true} if the sourcePath file was renamed by this method<br/>
     *         {@code false} if the sourcePath file could not be renamed because it did not exist
     * @throws ProviderException */
    public boolean renameFileIfSourceExists(String sourcePath, String targetPath) throws ProviderException;

    /** Renames multiple files if the source files exist.<br/>
     * Each entry in the map represents a source file path and its corresponding target path.<br/>
     * If a source file doesn't exist or an error occurs during renaming, the process can stop depending on the stopOnSingleFileError flag.
     * 
     * @param files a map where the key is the source file path and the value is the target file path
     * @param stopOnSingleFileError if {@code true}, the renaming process stops upon encountering an error with any file
     * @return a {@link RenameFilesResult} containing the results of the renaming operation, including errors if any
     * @throws ProviderException if an error occurs during the renaming process */
    public RenameFilesResult renameFilesIfSourceExists(Map<String, String> files, boolean stopOnSingleFileError) throws ProviderException;

    /** Retrieves the file if it exists at the specified path.
     * 
     * @param path the path of the file to retrieve
     * @return a {@link ProviderFile} representing the file if it exists, or null if the file does not exist
     * @throws ProviderException if an error occurs while checking for the file */
    public ProviderFile getFileIfExists(String path) throws ProviderException;

    /** Rereads the file if it exists, refreshing its content.
     * 
     * @param file the {@link ProviderFile} to reread
     * @return the refreshed {@link ProviderFile} if it exists, or null if the file does not exist
     * @throws ProviderException if an error occurs while rereading the file */
    public ProviderFile rereadFileIfExists(ProviderFile file) throws ProviderException;

    /** Retrieves the content of the file at the specified path if the file exists.
     * 
     * @param path the path of the file to retrieve content from
     * @return the content of the file as a string, or null if the file does not exist
     * @throws ProviderException if an error occurs while retrieving the file content */
    public String getFileContentIfExists(String path) throws ProviderException;

    /** Creates a new file or overwrites an existing file at the specified path with the provided content.
     * 
     * @param path the path of the file to create or overwrite
     * @param content the content to write to the file
     * @throws ProviderException if an error occurs while writing the file */
    public void writeFile(String path, String content) throws ProviderException;

    /** Sets the last modified timestamp of the file at the specified path to the given milliseconds value.
     * 
     * @param path the path of the file whose last modified timestamp will be updated
     * @param milliseconds the new last modified timestamp in milliseconds since the epoch
     * @throws ProviderException if an error occurs while setting the file's last modified timestamp */
    public void setFileLastModifiedFromMillis(String path, long milliseconds) throws ProviderException;

    /** Retrieves an {@link InputStream} for the file at the specified path.
     * 
     * @param path the path of the file to retrieve the input stream for
     * @return an {@link InputStream} for reading the file's content
     * @throws ProviderException if an error occurs while retrieving the input stream */
    public InputStream getInputStream(String path) throws ProviderException;

    /** Retrieves an {@link OutputStream} for the file at the specified path.<br/>
     * The file will be opened in append mode if the 'append' parameter is true, otherwise, it will overwrite the existing content.
     * 
     * @param path the path of the file to retrieve the output stream for
     * @param append if {@code true}, the output stream will append to the file; if {@code false}, it will overwrite the file
     * @return an {@link OutputStream} for writing content to the file
     * @throws ProviderException if an error occurs while retrieving the output stream */
    public OutputStream getOutputStream(String path, boolean append) throws ProviderException;

    /** Executes the specified command and returns the result.
     * 
     * @param command the command to execute
     * @return a {@link SOSCommandResult} containing the outcome of the command execution */
    public SOSCommandResult executeCommand(String command);

    /** Executes the specified command with a given timeout and returns the result.
     * 
     * @param command the command to execute
     * @param timeout the timeout duration for the command execution
     * @return a {@link SOSCommandResult} containing the outcome of the command execution */
    public SOSCommandResult executeCommand(String command, SOSTimeout timeout);

    /** Executes the specified command with the given environment variables and returns the result.
     * 
     * @param command the command to execute
     * @param env {@link SOSEnv} the environment variables to set for the command execution
     * @return a {@link SOSCommandResult} containing the outcome of the command execution */
    public SOSCommandResult executeCommand(String command, SOSEnv env);

    /** Executes the specified command with a given timeout,environment variables and returns the result.
     * 
     * @param command the command to execute
     * @param timeout the timeout duration for the command execution
     * @param env {@link SOSEnv} the environment variables to set for the command execution
     * @return a {@link SOSCommandResult} containing the outcome of the command execution */
    public SOSCommandResult executeCommand(String command, SOSTimeout timeout, SOSEnv env);

    /** Cancels the execution of any ongoing commands.
     * 
     * @return a {@link SOSCommandResult} indicating the outcome of the cancel operation */
    public SOSCommandResult cancelCommands();

    public void setProviderFileCreator(Function<ProviderFileBuilder, ProviderFile> creator);

    /** Context e.g. for YADE to determinate Source/Target */
    public void setContext(AProviderContext context);

    public AProviderContext getContext();

}
