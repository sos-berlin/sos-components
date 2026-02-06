package com.sos.commons.vfs.azure;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import com.sos.commons.exception.SOSNoSuchFileException;
import com.sos.commons.exception.SOSRequiredArgumentMissingException;
import com.sos.commons.httpclient.azure.AzureBlobStorageClient;
import com.sos.commons.httpclient.azure.AzureBlobStorageClient.Builder;
import com.sos.commons.httpclient.azure.commons.AzureBlobStorageOutputStream;
import com.sos.commons.httpclient.azure.commons.auth.AAzureStorageAuthProvider;
import com.sos.commons.httpclient.azure.commons.auth.blob.AzureBlobPublicAuthProvider;
import com.sos.commons.httpclient.azure.commons.auth.blob.AzureBlobSASAuthProvider;
import com.sos.commons.httpclient.azure.commons.auth.blob.AzureBlobSharedKeyAuthProvider;
import com.sos.commons.httpclient.commons.ABaseHttpClient;
import com.sos.commons.httpclient.commons.HttpExecutionResult;
import com.sos.commons.util.SOSClassUtil;
import com.sos.commons.util.SOSPathUtils;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.http.HttpUtils;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.util.proxy.ProxyConfig;
import com.sos.commons.util.proxy.ProxyConfigArguments;
import com.sos.commons.vfs.azure.commons.AzureBlobStorageProviderArguments;
import com.sos.commons.vfs.azure.commons.AzureBlobStorageProviderUtils;
import com.sos.commons.vfs.azure.commons.AzureBlobStorageResource;
import com.sos.commons.vfs.commons.AProvider;
import com.sos.commons.vfs.commons.IProvider;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.commons.vfs.commons.file.selection.ProviderFileSelection;
import com.sos.commons.vfs.exceptions.ProviderClientNotInitializedException;
import com.sos.commons.vfs.exceptions.ProviderConnectException;
import com.sos.commons.vfs.exceptions.ProviderException;
import com.sos.commons.vfs.exceptions.ProviderInitializationException;
import com.sos.commons.vfs.webdav.WebDAVProvider;

/** TODO:<br />
 * - upload "large" files<br />
 * -- The current implementation uses a byte array instead of an input stream because the HttpClein SDK internally sets a chunked transfer header when using the
 * --- input stream, and this header is not accepted by Azure. The upload fails with an unsupported header exception.<br/>
 * --- This means that the entire file content is in memory<br/>
 * - "Public" authentication:<br />
 * -- downloads with a FilePath selection (the full path is known) work.<br/>
 * -- using other selections fails with a 404 (Resource Not Found) error in the current test environment. The use of list blobs, etc., may not be allowed<br/>
 * 
 * @implNote AzureBlobStorageProvider class must avoid throwing custom or new IOException instances, since IOException is reserved for signaling underlying
 *           connection or transport errors */
public class AzureBlobStorageProvider extends AProvider<AzureBlobStorageProviderArguments, Object> {

    private final Object clientLock = new Object();
    private volatile AzureBlobStorageClient client;
    private volatile boolean connected = false;
    private volatile boolean connectivityFaultSimulationActive;

    private String containerName;

    public AzureBlobStorageProvider(ISOSLogger logger, AzureBlobStorageProviderArguments args) throws ProviderInitializationException {
        super(logger, args, args == null ? null : args.getAccountKey(), args == null ? null : args.getSASToken());
        try {
            getArguments().getServiceEndpoint().setValue(getArguments().getHost().getValue());
            setAccessInfo(getArguments().getAccessInfo());
        } catch (Exception e) {
            throw new ProviderInitializationException(e);
        }
    }

    /** Overrides {@link IProvider#getPathSeparator()} */
    @Override
    public String getPathSeparator() {
        return SOSPathUtils.PATH_SEPARATOR_UNIX;
    }

    /** Overrides {@link IProvider#isAbsolutePath(String)} */
    @Override
    public boolean isAbsolutePath(String path) {
        return SOSPathUtils.isAbsoluteURIPath(path);
    }

