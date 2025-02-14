package com.sos.commons.vfs.common;

import java.io.Closeable;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.SOSCommandResult;
import com.sos.commons.util.common.SOSEnv;
import com.sos.commons.util.common.SOSTimeout;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.commons.vfs.common.file.ProviderFileBuilder;
import com.sos.commons.vfs.common.file.selection.ProviderFileSelection;
import com.sos.commons.vfs.common.file.selection.ProviderFileSelectionConfig;
import com.sos.commons.vfs.exception.SOSProviderConnectException;
import com.sos.commons.vfs.exception.SOSProviderException;
import com.sos.commons.vfs.exception.SOSProviderInitializationException;

public abstract class AProvider<A extends AProviderArguments> implements IProvider {

    public static long DEFAULT_FILE_ATTR_VALUE = -1L;

    private final ISOSLogger logger;
    private final A arguments;

    // Default providerFileCreator function creates a standard ProviderFile using the builder
    private Function<ProviderFileBuilder, ProviderFile> providerFileCreator = builder -> builder.build();
    // source/target type - logging - is not set if only one provider is used (e.g. SSH JITL Job)
    private AProviderContext context;

    public AProvider(ISOSLogger logger, A arguments) throws SOSProviderInitializationException {
        this.logger = logger;
        this.arguments = arguments;
    }

    /** Method to set a custom providerFileCreator (a function that generates ProviderFile using the builder) */
    @Override
    public void setProviderFileCreator(Function<ProviderFileBuilder, ProviderFile> val) {
        providerFileCreator = val;
    }

    public Function<ProviderFileBuilder, ProviderFile> getProviderFileCreator() {
        return providerFileCreator;
    }

    /** Method to create a ProviderFile by using the providerFileCreator function
     * 
     * @param fullPath
     * @param size
     * @param lastModifiedMillis
     * @return */
    public ProviderFile createProviderFile(String fullPath, long size, long lastModifiedMillis) {
        return providerFileCreator.apply(new ProviderFileBuilder().fullPath(fullPath).size(size).lastModifiedMillis(lastModifiedMillis));
    }

    public static ProviderFile createProviderFile(Function<ProviderFileBuilder, ProviderFile> providerFileCreator, String fullPath, long size,
            long lastModifiedMillis) {
        return providerFileCreator.apply(new ProviderFileBuilder().fullPath(fullPath).size(size).lastModifiedMillis(lastModifiedMillis));
    }

    @Override
    public void setContext(AProviderContext val) {
        context = val;
    }

    @Override
    public AProviderContext getContext() {
        return context;
    }

    public String getLogPrefix() {
        return context == null ? "" : context.getLogPrefix();
    }

    @Override
    public void ensureConnected() throws SOSProviderConnectException {
        if (!isConnected()) {
            connect();
        }
    }

    @Override
    public boolean createDirectoriesIfNotExist(Collection<String> paths) throws SOSProviderException {
        if (SOSCollection.isEmpty(paths)) {
            return false;
        }
        boolean result = false;
        for (String path : paths) {
            if (createDirectoriesIfNotExist(path)) {
                result = true;
            }
        }
        return result;
    }

    /** Provider (non-YADE) method */
    public List<ProviderFile> selectFiles(String directory) throws SOSProviderException {
        return selectFiles(new ProviderFileSelection(new ProviderFileSelectionConfig.Builder().directory(directory).build()));
    }

    @Override
    public SOSCommandResult executeCommand(String command) {
        return executeCommand(command, null, null);
    }

    @Override
    public SOSCommandResult executeCommand(String command, SOSTimeout timeout) {
        return executeCommand(command, timeout, null);
    }

    @Override
    public SOSCommandResult executeCommand(String command, SOSEnv env) {
        return executeCommand(command, null, env);
    }

    public String getPathOperationPrefix(String path) {
        return getLogPrefix() + "[" + path + "]";
    }

    public void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Throwable e) {
            }
        }
    }

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

    public static boolean isValidFileSize(long fileSize) {
        return fileSize >= 0;
    }

    public static boolean isValidModificationTime(long milliseconds) {
        return milliseconds > 0;
    }

    public static void checkModificationTime(String path, long milliseconds) throws SOSProviderException {
        if (!isValidModificationTime(milliseconds)) {
            throw new SOSProviderException("[" + path + "][" + milliseconds + "]not valid modification time");
        }
    }
}
