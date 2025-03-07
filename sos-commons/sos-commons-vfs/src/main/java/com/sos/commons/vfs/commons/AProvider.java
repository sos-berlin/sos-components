package com.sos.commons.vfs.commons;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;

import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSPathUtil;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.SOSCommandResult;
import com.sos.commons.util.common.SOSEnv;
import com.sos.commons.util.common.SOSTimeout;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.commons.AProviderArguments.KeyStoreType;
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

    /** Checks the allowed file type - regular, symbolic link. If not set - all file types allowed */
    private final Function<Object, Boolean> fileTypeChecker;

    /** For Connect/Disconnect logging e.g. LocalProvider=null, SSH/FTP Provider=user@server:port */
    private String accessInfo;

    public AProvider(ISOSLogger logger, A arguments) throws SOSProviderInitializationException {
        this(logger, arguments, null);
    }

    public AProvider(ISOSLogger logger, A arguments, Function<Object, Boolean> fileTypeChecker) throws SOSProviderInitializationException {
        this.logger = logger;
        this.arguments = arguments;
        this.fileTypeChecker = fileTypeChecker;
    }

    /** Method to set a custom providerFileCreator (a function that generates ProviderFile using the builder)<br/>
     * Overrides {@link IProvider#setProviderFileCreator(Function)} */
    @Override
    public void setProviderFileCreator(Function<ProviderFileBuilder, ProviderFile> val) {
        providerFileCreator = val;
    }

    @Override
    public void setContext(AProviderContext val) {
        context = val;
    }

    @Override
    public AProviderContext getContext() {
        return context;
    }

    @Override
    public void ensureConnected() throws SOSProviderConnectException {
        if (!isConnected()) {
            connect();
        }
    }

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

    @Override
    public RenameFilesResult renameFileIfSourceExists(String sourcePath, String targetPath) throws SOSProviderException {
        checkParam("renameFileIfSourceExists", sourcePath, "sourcePath");
        checkParam("renameFileIfSourceExists", targetPath, "targetPath");

        return renameFilesIfSourceExists(Collections.singletonMap(sourcePath, targetPath), true);
    }

    @Override
    public String toPathStyle(String path) {
        return SOSPathUtil.isUnixStylePathSeparator(getPathSeparator()) ? SOSPathUtil.toUnixStyle(path) : SOSPathUtil.toWindowsStyle(path);
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

    public void setSystemProperties() {
        if (SOSCollection.isEmpty(getArguments().getSystemPropertyFiles().getValue())) {
            return;
        }
        String method = "setSystemProperties";
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
        return context == null ? "" : context.getLogPrefix();
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

    public boolean isValidFileType(Object fileRepresentator) {
        if (fileTypeChecker == null) {
            return true;
        }

        return fileTypeChecker.apply(fileRepresentator);
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
    
    public static KeyStore loadKeyStore(Path path, KeyStoreType type, String password) throws Exception {
        KeyStore ks = KeyStore.getInstance(type.name());
        char[] pass = password == null ? "".toCharArray() : password.toCharArray();
        try (InputStream is = Files.newInputStream(path)) {
            ks.load(is, pass);
        }
        return ks;
    }
}
