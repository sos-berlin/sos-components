package com.sos.commons.vfs.common;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.util.SOSPathUtil;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.SOSCommandResult;
import com.sos.commons.util.common.SOSEnv;
import com.sos.commons.util.common.SOSTimeout;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.commons.vfs.common.file.ProviderFileBuilder;
import com.sos.commons.vfs.exception.SOSProviderConnectException;
import com.sos.commons.vfs.exception.SOSProviderException;
import com.sos.commons.vfs.exception.SOSProviderInitializationException;

public abstract class AProvider<A extends AProviderArguments> implements IProvider {

    public static long DEFAULT_FILE_ATTR_VALUE = -1L;

    private final ISOSLogger logger;
    private final A arguments;

    // Default providerFileCreator function creates a standard ProviderFile using the builder
    private Function<ProviderFileBuilder, ProviderFile> providerFileCreator = builder -> builder.build();
    private Boolean typeSource;
    private String typeInfo;

    public AProvider(ISOSLogger logger, A arguments) throws SOSProviderInitializationException {
        this.logger = logger;
        this.arguments = arguments;
    }

    /** Method to set a custom providerFileCreator (a function that generates ProviderFile using the builder) */
    @Override
    public void setProviderFileCreator(Function<ProviderFileBuilder, ProviderFile> val) {
        providerFileCreator = val;
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

    @Override
    public void ensureConnected() throws SOSProviderConnectException {
        if (!isConnected()) {
            connect();
        }
    }

    @Override
    public String getDirectoryPathWithoutTrailingSeparator(String path) {
        String p = SOSPathUtil.toUnixStylePath(path);
        if (p == null) {
            return null;
        }
        return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }

    @Override
    public String getDirectoryPathWithTrailingSeparator(String path) {
        String p = SOSPathUtil.toUnixStylePath(path);
        if (p == null) {
            return null;
        }
        return path.endsWith("/") ? path : path + "/";
    }

    @Override
    public List<ProviderFile> selectFiles(String path) throws SOSProviderException {
        List<ProviderFile> result = new ArrayList<>();

        result.add(createProviderFile(path, 1, 1));
        return result;
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

    public ISOSLogger getLogger() {
        return logger;
    }

    public A getArguments() {
        return arguments;
    }

    public Boolean typeSource() {
        return typeSource;
    }

    public void typeSource(Boolean val) {
        typeSource = val;
        if (val != null) {
            typeInfo = typeSource ? "[source]" : "[target]";
        }
    }

    public String getTypeInfo() {
        if (typeInfo == null) {
            typeInfo = "";
        }
        return typeInfo;
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

    @SuppressWarnings("unused")
    private static boolean isAbsolutePathStyle(final String path) {
        if (SOSString.isEmpty(path)) {
            return false;
        }

        String np = SOSPathUtil.toUnixStylePath(path);
        if (SOSPathUtil.isAbsolutePathWindowsStyle(np)) {
            return true;
        }
        if (SOSPathUtil.isAbsolutePathUnixStyle(np)) {
            return true;
        }
        if (SOSPathUtil.isAbsolutePathURIStyle(np)) {
            return true;
        }
        return false;
    }

}
