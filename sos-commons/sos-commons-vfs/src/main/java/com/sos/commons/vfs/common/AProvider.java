package com.sos.commons.vfs.common;

import com.sos.commons.util.common.SOSCommandResult;
import com.sos.commons.util.common.SOSEnv;
import com.sos.commons.util.common.SOSTimeout;

public abstract class AProvider<A extends AProviderArguments> {

    private final A arguments;

    public AProvider(A arguments) {
        this.arguments = arguments;
    }

    public abstract void connect() throws Exception;

    public abstract boolean isConnected();

    public abstract void disconnect();

    public abstract void createDirectory(String path) throws Exception;

    public abstract void createDirectories(String path) throws Exception;

    /** Deletes a file/directory.
     *
     * <p>
     * If the file is a directory - deletes empty and not empty directories.
     *
     * <p>
     * throws SOSNoSuchFileException
     * 
     * if the file does not exist <i>(optional specific exception)</i>
     * 
     * @param path the path to the file to delete */
    public abstract void delete(String path) throws Exception;

    /** Deletes a file/directory if it exists.
     *
     * <p>
     * If the file is a directory - deletes empty and not empty directories.
     *
     * @param path the path to the file to delete
     *
     * @return {@code true} if the file was deleted by this method; {@code
     *          false} if the file could not be deleted because it did not exist */
    public abstract boolean deleteIfExists(String path) throws Exception;

    /** Renames a file/directory. */
    public abstract void rename(String oldpath, String newpath) throws Exception;

    /** Tests whether a file/directory exists. **/
    public abstract boolean exists(String path);

    public abstract boolean isFile(String path);

    public abstract boolean isDirectory(String path);

    public abstract long getSize(String path) throws Exception;

    public abstract long getModificationTime(String path) throws Exception;

    public abstract SOSCommandResult executeCommand(String command);

    public abstract SOSCommandResult executeCommand(String command, SOSTimeout timeout);

    public abstract SOSCommandResult executeCommand(String command, SOSEnv env);

    public abstract SOSCommandResult executeCommand(String command, SOSTimeout timeout, SOSEnv env);

    public A getArguments() {
        return arguments;
    }

}
