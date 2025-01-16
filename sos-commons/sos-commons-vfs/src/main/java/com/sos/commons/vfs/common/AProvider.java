package com.sos.commons.vfs.common;

import com.sos.commons.credentialstore.CredentialStoreArguments;
import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.SOSCommandResult;
import com.sos.commons.util.common.SOSEnv;
import com.sos.commons.util.common.SOSTimeout;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.exception.SOSProviderConnectException;
import com.sos.commons.vfs.exception.SOSProviderException;

public abstract class AProvider<A extends AProviderArguments> {

    private final ISOSLogger logger;
    private final A arguments;

    public AProvider(ISOSLogger logger, A arguments, CredentialStoreArguments csArgs) {
        this.logger = logger;
        this.arguments = arguments;
        if (this.arguments != null) {
            this.arguments.setCredentialStore(csArgs);
        }
    }

    public abstract void connect() throws SOSProviderConnectException;

    public abstract boolean isConnected();

    public abstract void disconnect();

    /** Creates a new directory. <br/>
     * The existence of the directory is not checked.<br/>
     * The existence of the parent directory is required.<br/>
     * void - because not all providers return Boolean/...
     * 
     * @param path
     * @throws SOSProviderException */
    public abstract void createDirectory(String path) throws SOSProviderException;

    /** Creates a directory by creating all nonexistent parent directories first.<br/>
     * Unlike the {@link createDirectory} method, an exception is not thrown if the directory could not be created because it already exists.
     * 
     * @param path
     * @throws SOSProviderException */
    public abstract void createDirectories(String path) throws SOSProviderException;

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
    public abstract void delete(String path) throws SOSProviderException;

    /** Deletes a file/directory if it exists.
     *
     * <p>
     * If the file is a directory - deletes empty and not empty directories.
     *
     * @param path the path to the file/directory to delete
     *
     * @return {@code true} if the file/directory was deleted by this method;<br/>
     *         {@code false} if the file/directory could not be deleted because it did not exist */
    public abstract boolean deleteIfExists(String path) throws SOSProviderException;

    /** Move or rename a file to a target file.<br/>
     * If the target file exists, then the target file is replaced.
     *
     * @param source
     * @param target
     * @throws SOSProviderException */
    public abstract void rename(String source, String target) throws SOSProviderException;

    /** Tests whether a file/directory exists. **/
    public abstract boolean exists(String path);

    /** Tests whether a file is a regular file.
     * 
     * @param path
     * @return */
    public abstract boolean isRegularFile(String path);

    public abstract boolean isDirectory(String path);

    /** Determines the size of a file or directory.
     * 
     * @param path
     * @return
     * @throws SOSProviderException */
    public abstract Long getSize(String path) throws SOSProviderException;

    /** Returns the modification time in milliseconds or NULL<br/>
     * (the time cannot be evaluated, e.g. because the file does not exist, ...)<br/>
     * Does not throw exceptions, but should report errors at warning level</br>
     * 
     * @param path
     * @return */
    public abstract Long getLastModifiedMillis(String path);

    /** Sets modification time from milliseconds.<br/>
     * Does not throw exceptions, but should report errors at warning level</br>
     * 
     * @param path
     * @param milliseconds
     * @return */
    public abstract boolean setLastModifiedFromMillis(String path, Long milliseconds);

    public abstract SOSCommandResult executeCommand(String command);

    public abstract SOSCommandResult executeCommand(String command, SOSTimeout timeout);

    public abstract SOSCommandResult executeCommand(String command, SOSEnv env);

    public abstract SOSCommandResult executeCommand(String command, SOSTimeout timeout, SOSEnv env);

    public abstract SOSCommandResult cancelCommands();

    public ISOSLogger getLogger() {
        return logger;
    }

    public A getArguments() {
        return arguments;
    }

    public static String millis2string(int val) {
        if (val <= 0) {
            return String.valueOf(val).concat("ms");
        }
        try {
            return String.valueOf(Math.round(val / 1000)).concat("s");
        } catch (Throwable e) {
            return String.valueOf(val).concat("ms");
        }
    }

    public static void checkParam(String paramValue, String msg) throws SOSProviderException {
        if (SOSString.isEmpty(paramValue)) {
            throw new SOSProviderException(new SOSMissingDataException(msg));
        }
    }

    public static boolean isValidModificationTime(Long milliseconds) {
        return milliseconds != null && milliseconds.longValue() > 0;
    }
}
