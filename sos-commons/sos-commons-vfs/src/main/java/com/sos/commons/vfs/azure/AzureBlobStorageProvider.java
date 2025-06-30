package com.sos.commons.vfs.azure;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.sos.commons.exception.SOSNoSuchFileException;
import com.sos.commons.exception.SOSRequiredArgumentMissingException;
import com.sos.commons.httpclient.azure.AzureBlobStorageClient;
import com.sos.commons.httpclient.azure.AzureBlobStorageClient.Builder;
import com.sos.commons.httpclient.azure.commons.AzureBlobStorageOutputStream;
import com.sos.commons.httpclient.azure.commons.auth.blob.AzureBlobPublicAuthProvider;
import com.sos.commons.httpclient.azure.commons.auth.blob.AzureBlobSASAuthProvider;
import com.sos.commons.httpclient.azure.commons.auth.blob.AzureBlobSharedKeyAuthProvider;
import com.sos.commons.httpclient.commons.HttpExecutionResult;
import com.sos.commons.util.SOSClassUtil;
import com.sos.commons.util.SOSPathUtils;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.http.HttpUtils;
import com.sos.commons.util.loggers.base.ISOSLogger;
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
 */
public class AzureBlobStorageProvider extends AProvider<AzureBlobStorageProviderArguments> {

