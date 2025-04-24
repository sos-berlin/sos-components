package com.sos.commons.vfs.commons;

import java.io.BufferedReader;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;

import com.sos.commons.util.SOSClassUtil;
import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSPathUtils;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.arguments.impl.SSLArguments;
import com.sos.commons.util.beans.SOSCommandResult;
import com.sos.commons.util.beans.SOSEnv;
import com.sos.commons.util.beans.SOSTimeout;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.commons.vfs.commons.file.ProviderFileBuilder;
import com.sos.commons.vfs.commons.file.selection.ProviderFileSelection;
import com.sos.commons.vfs.commons.file.selection.ProviderFileSelectionConfig;
import com.sos.commons.vfs.commons.proxy.ProxyProvider;
import com.sos.commons.vfs.exceptions.ProviderConnectException;
import com.sos.commons.vfs.exceptions.ProviderException;
import com.sos.commons.vfs.exceptions.ProviderInitializationException;

public abstract class AProvider<A extends AProviderArguments> implements IProvider {

    public static long DEFAULT_FILE_ATTR_VALUE = -1L;

    private final ISOSLogger logger;
    private final A arguments;
    private final ProxyProvider proxyProvider;

    /** Default providerFileCreator function creates a standard ProviderFile using the builder */
    private Function<ProviderFileBuilder, ProviderFile> providerFileCreator = builder -> builder.build(this);
    /** Source/Target type - logging - is not set if only one provider is used (e.g. SSH JITL Job) */
    private AProviderContext context;

    private AProviderReusableResource<?> reusableResource;

    /** For Connect/Disconnect logging e.g. LocalProvider=null, SSH/FTP Provider=user@server:port */
    private String accessInfo;
    private String label;

    public AProvider(ISOSLogger logger, A arguments, SOSArgument<?>... additionalCredentialStoreArg) throws ProviderInitializationException {
        this.logger = logger;
        this.arguments = arguments;
        // before proxyProvider
        resolveCredentialStore(additionalCredentialStoreArg);
        this.proxyProvider = this.arguments == null ? null : ProxyProvider.createInstance(this.arguments.getProxy());
    }

    /** Validates that all required global properties (e.g., client, session) are properly initialized and not null.<br/>
     * If any of the required properties are not set, an exception will be thrown.
     * 
     * @throws ProviderException if any required precondition is not met (e.g., uninitialized client or session). */
    public abstract void validatePrerequisites(String method) throws ProviderException;

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
    public void ensureConnected() throws ProviderConnectException {
        if (!isConnected()) {
            disconnect();
            connect();
        }
    }

    /** Overrides {@link IProvider#createDirectoriesIfNotExists(Collection)} */
    @Override
    public boolean createDirectoriesIfNotExists(Collection<String> paths) throws ProviderException {
        if (SOSCollection.isEmpty(paths)) {
            return false;
        }
        validatePrerequisites("createDirectoriesIfNotExists");

        boolean result = false;
        for (String path : paths) {
            if (createDirectoriesIfNotExists(path)) {
                result = true;
            }
        }
        return result;
    }