    /** Overrides {@link IProvider#normalizePath(String)} */
    @Override
    public String normalizePath(String path) {
        if (SOSString.isEmpty(path)) {
            return path;
        }
        String containerName = getContainerName(path);
        String blobPath = getBlobPath(path, containerName);
        String p = containerName;
        if (!SOSString.isEmpty(blobPath)) {
            p = p + getPathSeparator() + blobPath;
        }
        return p;
    }

    /** Overrides {@link IProvider#connect()} */
    @Override
    public void connect() throws ProviderConnectException {
        if (SOSString.isEmpty(getArguments().getServiceEndpoint().getValue())) {
            throw new ProviderConnectException(new SOSRequiredArgumentMissingException("ServiceEndpoint"));
        }

        synchronized (clientLock) {
            try {
                Builder builder = AzureBlobStorageClient.withBuilder();
                builder = builder.withLogger(getLogger());
                builder = builder.withConnectTimeout(Duration.ofSeconds(getArguments().getConnectTimeoutAsSeconds()));
                builder = builder.withDefaultHeaders(getArguments().getHttpHeaders().getValue());
                builder = builder.withProxyConfig(getProxyConfig());
                builder = builder.withSSL(getArguments().getSsl());

                builder = builder.withServiceEndpoint(getArguments().getServiceEndpoint().getValue());

                String accountName = getArguments().getUser().getValue();
                String accountKey = getArguments().getAccountKey().getValue();
                String apiVersion = getArguments().getApiVersion().getValue();
                switch (getArguments().getAuthMethod().getValue()) {
                case SAS_TOKEN:
                    builder = builder.withAuthProvider(new AzureBlobSASAuthProvider(getLogger(), accountName, accountKey, apiVersion, getArguments()
                            .getSASToken().getValue()));
                    break;
                case SHARED_KEY:
                    builder = builder.withAuthProvider(new AzureBlobSharedKeyAuthProvider(getLogger(), accountName, accountKey, apiVersion));
                    break;
                case PUBLIC:
                default:
                    builder = builder.withAuthProvider(new AzureBlobPublicAuthProvider(getLogger(), accountName, accountKey, apiVersion));
                    break;
                }
                logIfHostnameVerificationDisabled(getArguments().getSsl());

                client = builder.build();

                getLogger().info(getConnectMsg());
                // containing the XML response with details of all available container(s)
                HttpExecutionResult<String> result = client.executeGETStorage();
                result.formatWithResponseBody(true);
                int code = result.response().statusCode();
                if (!HttpUtils.isSuccessful(code)) {
                    if (!HttpUtils.isForbidden(code)) {// see client.executeGETStorage description Note
                        if (HttpUtils.isNotFound(code) && client.getAuthProvider().isPublic()) {

                        } else {
                            throw new Exception(client.formatExecutionResultForException(result));
                        }
                    }
                }
                connected = true;

                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("%s[connected][container(s)]%s", getLogPrefix(), AzureBlobStorageClient.formatExecutionResult(result));
                }
                getLogger().info(getConnectedMsg(client.getServerInfo(result.response())));
                debugServiceProperties();
            } catch (Exception e) {
                connected = false;

                // Do not call disconnect() here. it sets the client to null and may cause a ProviderClientNotInitializedException instead of a real connection
                // error in methods executed after connect() - e.g. if retry, roll back...
                // Call disconnect() in the application's finally block.

                // disconnectInternal();
                throw new ProviderConnectException(String.format("[%s]", getAccessInfo()), e);
            }
        }
    }

    /** Overrides {@link IProvider#isConnected()} */
    @Override
    public boolean isConnected() {
        synchronized (clientLock) {
            return connected && client != null;
        }
    }

    /** Overrides {@link IProvider#disconnect()} */
    @Override
    public void disconnect() {
        if (disconnectInternal()) {
            getLogger().info(getDisconnectedMsg());
        }
    }

    /** Overrides {@link IProvider#injectConnectivityFault()} */
    @Override
    public void injectConnectivityFault() {
        connectivityFaultSimulationActive = true;
        connected = false;
        synchronized (clientLock) {
            try {
                String prefix = "yade-connectivity-fault-";
                ProxyConfigArguments args = new ProxyConfigArguments();
                args.getHost().setValue(prefix + "proxy");

                AAzureStorageAuthProvider p = new AzureBlobPublicAuthProvider(getLogger(), prefix + "user", Base64.getEncoder().encodeToString((prefix
                        + "account-key").getBytes()), "123");

                client = AzureBlobStorageClient.withBuilder().withLogger(getLogger()).withProxyConfig(ProxyConfig.createInstance(args))
                        .withServiceEndpoint("http://" + prefix + "service-endpoint").withAuthProvider(p).build();

                getLogger().info(getInjectConnectivityFaultMsg());
            } catch (Exception e) {
                getLogger().info(getInjectConnectivityFaultMsg(e));
                e.printStackTrace();
            }
        }
    }

    /** Overrides {@link IProvider#selectFiles(ProviderFileSelection)} */
    @Override
    public List<ProviderFile> selectFiles(ProviderFileSelection selection) throws ProviderException {
        selection = ProviderFileSelection.createIfNull(selection);
        String directory = selection.getConfig().getDirectory() == null ? "" : selection.getConfig().getDirectory();
        try {
            String containerName = getContainerName(directory);
            String blobPath = getBlobFilePath(directory, containerName);

            List<ProviderFile> result = new ArrayList<>();
            AzureBlobStorageProviderUtils.selectFiles(this, selection, containerName, blobPath, result);
            return result;
        } catch (IOException e) {
            throwProviderConnectException(directory, e);
            return null;
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(directory), e);
        }
    }

    /** Overrides {@link IProvider#exists(String)} */
    @Override
    public boolean exists(String path) throws ProviderException {
        validateArgument("exists", path, "path");

        try {
            String containerName = getContainerName(path);
            String blobPath = getBlobFilePath(path, containerName);

            AzureBlobStorageClient client = requireAzureClient();
            HttpExecutionResult<Void> result = client.executeHEADBlob(containerName, blobPath);
            result.formatWithResponseBody(true);
            int code = result.response().statusCode();
            if (!HttpUtils.isSuccessful(code)) {
                if (HttpUtils.isNotFound(code)) {
                    return false;
                }
                throw new Exception(client.formatExecutionResultForException(result));
            }
            return true;
        } catch (IOException e) {
            throwProviderConnectException(path, e);
            return false;
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#createDirectoriesIfNotExists(String)} */
    @Override
    public boolean createDirectoriesIfNotExists(String path) throws ProviderException {
        return true;
    }

    /** Overrides {@link IProvider#deleteIfExists(String)} */
    @Override
    public boolean deleteIfExists(String path) throws ProviderException {
        validateArgument("deleteIfExists", path, "path");

        try {
            String containerName = getContainerName(path);
            String blobPath = getBlobFilePath(path, containerName);

            return deleteIfExists(path, containerName, blobPath);
        } catch (IOException e) {
            throwProviderConnectException(path, e);
            return false;
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#deleteFileIfExists(String)} */
    @Override
    public boolean deleteFileIfExists(String path) throws ProviderException {
        return deleteIfExists(path);
    }

    /** Overrides {@link IProvider#moveFileIfExists(String, String)}<br/>
     * PUT,DELETE implementation<br />
     * - alternative - MOVE (may not be supported by the server…)
     * 
     * Note: Testing with Tomcat 10.1.33(Windows): - client.executePUT(targetURI, is) fails with 500 Server error<br/>
     * -- Cause: java.lang.ClassNotFoundException: org.apache.tomcat.util.http.parser.HttpHeaderParser$HeaderParseStatus<br/>
     * --- Due to chunked transfer of the java net HttpClient... and the Tomcat ClassLoader strategy<br/>
     * --- This class is located in a tomcat-coyote.jar<br/>
     * --- The tomcat-coyote.jar file is enabled in the $CATALINA_HOME/lib/tomcat-coyote.jar directory<br/>
     * -- Solution 1 (recommended) - $CATALINA_BASE/conf/catalina.properties, set<br/>
     * --- shared.loader=$CATALINA_HOME/lib/tomcat-coyote.jar<br/>
     * -- Solution 2 - copy $CATALINA_HOME/lib/tomcat-coyote.jar jar to the WEB-INF/lib location of the respective application<br/>
     * 
     * @param source
     * @param target
     * @return
     * @throws ProviderException */
    @Override
    public boolean moveFileIfExists(String source, String target) throws ProviderException {
        validateArgument("moveFileIfExists", source, "source");
        validateArgument("moveFileIfExists", target, "target");

        InputStream is = null;
        try {
            String sourceContainerName = getContainerName(source);
            String sourceBlobPath = getBlobFilePath(source, sourceContainerName);

            String targetContainerName = sourceContainerName;
            String targetBlobPath = getBlobFilePath(target, targetContainerName);

            // 1) check if source exists
            AzureBlobStorageClient client = requireAzureClient();
            HttpExecutionResult<Void> resultSource = client.executeHEADBlob(sourceContainerName, sourceBlobPath);
            resultSource.formatWithResponseBody(true);
            int code = resultSource.response().statusCode();
            if (!HttpUtils.isSuccessful(code)) {
                throw new Exception(client.formatExecutionResultForException(resultSource));
            }
            is = getInputStream(source, sourceContainerName, sourceBlobPath, 0L);

            // 2) delete Target if exists
            deleteIfExists(target, targetContainerName, targetBlobPath);
            // 3) create Target
            @SuppressWarnings("unused")
            Long targetFileSize = upload(is, targetContainerName, targetBlobPath);

            // 4) delete Source
            deleteIfExists(source, sourceContainerName, sourceBlobPath);
            return true;
        } catch (SOSNoSuchFileException e) { // is = getInputStream(s);
            return false;
        } catch (IOException e) {
            throwProviderConnectException(source + "->" + target, e);
            return false;
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(source + "->" + target), e);
        } finally {
            SOSClassUtil.closeQuietly(is);
        }
    }

    /** Overrides {@link IProvider#getFileIfExists(String)} */
    @Override
    public ProviderFile getFileIfExists(String path) throws ProviderException {
        validateArgument("getFileIfExists", path, "path");

        try {
            String containerName = getContainerName(path);
            String blobPath = getBlobFilePath(path, containerName);

            return createProviderFile(AzureBlobStorageProviderUtils.getResource(this, containerName, blobPath, false, false));
        } catch (IOException e) {
            throwProviderConnectException(path, e);
            return null;
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#getFileContentIfExists(String, String)} <br/>
     * 
     * @apiNote this method is implemented in the same way as webdav {@link WebDAVProvider#getFileContentIfExists(String)}.<br/>
     */
    @Override
    public String getFileContentIfExists(String path) throws ProviderException {
        validateArgument("getFileContentIfExists", path, "path");

        try {
            String containerName = getContainerName(path);
            String blobPath = getBlobFilePath(path, containerName);

            AzureBlobStorageClient client = requireAzureClient();
            HttpExecutionResult<String> result = client.executeGETBlobContent(containerName, blobPath);
            result.formatWithResponseBody(true);
            int code = result.response().statusCode();
            if (!HttpUtils.isSuccessful(code)) {
                if (HttpUtils.isNotFound(code)) {
                    return null;
                }
                throw new Exception(client.formatExecutionResultForException(result));
            }
            return result.response().body();
        } catch (IOException e) {
            throwProviderConnectException(path, e);
            return null;
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#writeFile(String, String)}<br/>
     */
    @Override
    public void writeFile(String path, String content) throws ProviderException {
        validateArgument("uploadContent", path, "path");

        try {
            String containerName = getContainerName(path);
            String blobPath = getBlobFilePath(path, containerName);

            AzureBlobStorageClient client = requireAzureClient();
            HttpExecutionResult<String> result = client.executePUTBlob(containerName, blobPath, content.getBytes(StandardCharsets.UTF_8),
                    HttpUtils.HEADER_CONTENT_TYPE_BINARY);
            result.formatWithResponseBody(true);
            int code = result.response().statusCode();
            if (!HttpUtils.isSuccessful(code)) {
                throw new Exception(client.formatExecutionResultForException(result));
            }
        } catch (IOException e) {
            throwProviderConnectException(path, e);
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#setFileLastModifiedFromMillis(String, long)} */
    @Override
    public void setFileLastModifiedFromMillis(String path, long milliseconds) throws ProviderException {
        logNotImpementedMethod("setFileLastModifiedFromMillis", "path=" + path + ",milliseconds=" + milliseconds);
    }

    /** Overrides {@link IProvider#supportsReadOffset()} */
    public boolean supportsReadOffset() {
        return false;
    }

    /** Overrides {@link IProvider#getInputStream(String)}<br/>
     */
    @Override
    public InputStream getInputStream(String path) throws ProviderException {
        return getInputStream(path, 0L);
    }

    /** Overrides {@link IProvider#getInputStream(String, long)}<br/>
     */
    @Override
    public InputStream getInputStream(String path, long offset) throws ProviderException {
        validateArgument("getInputStream", path, "path");

        try {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("%s[getInputStream][supportsReadOffset=%s, offset=%s]%s", getLogPrefix(), supportsReadOffset(), offset, path);
            }

            String containerName = getContainerName(path);
            String blobPath = getBlobFilePath(path, containerName);

            return getInputStream(path, containerName, blobPath, offset);
        } catch (IOException e) {
            throwProviderConnectException(path, e);
            return null;
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#getOutputStream(String, boolean)}<br/>
     * 
     * @apiNote YADE - not used - see {@link #upload(InputStream, String, String)} methods */
    @Override
    public OutputStream getOutputStream(String path, boolean append) throws ProviderException {
        validateArgument("getOutputStream", path, "path");

        try {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("%s[getOutputStream][append=%s]%s", getLogPrefix(), append, path);
            }

            String containerName = getContainerName(path);
            String blobPath = getBlobFilePath(path, containerName);

            return new AzureBlobStorageOutputStream(requireAzureClient(), containerName, blobPath);
        } catch (IOException e) {
            throwProviderConnectException(path, e);
            return null;
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    // TODO - currently limitation - InputStream converted to ByteArray ...
    public long upload(String path, InputStream source, long sourceSize) throws ProviderException {
        validateArgument("upload", path, "path");
        validateArgument("upload", source, "InputStream source");

        try {
            String containerName = getContainerName(path);
            String blobPath = getBlobFilePath(path, containerName);

            return upload(source, containerName, blobPath);
        } catch (IOException e) {
            throwProviderConnectException(path, e);
            return -1L;
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    public AzureBlobStorageClient requireAzureClient() throws ProviderException {
        synchronized (clientLock) {
            if (client == null) {
                throw new ProviderClientNotInitializedException(getLogPrefix(), AzureBlobStorageClient.class, SOSClassUtil.getMethodName());
            }
            return client;
        }
    }

    public ProviderFile createProviderFile(AzureBlobStorageResource resource) throws Exception {
        if (resource == null) {
            return null;
        }
        // TODO chunked?
        if (resource.getSize() < 0) {
            return null;
        }
        return createProviderFile(resource.getFullPath(), resource.getSize(), resource.getLastModifiedInMillis());
    }

    // without logging
    private boolean disconnectInternal() {
        synchronized (clientLock) {
            if (client == null) {
                connected = false;
                return false;
            }

            SOSClassUtil.closeQuietly(client);
            client = null;
            connected = false;
        }
        return true;
    }

    private void debugServiceProperties() {
        if (!getLogger().isDebugEnabled()) {
            return;
        }
        try {
            HttpExecutionResult<String> result = requireAzureClient().executeGETStorageServicePropertiers();
            result.formatWithResponseBody(true);
            getLogger().debug("%s[connected][service properties]%s", getLogPrefix(), AzureBlobStorageClient.formatExecutionResult(result));
        } catch (Exception e) {
            getLogger().debug("%s[connected][debugServiceProperties]%s", getLogPrefix(), e.toString(), e);
        }
    }

    private String getContainerName(String path) {
        if (containerName == null) {
            if (!getArguments().getContainerName().isEmpty()) {
                containerName = getArguments().getContainerName().getValue();
            } else if (SOSString.isEmpty(path)) {
                containerName = "";
            } else {
                // Remove leading backslashes or slashes, if they exist and split in 2 parts
                String[] pathParts = path.replaceAll("^[/\\\\]+", "").split("[/\\\\]", 2);
                // shareName = pathParts.length > 1 ? pathParts[0] : "";
                containerName = pathParts[0];
            }
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("%s[containerName]%s", getLogPrefix(), containerName);
            }
        }
        return containerName;
    }

    private String getBlobFilePath(String path, String containerName) {
        String blobPath = getBlobPath(path, containerName);
        if (SOSString.isEmpty(blobPath)) {
            return blobPath;
        }
        return SOSString.trimEnd(blobPath, getPathSeparator());
    }

    /** Returns normalized path without containerName
     * 
     * @param path
     * @return */
    private String getBlobPath(String path, String containerName) {
        String blobPath = toPathStyle(path);
        if (SOSString.isEmpty(blobPath)) {
            return "";
        }
        blobPath = SOSString.trimStart(blobPath, getPathSeparator());
        if (!SOSString.isEmpty(containerName)) {
            if (containerName.equalsIgnoreCase(blobPath)) {
                return "";
            }
            // finds the container name in the path and removes it
            int containerIndex = blobPath.indexOf(containerName + getPathSeparator());
            if (containerIndex != -1) {
                blobPath = blobPath.substring(containerIndex + containerName.length() + 1); // +1 for pathSeparator
            }
        }
        return blobPath;
    }

    private boolean deleteIfExists(String path, String containerName, String blobPath) throws Exception {
        AzureBlobStorageClient client = requireAzureClient();
        HttpExecutionResult<String> result = client.executeDELETEBlob(containerName, blobPath);
        result.formatWithResponseBody(true);
        int code = result.response().statusCode();
        if (!HttpUtils.isSuccessful(code)) {
            if (HttpUtils.isNotFound(code)) {
                return false;
            }
            throw new Exception(client.formatExecutionResultForException(result));
        }
        return true;
    }

    /** @TODO user Header etc see TODO {@link ABaseHttpClient#getHTTPInputStream(java.net.URI, long)} */
    private InputStream getInputStream(String path, String containerName, String blobPath, long offset) throws Exception {
        AzureBlobStorageClient client = requireAzureClient();
        HttpExecutionResult<InputStream> result = client.executeGETBlobInputStream(containerName, blobPath);
        result.formatWithResponseBody(true);
        int code = result.response().statusCode();
        if (!HttpUtils.isSuccessful(code)) {
            if (HttpUtils.isNotFound(code)) {
                throw new SOSNoSuchFileException(path, new Exception(client.formatExecutionResultForException(result)));
            }
            throw new Exception(client.formatExecutionResultForException(result));
        }
        InputStream is = result.response().body();
        if (is == null) {
            throw new Exception("response body InputStream is null");
        }

        if (getArguments().isConnectivityFaultSimulationEnabled()) {
            is = new FilterInputStream(is) {

                @Override
                public int read(byte b[]) throws IOException {
                    if (connectivityFaultSimulationActive) {
                        if (!isConnected()) {
                            connectivityFaultSimulationActive = false;
                            throw new IOException("Not connected");
                        }
                    }
                    return super.read(b);
                }
            };
        }

        if (offset > 0) {
            SOSClassUtil.skipFully(is, offset);
        }

        return is;
    }

    private long upload(InputStream source, String targetContainerName, String targetBlobPath) throws Exception {
        // 1) PUT file
        AzureBlobStorageClient client = requireAzureClient();
        HttpExecutionResult<String> result = client.executePUTBlob(targetContainerName, targetBlobPath, source, HttpUtils.HEADER_CONTENT_TYPE_BINARY);
        result.formatWithResponseBody(true);
        int code = result.response().statusCode();
        if (!HttpUtils.isSuccessful(code)) {
            throw new Exception(client.formatExecutionResultForException(result));
        }
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("%s[upload]%s", getLogPrefix(), AzureBlobStorageClient.formatExecutionResult(result));
        }
        // 2) get uploaded file size (PUT not returns file size)
        client = requireAzureClient();// refresh client state - maybe disconnected
        HttpExecutionResult<Void> resultExists = client.executeHEADBlob(targetContainerName, targetBlobPath);
        code = resultExists.response().statusCode();
        if (!HttpUtils.isSuccessful(code)) {
            throw new Exception(client.formatExecutionResultForException(resultExists));
        }
        return client.getFileSize(resultExists.response());
    }

    private void throwProviderConnectException(String path, IOException e) throws ProviderException {
        connected = false;
        throw new ProviderConnectException(getPathOperationPrefix(path), e);
    }

}