    private AzureBlobStorageClient client;
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
        return toPathStyle(path);
    }

    /** Overrides {@link IProvider#connect()} */
    @Override
    public void connect() throws ProviderConnectException {
        if (SOSString.isEmpty(getArguments().getServiceEndpoint().getValue())) {
            throw new ProviderConnectException(new SOSRequiredArgumentMissingException("ServiceEndpoint"));
        }

        try {
            Builder builder = AzureBlobStorageClient.withBuilder();
            builder = builder.withLogger(getLogger());
            builder = builder.withConnectTimeout(Duration.ofSeconds(getArguments().getConnectTimeoutAsSeconds()));
            builder = builder.withHeaders(getArguments().getHttpHeaders().getValue());
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
            HttpExecutionResult<String> result = client.executeGETStorage();
            result.formatWithResponseBody(true);
            int code = result.response().statusCode();
            if (!HttpUtils.isSuccessful(code)) {
                if (!HttpUtils.isForbidden(code)) {// e.g. SAS token: 'srt=co' instead of 'srt=sco' to check service

                    if (HttpUtils.isNotFound(code) && client.getAuthProvider().isPublic()) {

                    } else {
                        throw new IOException(client.formatExecutionResultForException(result));
                    }
                }
            }
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("%s[connected]%s", getLogPrefix(), AzureBlobStorageClient.formatExecutionResult(result));
            }

            getLogger().info(getConnectedMsg());
        } catch (Throwable e) {
            disconnect();
            throw new ProviderConnectException(String.format("[%s]", getAccessInfo()), e);
        }
    }

    /** Overrides {@link IProvider#isConnected()} */
    @Override
    public boolean isConnected() {
        return client != null;
    }

    /** Overrides {@link IProvider#disconnect()} */
    @Override
    public void disconnect() {
        if (client == null) {
            return;
        }

        SOSClassUtil.closeQuietly(client);
        client = null;

        getLogger().info(getDisconnectedMsg());
    }

    /** Overrides {@link IProvider#selectFiles(ProviderFileSelection)} */
    @Override
    public List<ProviderFile> selectFiles(ProviderFileSelection selection) throws ProviderException {
        validatePrerequisites("selectFiles");

        selection = ProviderFileSelection.createIfNull(selection);
        String directory = selection.getConfig().getDirectory() == null ? "" : selection.getConfig().getDirectory();
        try {
            String containerName = getContainerName(directory);
            String blobPath = getBlobFilePath(directory);

            List<ProviderFile> result = new ArrayList<>();
            AzureBlobStorageProviderUtils.selectFiles(this, selection, containerName, blobPath, result);
            return result;
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(directory), e);
        }
    }

    /** Overrides {@link IProvider#exists(String)} */
    @Override
    public boolean exists(String path) throws ProviderException {
        validatePrerequisites("exists", path, "path");

        try {
            String containerName = getContainerName(path);
            String blobPath = getBlobFilePath(path);

            HttpExecutionResult<Void> result = client.executeHEADBlob(containerName, blobPath);
            result.formatWithResponseBody(true);
            int code = result.response().statusCode();
            if (!HttpUtils.isSuccessful(code)) {
                if (HttpUtils.isNotFound(code)) {
                    return false;
                }
                throw new IOException(client.formatExecutionResultForException(result));
            }
            return true;
        } catch (Throwable e) {
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
        validatePrerequisites("deleteIfExists", path, "path");

        try {
            String containerName = getContainerName(path);
            String blobPath = getBlobFilePath(path);

            return deleteIfExists(path, containerName, blobPath);
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#deleteFileIfExists(String)} */
    @Override
    public boolean deleteFileIfExists(String path) throws ProviderException {
        return deleteIfExists(path);
    }

    /** Overrides {@link IProvider#renameFileIfSourceExists(String, String)}<br/>
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
    public boolean renameFileIfSourceExists(String source, String target) throws ProviderException {
        validatePrerequisites("renameFileIfSourceExists", source, "source");
        validateArgument("renameFileIfSourceExists", target, "target");

        InputStream is = null;
        try {
            String sourceContainerName = getContainerName(source);
            String sourceBlobPath = getBlobFilePath(source);

            String targetContainerName = sourceContainerName;
            String targetBlobPath = getBlobFilePath(target);

            // 1) check if source exists
            HttpExecutionResult<Void> resultSource = client.executeHEADBlob(sourceContainerName, sourceBlobPath);
            resultSource.formatWithResponseBody(true);
            int code = resultSource.response().statusCode();
            if (!HttpUtils.isSuccessful(code)) {
                throw new IOException(client.formatExecutionResultForException(resultSource));
            }
            long sourceSize = client.getFileSize(resultSource.response());
            is = getInputStream(source, sourceContainerName, sourceBlobPath);

            // 2) delete Target if exists
            deleteIfExists(target, targetContainerName, targetBlobPath);
            // 3) create Target
            @SuppressWarnings("unused")
            Long targetFileSize = upload(sourceBlobPath, targetContainerName, targetBlobPath, is, sourceSize);

            // 4) delete Source
            deleteIfExists(source, sourceContainerName, sourceBlobPath);
            return true;
        } catch (SOSNoSuchFileException e) { // is = getInputStream(s);
            return false;
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(source + "->" + target), e);
        } finally {
            SOSClassUtil.closeQuietly(is);
        }
    }

    /** Overrides {@link IProvider#getFileIfExists(String)} */
    @Override
    public ProviderFile getFileIfExists(String path) throws ProviderException {
        validatePrerequisites("getFileIfExists", path, "path");

        try {
            String containerName = getContainerName(path);
            String blobPath = getBlobFilePath(path);

            return createProviderFile(AzureBlobStorageProviderUtils.getResource(this, containerName, blobPath, false, false));
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#getFileContentIfExists(String, String)} <br/>
     * 
     * @apiNote this method is implemented in the same way as webdav {@link WebDAVProvider#getFileContentIfExists(String)}.<br/>
     */
    @Override
    public String getFileContentIfExists(String path) throws ProviderException {
        validatePrerequisites("getFileContentIfExists", path, "path");

        try {
            String containerName = getContainerName(path);
            String blobPath = getBlobFilePath(path);

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
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#writeFile(String, String)}<br/>
     */
    @Override
    public void writeFile(String path, String content) throws ProviderException {
        validatePrerequisites("uploadContent", path, "path");

        try {
            String containerName = getContainerName(path);
            String blobPath = getBlobFilePath(path);

            HttpExecutionResult<String> result = client.executePUTBlob(containerName, blobPath, content.getBytes(StandardCharsets.UTF_8),
                    HttpUtils.HEADER_CONTENT_TYPE_BINARY);
            result.formatWithResponseBody(true);
            int code = result.response().statusCode();
            if (!HttpUtils.isSuccessful(code)) {
                throw new Exception(client.formatExecutionResultForException(result));
            }
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#setFileLastModifiedFromMillis(String, long)} */
    @Override
    public void setFileLastModifiedFromMillis(String path, long milliseconds) throws ProviderException {
        logNotImpementedMethod("setFileLastModifiedFromMillis", "path=" + path + ",milliseconds=" + milliseconds);
    }

    /** Overrides {@link IProvider#getInputStream(String)}<br/>
     * 
     * @apiNote this method is implemented in the same way as webdav {@link WebDAVProvider#getInputStream(String)}.<br/>
     */
    @Override
    public InputStream getInputStream(String path) throws ProviderException {
        validatePrerequisites("getInputStream", path, "path");

        try {
            String containerName = getContainerName(path);
            String blobPath = getBlobFilePath(path);

            return getInputStream(path, containerName, blobPath);
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#getOutputStream(String, boolean)}<br/>
     * 
     * @apiNote this method is implemented in the same way as webdav {@link WebDAVProvider#getOutputStream(String,boolean)}.<br/>
     */
    @Override
    public OutputStream getOutputStream(String path, boolean append) throws ProviderException {
        validatePrerequisites("getOutputStream", path, "path");

        try {
            String containerName = getContainerName(path);
            String blobPath = getBlobFilePath(path);

            return new AzureBlobStorageOutputStream(client, containerName, blobPath);
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    // TOTO - currently limitation - InputStream converted to ByteArray ...
    public long upload(String path, InputStream source, long sourceSize) throws ProviderException {
        validatePrerequisites("upload", path, "path");
        validateArgument("upload", source, "InputStream source");

        try {
            String containerName = getContainerName(path);
            String blobPath = getBlobFilePath(path);

            return upload(path, containerName, blobPath, source, sourceSize);
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link AProvider#validatePrerequisites(String)} */
    @Override
    public void validatePrerequisites(String method) throws ProviderException {
        if (client == null) {
            throw new ProviderClientNotInitializedException(getLogPrefix() + method + "HTTPPClient");
        }
    }

    public void validatePrerequisites(String method, String argValue, String msg) throws ProviderException {
        validatePrerequisites(method);
        validateArgument(method, argValue, msg);
    }

    public AzureBlobStorageClient getClient() {
        return client;
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

    private String getBlobFilePath(String path) {
        String blobPath = getBlobPath(path);
        if (SOSString.isEmpty(blobPath)) {
            return blobPath;
        }
        return SOSString.trimEnd(blobPath, getPathSeparator());
    }

    /** Returns normalized path without containerName
     * 
     * @param path
     * @return */
    private String getBlobPath(String path) {
        String blobPath = normalizePath(path);
        if (SOSString.isEmpty(blobPath)) {
            return "";
        }
        String containerName = getContainerName(path);
        blobPath = SOSString.trimStart(blobPath, getPathSeparator());
        if (!SOSString.isEmpty(containerName)) {
            if (containerName.equalsIgnoreCase(blobPath)) {
                return "";
            }
            // finds the share name in the path and removes it
            int shareIndex = blobPath.indexOf(containerName + getPathSeparator());
            if (shareIndex != -1) {
                blobPath = blobPath.substring(shareIndex + containerName.length() + 1); // +1 for pathSeparator
            }
        }
        return blobPath;
    }

    private boolean deleteIfExists(String path, String containerName, String blobPath) throws Exception {
        HttpExecutionResult<String> result = client.executeDELETEBlob(containerName, blobPath);
        result.formatWithResponseBody(true);
        int code = result.response().statusCode();
        if (!HttpUtils.isSuccessful(code)) {
            if (HttpUtils.isNotFound(code)) {
                return false;
            }
            throw new IOException(client.formatExecutionResultForException(result));
        }
        return true;
    }

    private InputStream getInputStream(String path, String containerName, String blobPath) throws Exception {
        HttpExecutionResult<InputStream> result = client.executeGETBlobInputStream(containerName, blobPath);
        result.formatWithResponseBody(true);
        int code = result.response().statusCode();
        if (!HttpUtils.isSuccessful(code)) {
            if (HttpUtils.isNotFound(code)) {
                throw new SOSNoSuchFileException(path, new Exception(client.formatExecutionResultForException(result)));
            }
            throw new Exception(client.formatExecutionResultForException(result));
        }
        return result.response().body();
    }

    private long upload(String path, String targetContainerName, String targetBlobPath, InputStream source, long sourceSize) throws Exception {
        HttpExecutionResult<String> result = client.executePUTBlob(targetContainerName, targetBlobPath, source, HttpUtils.HEADER_CONTENT_TYPE_BINARY);
        result.formatWithResponseBody(true);
        int code = result.response().statusCode();
        if (!HttpUtils.isSuccessful(code)) {
            throw new Exception(client.formatExecutionResultForException(result));
        }
        // PUT not returns file size
        HttpExecutionResult<Void> resultExists = client.executeHEADBlob(targetContainerName, targetBlobPath);
        code = resultExists.response().statusCode();
        if (!HttpUtils.isSuccessful(code)) {
            throw new IOException(client.formatExecutionResultForException(resultExists));
        }
        return client.getFileSize(resultExists.response());
    }

}
