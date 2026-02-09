package com.sos.commons.vfs.commons;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSPathUtils;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.beans.SOSCommandResult;
import com.sos.commons.util.beans.SOSEnv;
import com.sos.commons.util.beans.SOSTimeout;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.util.proxy.ProxyConfig;
import com.sos.commons.util.ssl.SslArguments;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.commons.vfs.commons.file.ProviderFileBuilder;
import com.sos.commons.vfs.commons.file.selection.ProviderFileSelection;
import com.sos.commons.vfs.commons.file.selection.ProviderFileSelectionConfig;
import com.sos.commons.vfs.exceptions.ProviderConnectException;
import com.sos.commons.vfs.exceptions.ProviderException;
import com.sos.commons.vfs.exceptions.ProviderInitializationException;

public abstract class AProvider<A extends AProviderArguments, R> implements IProvider {

    public static long DEFAULT_FILE_ATTR_VALUE = -1L;

    private final ISOSLogger logger;
    private final A arguments;
    private final ProxyConfig proxyConfig;

    /** see {@link #ensureConnected()} */
    private final ReentrantLock reconnectLock = new ReentrantLock(true);
    private final AtomicBoolean reconnecting = new AtomicBoolean(false);

    /** Default providerFileCreator function creates a standard ProviderFile using the builder */
    private Function<ProviderFileBuilder, ProviderFile> providerFileCreator = builder -> builder.build(this);
    /** Source/Target type - logging - is not set if only one provider is used (e.g. SSH JITL Job) */
    private AProviderContext context;

    /** Pool managing reusable provider resources (e.g. SFTP, SMB handles), created lazily and shared across operations */
    private volatile ProviderReusableResourcePool<? extends AProviderReusableResource<R>, R> resourcePool;

    /** For Connect/Disconnect logging e.g. LocalProvider=null, SSH/FTP Provider=user@server:port */
    private String accessInfo;
    private String label;

    private boolean doneLogIfHostnameVerificationDisabled;

