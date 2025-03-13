package com.sos.commons.vfs.commons;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;

import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSPathUtil;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.beans.SOSCommandResult;
import com.sos.commons.util.beans.SOSEnv;
import com.sos.commons.util.beans.SOSTimeout;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.commons.vfs.commons.file.ProviderFileBuilder;
import com.sos.commons.vfs.commons.file.files.RenameFilesResult;
import com.sos.commons.vfs.commons.file.selection.ProviderFileSelection;
import com.sos.commons.vfs.commons.file.selection.ProviderFileSelectionConfig;
import com.sos.commons.vfs.exceptions.SOSProviderConnectException;
import com.sos.commons.vfs.exceptions.SOSProviderException;
import com.sos.commons.vfs.exceptions.SOSProviderInitializationException;

public abstract class AProvider<A extends AProviderArguments> implements IProvider {

    public static long DEFAULT_FILE_ATTR_VALUE = -1L;

    private final ISOSLogger logger;
    private final A arguments;

    /** Default providerFileCreator function creates a standard ProviderFile using the builder */
    private Function<ProviderFileBuilder, ProviderFile> providerFileCreator = builder -> builder.build(this);
    /** Source/Target type - logging - is not set if only one provider is used (e.g. SSH JITL Job) */
    private AProviderContext context;

    /** For Connect/Disconnect logging e.g. LocalProvider=null, SSH/FTP Provider=user@server:port */
    private String accessInfo;

    private String logPrefix;

    public AProvider(ISOSLogger logger, A arguments) throws SOSProviderInitializationException {
        this.logger = logger;
        this.arguments = arguments;
    }

    /** Method to set a custom providerFileCreator (a function that generates ProviderFile using the builder)<br/>
     * Overrides {@link IProvider#setProviderFileCreator(Function)} */
    @Override
    public void setProviderFileCreator(Function<ProviderFileBuilder, ProviderFile> val) {
        providerFileCreator = val;
    }

    /** Overrides {@link IProvider#setContext(AProviderContext)} */
    @Override
    public void setContext(AProviderContext val) {
        context = val;
    }

    /** Overrides {@link IProvider#getContext()} */
    @Override
    public AProviderContext getContext() {
        return context;
    }

    /** Overrides {@link IProvider#ensureConnected())} */
    @Override
    public void ensureConnected() throws SOSProviderConnectException {
        if (!isConnected()) {
            connect();
        }
    }

    /** Overrides {@link IProvider#createDirectoriesIfNotExists(Collection)} */
    @Override
    public boolean createDirectoriesIfNotExists(Collection<String> paths) throws SOSProviderException {
        if (SOSCollection.isEmpty(paths)) {
            return false;
        }
        boolean result = false;
        for (String path : paths) {
            if (createDirectoriesIfNotExists(path)) {
                result = true;
            }
        }
        return result;
    }

    /** Overrides {@link IProvider#renameFileIfSourceExists(String, String)} */
    @Override
    public RenameFilesResult renameFileIfSourceExists(String sourcePath, String targetPath) throws SOSProviderException {
        checkParam("renameFileIfSourceExists", sourcePath, "sourcePath");
        checkParam("renameFileIfSourceExists", targetPath, "targetPath");

        return renameFilesIfSourceExists(Collections.singletonMap(sourcePath, targetPath), true);
    }

    /** Overrides {@link IProvider#toPathStyle(String)} */
    @Override
    public String toPathStyle(String path) {
        return SOSPathUtil.isUnixStylePathSeparator(getPathSeparator()) ? SOSPathUtil.toUnixStyle(path) : SOSPathUtil.toWindowsStyle(path);
    }

    /** Overrides {@link IProvider#executeCommand(String)} */
    @Override
    public SOSCommandResult executeCommand(String command) {
        return executeCommand(command, null, null);
    }

    /** Overrides {@link IProvider#executeCommand(String, SOSTimeout)} */
    @Override
    public SOSCommandResult executeCommand(String command, SOSTimeout timeout) {
        return executeCommand(command, timeout, null);
    }

    /** Overrides {@link IProvider#executeCommand(String, SOSTimeout, SOSEnv)} */
    @Override
    public SOSCommandResult executeCommand(String command, SOSEnv env) {
        return executeCommand(command, null, env);
    }

    /** Overrides {@link IProvider#executeCommand(String,SOSTinmeout,SOSEnv)} */
    @Override
    public SOSCommandResult executeCommand(String command, SOSTimeout timeout, SOSEnv env) {
        logNotImpementedMethod("executeCommand", "[command]" + command);
        return null;
    }

    // other thread
    /** Overrides {@link IProvider#cancelCommands()} */
    @Override
    public SOSCommandResult cancelCommands() {
        return null;
    }