    /** Overrides {@link IProvider#rereadFileIfExists(ProviderFile)} */
    @Override
    public ProviderFile rereadFileIfExists(ProviderFile file) throws ProviderException {
        validatePrerequisites("rereadFileIfExists");

        try {
            return refreshFileMetadata(file, getFileIfExists(file.getFullPath()));
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(file.getFullPath()), e);
        }
    }

    /** Overrides {@link IProvider#toPathStyle(String)} */
    @Override
    public String toPathStyle(String path) {
        return SOSPathUtils.isUnixStylePathSeparator(getPathSeparator()) ? SOSPathUtils.toUnixStyle(path) : SOSPathUtils.toWindowsStyle(path);
    }

    /** Overrides {@link IProvider#onInputStreamClosed(String)} */
    @Override
    public void onInputStreamClosed(String path) throws ProviderException {

    }

    /** Overrides {@link IProvider#onOutputStreamClosed(String)} */
    @Override
    public void onOutputStreamClosed(String path) throws ProviderException {

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

    // cancelCommands - other thread
    /** Overrides {@link IProvider#cancelCommands()} */
    @Override
    public SOSCommandResult cancelCommands() {
        return null;
    }

    public void enableReusableResource() {

    }

    public void enableReusableResource(AProviderReusableResource<?> resource) throws Exception {
        reusableResource = resource;
    }

    public AProviderReusableResource<?> getReusableResource() {
        return reusableResource;
    }

    public void disableReusableResource() {
        if (reusableResource != null) {
            SOSClassUtil.closeQuietly(reusableResource);
            reusableResource = null;
        }
    }

    public Properties getConfigurationPropertiesFromFiles() {
        if (SOSCollection.isEmpty(getArguments().getConfigurationFiles().getValue())) {
            return null;
        }
        String method = "getConfigurationPropertiesFromFiles";
        if (logger.isDebugEnabled()) {
            logger.debug("[%s][%s]%s", getLabel(), method, SOSString.join(getArguments().getConfigurationFiles().getValue(), ",", f -> f.toString()));
        }
        Properties p = new Properties();
        for (Path file : getArguments().getConfigurationFiles().getValue()) {
            if (Files.exists(file) && Files.isRegularFile(file)) {
                try (BufferedReader reader = Files.newBufferedReader(file)) {
                    p.load(reader);
                    logger.info("[%s][%s][%s]loaded", getLabel(), method, file);
                } catch (Throwable e) {
                    logger.warn("[%s][%s][%s][failed]%s", getLabel(), method, file, e.toString());
                }
            } else {
                logger.warn("[%s][%s][%s]does not exist or is not a regular file", getLabel(), method, file);
            }
        }

        if (logger.isDebugEnabled()) {
            for (String n : p.stringPropertyNames()) {
                String v = p.getProperty(n);
                logger.debug("[%s][%s]%s=%s", getLabel(), method, n, v);
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
    public List<ProviderFile> selectFiles(String directory) throws ProviderException {
        return selectFiles(new ProviderFileSelection(new ProviderFileSelectionConfig.Builder().directory(directory).build()));
    }

    public String getLabel() {
        if (label == null) {
            label = context == null ? "" : context.getLabel();
        }
        return label;
    }

    public String getPathOperationPrefix(String path) {
        return getLogPrefix() + "[" + path + "]";
    }

    public String getLogPrefix() {
        return SOSString.isEmpty(getLabel()) ? "" : "[" + getLabel() + "]";
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

    public void validateArgument(String method, String argValue, String msg) throws ProviderException {
        if (SOSString.isEmpty(argValue)) {
            throw new ProviderException(getLogPrefix() + "[" + method + "]" + msg + " missing");
        }
    }

    public void validateArgument(String method, InputStream argValue, String msg) throws ProviderException {
        if (argValue == null) {
            throw new ProviderException(getLogPrefix() + "[" + method + "]" + msg + " missing");
        }
    }

    public void validateModificationTime(String path, long milliseconds) throws ProviderException {
        if (!isValidModificationTime(milliseconds)) {
            throw new ProviderException(getLogPrefix() + "[" + path + "][" + milliseconds + "]not valid modification time");
        }
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

    public ProxyProvider getProxyProvider() {
        return proxyProvider;
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

    public String getAccessInfo() {
        return accessInfo;
    }

    public void setAccessInfo(String val) {
        if (proxyProvider == null) {
            accessInfo = val;
        } else {
            accessInfo = "[proxy " + proxyProvider.getAccessInfo() + "]" + val;
        }
    }

    public String getDirectoryNotFoundMsg(String directory) {
        return getLogPrefix() + "[Directory]" + directory;
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

    public void logIfHostnameVerificationDisabled(SSLArguments args) {
        if (!args.getVerifyCertificateHostname().isTrue()) {
            logger.info("*********************** Security warning *********************************************************************");
            logger.info("YADE option \"%s\" is currently \"false\" for %s connections. ", args.getVerifyCertificateHostname().getName(),
                    getArguments().getProtocol().getValue());
            logger.info("The certificate verification process will not verify the DNS name of the certificate presented by the server,");
            logger.info(" with the hostname of the server used by the YADE client connection.");
            logger.info("**************************************************************************************************************");
        }
    }

    public void logNotImpementedMethod(String methodName, String add) {
        logger.info("[%s][%s][%s][not implemented yet]%s", getLabel(), getClass().getSimpleName(), methodName, add);
    }

    /** Called when credential store are successfully resolved.
     * 
     * This method can be overridden by subclasses to perform additional actions after credential store have been resolved. */
    public void onCredentialStoreResolved() throws Exception {

    }

    private void resolveCredentialStore(SOSArgument<?>... additionalCredentialStoreArg) throws ProviderInitializationException {
        if (arguments == null) {
            return;
        }

        try {
            if (ProviderCredentialStoreResolver.resolve(arguments, arguments.getProxy(), additionalCredentialStoreArg)) {
                onCredentialStoreResolved();
            }
        } catch (Throwable e) {
            throw new ProviderInitializationException(e);
        }
    }

}