    public AProvider(ISOSLogger logger, A arguments, SOSArgument<?>... additionalCredentialStoreArg) throws ProviderInitializationException {
        this.logger = logger;
        this.arguments = arguments;
        // before proxyProvider
        resolveCredentialStore(additionalCredentialStoreArg);
        this.proxyConfig = this.arguments == null ? null : ProxyConfig.createInstance(this.arguments.getProxy());
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

    /** Overrides {@link IProvider#ensureConnected())}<br/>
     * /** Ensures that the provider is connected.
     * <p>
     * If the provider is already connected, this method returns immediately.<br/>
     * Otherwise, exactly one thread performs the reconnect while all other concurrent threads wait until the reconnect attempt completes.
     * <p>
     * Reconnection semantics:
     * <ul>
     * <li>If another thread is currently reconnecting, this method blocks until that reconnect attempt finishes.</li>
     * <li>If the provider is still not connected after the reconnect attempt completed in another thread, a {@link ProviderConnectException} is thrown.</li>
     * <li>If this thread performs the reconnect, it will disconnect any existing state and establish a new connection.</li>
     * </ul>
     * <p>
     * This method is thread-safe and guarantees that connect/disconnect operations are never executed concurrently.
     *
     * @throws ProviderConnectException if reconnecting fails or the provider remains disconnected after another thread finished reconnecting */
    @Override
    public void ensureConnected() throws ProviderConnectException {
        if (isConnected()) {
            return;
        }

        // Another thread is already reconnecting: wait
        if (!reconnecting.compareAndSet(false, true)) {
            waitForReconnect(); // maybe connected, maybe failed ...

            if (!isConnected()) {
                // ensureConnected(); ???
                // Reconnect failed in other thread
                throw new ProviderConnectException(String.format("%s[%s]Not connected", getLogPrefix(), getAccessInfo()));
            }
            return;
        }

        try {
            reconnectLock.lock();
            try {
                if (isConnected()) {
                    return;
                }
                disconnect();
                connect();
            } finally {
                reconnectLock.unlock();
            }
        } catch (ProviderConnectException e) {
            throw e;
        } catch (Exception e) {
            throw new ProviderConnectException(String.format("%s[%s]Reconnect failed", getLogPrefix(), getAccessInfo()), e);
        } finally {
            reconnecting.set(false);
        }
    }

    /** Overrides {@link IProvider#createDirectoriesIfNotExists(Collection)} */
    @Override
    public boolean createDirectoriesIfNotExists(Collection<String> paths) throws ProviderException {
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

    /** Overrides {@link IProvider#rereadFileIfExists(ProviderFile)} */
    @Override
    public ProviderFile rereadFileIfExists(ProviderFile file) throws ProviderException {
        try {
            return refreshFileMetadata(file, getFileIfExists(file.getFullPath()));
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(file.getFullPath()), e);
        }
    }

    /** Overrides {@link IProvider#toPathStyle(String)} */
    @Override
    public String toPathStyle(String path) {
        return SOSPathUtils.isUnixStylePathSeparator(getPathSeparator()) ? SOSPathUtils.toUnixStyle(path) : SOSPathUtils.toWindowsStyle(path);
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

    /** Cancels currently running provider commands.
     * <p>
     * Overrides {@link IProvider#cancelCommands()}
     * </p>
     * <p>
     * This method may be invoked from another thread and should attempt to interrupt or abort ongoing operations in a provider-specific manner.
     * </p>
     * 
     * @return the result of the cancel operation */
    @Override
    public SOSCommandResult cancelCommands() {
        return null;
    }

    /** Creates a new reusable resource pool for this provider.
     * <p>
     * Implementations must return a fully initialized, thread-safe pool managing provider-specific reusable resources.
     *
     * @return a new reusable resource pool
     * @throws ProviderException if the pool cannot be created */
    public ProviderReusableResourcePool<? extends AProviderReusableResource<R>, R> createResourcePool() throws ProviderException {
        return null;
    }

    /** Creates a new reusable resource pool for this provider using a path-specific configuration.
     * <p>
     * This variant allows providers to create path-dependent pools if required by the underlying protocol or implementation.
     *
     * @param path the path for which the resource pool is created
     * @return a new reusable resource pool
     * @throws ProviderException if the pool cannot be created */
    public ProviderReusableResourcePool<? extends AProviderReusableResource<R>, R> createResourcePool(String path) throws ProviderException {
        return null;
    }

    /** Returns the reusable resource pool for this provider, creating it lazily if necessary.
     * <p>
     * The pool is initialized in a thread-safe manner and reused for all subsequent operations.
     *
     * @return the reusable resource pool
     * @throws ProviderException if the pool cannot be created */
    public ProviderReusableResourcePool<? extends AProviderReusableResource<R>, R> requireResourcePool() throws ProviderException {

        ProviderReusableResourcePool<? extends AProviderReusableResource<R>, R> p = resourcePool;
        if (p == null) {
            synchronized (this) {
                p = resourcePool;
                if (p == null) {
                    p = createResourcePool();
                    resourcePool = p;
                }
            }
        }
        return p;
    }

    /** Returns the reusable resource pool for this provider, creating it lazily if necessary and using a path-specific configuration.
     * <p>
     * The pool is initialized in a thread-safe manner and reused for all subsequent operations.
     *
     * @param path the path used for pool creation
     * @return the reusable resource pool
     * @throws ProviderException if the pool cannot be created */
    public ProviderReusableResourcePool<? extends AProviderReusableResource<R>, R> requireResourcePool(String path) throws ProviderException {

        ProviderReusableResourcePool<? extends AProviderReusableResource<R>, R> p = resourcePool;
        if (p == null) {
            synchronized (this) {
                p = resourcePool;
                if (p == null) {
                    p = createResourcePool(path);
                    resourcePool = p;
                }
            }
        }
        return p;
    }

    /** Returns the currently initialized reusable resource pool.
     * <p>
     * This method does not trigger pool creation and may return {@code null} if the pool has not yet been initialized.
     *
     * @return the reusable resource pool or {@code null} if not initialized */
    public ProviderReusableResourcePool<? extends AProviderReusableResource<R>, R> getResourcePool() {
        return resourcePool;
    }

    /** Reduces the pool size to the minimal number of reusable resources (1) */
    public void reduceResourcePool() {
        if (getResourcePool() == null) {
            return;
        }
        resourcePool.reduce();
    }

    public void closeResourcePool() {
        if (getResourcePool() == null) {
            return;
        }
        resourcePool.closeAll();
        resourcePool = null;
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
                } catch (Exception e) {
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

    public void validateArgument(String method, Object argValue, String msg) throws ProviderException {
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

    public ProxyConfig getProxyConfig() {
        return proxyConfig;
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

    public String getInjectConnectivityFaultMsg() {
        return getInjectConnectivityFaultMsg(null);
    }

    public String getInjectConnectivityFaultMsg(Exception e) {
        if (e == null) {
            return getLogPrefix() + "[" + getClass().getSimpleName() + "]" + "[injectConnectivityFault]injected";
        }
        return getLogPrefix() + "[" + getClass().getSimpleName() + "]" + "[injectConnectivityFault]" + e;
    }

    public String getAccessInfo() {
        return accessInfo;
    }

    public void setAccessInfo(String val) {
        if (proxyConfig == null) {
            accessInfo = val;
        } else {
            accessInfo = "[proxy " + proxyConfig.getAccessInfo() + "]" + val;
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
        } catch (Exception e) {
            return String.valueOf(val).concat("ms");
        }
    }

    public static boolean isValidFileSize(long fileSize) {
        return fileSize >= 0;
    }

    public static boolean isValidModificationTime(long milliseconds) {
        return milliseconds > 0;
    }

    public void logIfHostnameVerificationDisabled(SslArguments args) {
        if (!args.getUntrustedSslVerifyCertificateHostname().isTrue()) {
            if (doneLogIfHostnameVerificationDisabled) {
                return;
            }
            doneLogIfHostnameVerificationDisabled = true;

            String name = args.getUntrustedSslVerifyCertificateHostname().getName();
            Boolean val = args.getUntrustedSslVerifyCertificateHostname().getValue();
            // e.g. YADE uses DisableCertificateHostnameVerification
            if (args.getUntrustedSslVerifyCertificateHostnameOppositeName() != null) {
                name = args.getUntrustedSslVerifyCertificateHostnameOppositeName();
                val = !val;
            }
            logger.info("*********************** Security warning *********************************************************************");
            logger.info("\"%s\" is currently \"%s\" for %s connections. ", name, val, getArguments().getProtocol().getValue());
            logger.info("The certificate verification process will not verify the DNS name of the certificate presented by the server,");
            logger.info("with the hostname of the server used by the client connection.");
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
        } catch (Exception e) {
            throw new ProviderInitializationException(e);
        }
    }

    private void waitForReconnect() throws ProviderConnectException {
        while (reconnecting.get()) {
            if (Thread.currentThread().isInterrupted()) {
                throw new ProviderConnectException("Interrupted while waiting for reconnect");
            }
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(10));
        }
    }

}