    public void setSystemPropertiesFromFiles() {
        if (SOSCollection.isEmpty(getArguments().getSystemPropertyFiles().getValue())) {
            return;
        }
        String method = "setSystemPropertiesFromFiles";
        logger.info("%s[%s][files]", getLogPrefix(), method, SOSString.join(getArguments().getSystemPropertyFiles().getValue(), ",", f -> f
                .toString()));
        Properties p = new Properties();
        for (Path file : getArguments().getSystemPropertyFiles().getValue()) {
            if (Files.exists(file) && Files.isRegularFile(file)) {
                try (BufferedReader reader = Files.newBufferedReader(file)) {
                    p.load(reader);
                    logger.info("[%s][%s]loaded", method, file);
                } catch (Throwable e) {
                    logger.warn("[%s][%s][failed]%s", method, file, e.toString());
                }
            } else {
                logger.warn("[%s][%s]does not exist or is not a regular file", method, file);
            }
        }

        for (String n : p.stringPropertyNames()) {
            String v = p.getProperty(n);
            if (logger.isDebugEnabled()) {
                logger.debug("[%s]%s=%s", method, n, v);
            }
            System.setProperty(n, v);
        }
    }

    public Properties getConfigurationPropertiesFromFiles() {
        if (SOSCollection.isEmpty(getArguments().getConfigurationFiles().getValue())) {
            return null;
        }
        String method = "getConfigurationPropertiesFromFiles";
        logger.info("%s[%s][files]", getLogPrefix(), method, SOSString.join(getArguments().getConfigurationFiles().getValue(), ",", f -> f
                .toString()));
        Properties p = new Properties();
        for (Path file : getArguments().getConfigurationFiles().getValue()) {
            if (Files.exists(file) && Files.isRegularFile(file)) {
                try (BufferedReader reader = Files.newBufferedReader(file)) {
                    p.load(reader);
                    logger.info("[%s][%s]loaded", method, file);
                } catch (Throwable e) {
                    logger.warn("[%s][%s][failed]%s", method, file, e.toString());
                }
            } else {
                logger.warn("[%s][%s]does not exist or is not a regular file", method, file);
            }
        }

        if (logger.isDebugEnabled()) {
            for (String n : p.stringPropertyNames()) {
                String v = p.getProperty(n);
                logger.debug("[%s]%s=%s", method, n, v);
            }
        }
        return p;
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

    /** Provider (non-YADE) method */
    public List<ProviderFile> selectFiles(String directory) throws SOSProviderException {
        return selectFiles(new ProviderFileSelection(new ProviderFileSelectionConfig.Builder().directory(directory).build()));
    }

    public String getLogPrefix() {
        if (logPrefix == null) {
            logPrefix = context == null ? "" : context.getLogPrefix();
        }
        return logPrefix;
    }

    public String getPathOperationPrefix(String path) {
        return getLogPrefix() + "[" + path + "]";
    }

    /** Refresh file size and lastModified<br/>
     * Note: fileAfterReread is not returned because there is other information (e.g. status) in the file
     * 
     * @param file
     * @param fileAfterReread
     * @return */
    public ProviderFile refreshFileMetadata(ProviderFile file, ProviderFile fileAfterReread) {
        if (fileAfterReread == null || file == null) {
            return null;
        }
        file.setSize(fileAfterReread.getSize());
        file.setLastModifiedMillis(fileAfterReread.getLastModifiedMillis());
        return file;
    }

    public void checkParam(String method, String paramValue, String msg) throws SOSProviderException {
        if (SOSString.isEmpty(paramValue)) {
            throw new SOSProviderException(getLogPrefix() + "[" + method + "]" + msg + " missing");
        }
    }

    public void checkModificationTime(String path, long milliseconds) throws SOSProviderException {
        if (!isValidModificationTime(milliseconds)) {
            throw new SOSProviderException(getLogPrefix() + "[" + path + "][" + milliseconds + "]not valid modification time");
        }
    }

    public String getConnectMsg() {
        return String.format("%s[connect]%s ...", getLogPrefix(), accessInfo);
    }

    public String getConnectedMsg() {
        return getConnectedMsg(null);
    }

    public String getConnectedMsg(String additionalInfos) {
        String add = SOSString.isEmpty(additionalInfos) ? accessInfo : "[" + accessInfo + "]" + additionalInfos;
        return String.format("%s[connected]%s", getLogPrefix(), add);
    }

    public String getDisconnectedMsg() {
        return String.format("%s[disconnected]%s", getLogPrefix(), accessInfo);
    }

    public Function<ProviderFileBuilder, ProviderFile> getProviderFileCreator() {
        return providerFileCreator;
    }

    public ISOSLogger getLogger() {
        return logger;
    }

    public A getArguments() {
        return arguments;
    }

    public String getAccessInfo() {
        return accessInfo;
    }

    public void setAccessInfo(String val) {
        accessInfo = val;
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

    public static boolean isValidFileSize(long fileSize) {
        return fileSize >= 0;
    }

    public static boolean isValidModificationTime(long milliseconds) {
        return milliseconds > 0;
    }

    public void logNotImpementedMethod(String methodName, String add) {
        logger.info("%s[%s][%s][not implemented yet]%s", getLogPrefix(), getClass().getSimpleName(), methodName, add);
    }

}
