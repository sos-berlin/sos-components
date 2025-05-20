package com.sos.commons.vfs.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URI;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

import com.sos.commons.exception.SOSNoSuchFileException;
import com.sos.commons.exception.SOSRequiredArgumentMissingException;
import com.sos.commons.httpclient.BaseHttpClient;
import com.sos.commons.httpclient.BaseHttpClient.Builder;
import com.sos.commons.httpclient.BaseHttpClient.ExecuteResult;
import com.sos.commons.httpclient.commons.HttpOutputStream;
import com.sos.commons.httpclient.commons.auth.HttpClientAuthConfig;
import com.sos.commons.util.SOSClassUtil;
import com.sos.commons.util.SOSPathUtils;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.http.HttpUtils;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.commons.AProvider;
import com.sos.commons.vfs.commons.AProviderArguments.Protocol;
import com.sos.commons.vfs.commons.IProvider;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.commons.vfs.commons.file.selection.ProviderFileSelection;
import com.sos.commons.vfs.exceptions.ProviderClientNotInitializedException;
import com.sos.commons.vfs.exceptions.ProviderConnectException;
import com.sos.commons.vfs.exceptions.ProviderException;
import com.sos.commons.vfs.exceptions.ProviderInitializationException;
import com.sos.commons.vfs.http.commons.HTTPProviderArguments;
import com.sos.commons.vfs.webdav.WebDAVProvider;

/** TODO<br/>
 * - Parallelism<br/>
 * -- HTTPProvider is a Source - seems to work<br/>
 * -- HTTPProvider is a Target - doesn't work<br/>
 * - How to implement not chunked transfer?<br/>
 * -- set Content-Lenght throws the java.lang.IllegalArgumentException: restricted header name: "Content-Length" Exception...<br/>
 * - When using an HTTP Proxy, the exception "[411]Length Required" is thrown....<br/>
 * -- [411]Length Required - Server rejected the request because the Content-Length header field is not defined and the server requires<br/>
 */
public class HTTPProvider extends AProvider<HTTPProviderArguments> {

    private BaseHttpClient client;

    public HTTPProvider(ISOSLogger logger, HTTPProviderArguments args) throws ProviderInitializationException {
        super(logger, args);
        try {
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
        return HttpUtils.normalizePath(getArguments().getBaseURI(), path);
    }

    /** Overrides {@link IProvider#connect()} */
    @Override
    public void connect() throws ProviderConnectException {
        if (SOSString.isEmpty(getArguments().getHost().getValue())) {
            throw new ProviderConnectException(new SOSRequiredArgumentMissingException("host"));
        }

        try {
            getLogger().info(getConnectMsg());

            Builder builder = BaseHttpClient.withBuilder();
            builder = builder.withLogger(getLogger());
            builder = builder.withConnectTimeout(Duration.ofSeconds(getArguments().getConnectTimeoutAsSeconds()));
            builder = builder.withHeaders(getArguments().getHttpHeaders().getValue());
            builder = builder.withAuth(getAuthConfig());
            builder = builder.withProxyConfig(getProxyConfig());
            if (isSecureConnectionEnabled()) {
                builder = builder.withSSL(getArguments().getSsl());
                logIfHostnameVerificationDisabled(getArguments().getSsl());
            }
            client = builder.build();
            connect(getArguments().getBaseURI());

            getLogger().info(getConnectedMsg());
        } catch (Throwable e) {
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
        throw new ProviderException(getPathOperationPrefix(SOSString.toString(selection, Collections.singletonList("result"), true))
                + "not supported via " + getArguments().getProtocol().getValue());
    }

    /** Overrides {@link IProvider#exists(String)} */
    @Override
    public boolean exists(String path) throws ProviderException {
        validatePrerequisites("exists", path, "path");

        URI uri = null;
        try {
            uri = new URI(normalizePath(path));

            ExecuteResult<Void> result = client.executeHEADOrGET(uri);
            int code = result.response().statusCode();
            if (!HttpUtils.isSuccessful(code)) {
                if (HttpUtils.isNotFound(code)) {
                    return false;
                }
                throw new IOException(BaseHttpClient.getResponseStatus(result));
            }

            return true;
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(uri == null ? path : uri.toString()), e);
        }
    }

    /** Overrides {@link IProvider#createDirectoriesIfNotExists(String)} */
    @Override
    public boolean createDirectoriesIfNotExists(String path) throws ProviderException {
        if (exists(SOSPathUtils.getUnixStyleDirectoryWithTrailingSeparator(path))) {
            return false;
        }
        throw new ProviderException(getPathOperationPrefix(path) + "[does not exist]a directory cannot be created via " + getArguments().getProtocol()
                .getValue());
    }

    /** Overrides {@link IProvider#deleteIfExists(String)} */
    @Override
    public boolean deleteIfExists(String path) throws ProviderException {
        validatePrerequisites("deleteIfExists", path, "path");

        try {
            URI uri = new URI(normalizePath(path));

            ExecuteResult<Void> result = client.executeDELETE(uri);
            int code = result.response().statusCode();
            if (!HttpUtils.isSuccessful(code)) {
                if (HttpUtils.isNotFound(code)) {
                    return false;
                }
                throw new IOException(BaseHttpClient.getResponseStatus(result));
            }

            return true;
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
            deleteIfExists(target);

            URI sourceURI = new URI(normalizePath(source));
            URI targetURI = new URI(normalizePath(target));

            ExecuteResult<Void> result;
            int code;
            long sourceSize = -1L;
            if (!client.isChunkedTransfer()) {
                result = client.executeHEADOrGET(sourceURI);
                code = result.response().statusCode();
                if (!HttpUtils.isSuccessful(code)) {
                    return false;
                }
                sourceSize = client.getFileSize(result.response());
            }
            is = client.getHTTPInputStream(sourceURI);

            result = client.executePUT(targetURI, is, sourceSize, false);
            code = result.response().statusCode();
            if (!HttpUtils.isSuccessful(code)) {
                if (HttpUtils.isNotFound(code)) {
                    return false;
                }
                throw new IOException(BaseHttpClient.getResponseStatus(result));
            }

            deleteIfExists(source);
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
            URI uri = new URI(normalizePath(path));

            ExecuteResult<Void> result = client.executeGET(uri);
            int code = result.response().statusCode();
            if (!HttpUtils.isSuccessful(code)) {
                if (HttpUtils.isNotFound(code)) {
                    return null;
                }
                throw new IOException(BaseHttpClient.getResponseStatus(result));
            }

            return createProviderFile(uri, result.response());
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

        StringBuilder content = new StringBuilder();
        try (InputStream is = client.getHTTPInputStream(new URI(normalizePath(path))); Reader r = new InputStreamReader(is, StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(r)) {
            br.lines().forEach(content::append);
            return content.toString();
        } catch (SOSNoSuchFileException e) {
            return null;
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#writeFile(String, String)}<br/>
     * 
     * @apiNote this method is implemented in the same way as webdav {@link WebDAVProvider#writeFile(String,String)}.<br/>
     */
    @Override
    public void writeFile(String path, String content) throws ProviderException {
        uploadContent(path, content, false);
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
            return client.getHTTPInputStream(new URI(normalizePath(path)));
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
            return new HttpOutputStream(client, new URI(normalizePath(path)), false);
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    public long upload(String path, InputStream source, long sourceSize) throws ProviderException {
        return upload(path, source, sourceSize, false);
    }

    public long upload(String path, InputStream source, long sourceSize, boolean isWebDAV) throws ProviderException {
        validatePrerequisites("upload", path, "path");
        validateArgument("upload", source, "InputStream source");

        URI uri = null;
        try {
            uri = new URI(normalizePath(path));
            ExecuteResult<Void> result = client.executePUT(uri, source, sourceSize, isWebDAV);
            int code = result.response().statusCode();
            if (!HttpUtils.isSuccessful(code)) {
                throw new IOException(BaseHttpClient.getResponseStatus(result));
            }
            // PUT not returns file size
            result = client.executeHEADOrGET(uri);
            code = result.response().statusCode();
            if (!HttpUtils.isSuccessful(code)) {
                throw new IOException(BaseHttpClient.getResponseStatus(result));
            }
            return client.getFileSize(result.response());
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(uri == null ? path : uri.toString()), e);
        }
    }

    /** Overrides {@link AProvider#validatePrerequisites(String)} */
    @Override
    public void validatePrerequisites(String method) throws ProviderException {
        if (client == null) {
            throw new ProviderClientNotInitializedException(getLogPrefix() + method + "HTTPPClient");
        }
    }

    public void uploadContent(String path, String content, boolean isWebDAV) throws ProviderException {
        validatePrerequisites("uploadContent", path, "path");

        try {
            URI uri = new URI(normalizePath(path));

            ExecuteResult<Void> result = client.executePUT(uri, content, isWebDAV);
            int code = result.response().statusCode();
            if (!HttpUtils.isSuccessful(code)) {
                throw new IOException(BaseHttpClient.getResponseStatus(result));
            }
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    public BaseHttpClient getClient() {
        return client;
    }

    public void validatePrerequisites(String method, String argValue, String msg) throws ProviderException {
        validatePrerequisites(method);
        validateArgument(method, argValue, msg);
    }

    public boolean isSecureConnectionEnabled() {
        return Protocol.HTTPS.equals(getArguments().getProtocol().getValue()) || Protocol.WEBDAVS.equals(getArguments().getProtocol().getValue());
    }

    private void connect(URI uri) throws Exception {
        String notFoundMsg = null;

        ExecuteResult<Void> result = client.executeHEADOrGET(uri);
        int code = result.response().statusCode();
        if (HttpUtils.isServerError(code)) {
            throw new Exception(BaseHttpClient.getResponseStatus(result));
        }
        if (HttpUtils.isNotFound(code)) {
            notFoundMsg = BaseHttpClient.getResponseStatus(result);
        }

        // Connection successful but baseURI not found - try redefining baseURI
        // - recursive attempt with parent path
        if (notFoundMsg != null) {
            if (SOSString.isEmpty(uri.getPath())) {
                throw new Exception(notFoundMsg);
            }
            URI parentURI = HttpUtils.getParentURI(uri);
            if (parentURI == null || parentURI.equals(uri)) {
                throw new Exception(notFoundMsg);
            }
            // TODO info?
            getLogger().info("%s[connect][%s]using parent path %s ...", getLogPrefix(), notFoundMsg, parentURI);

            getArguments().setBaseURI(parentURI);
            setAccessInfo(getArguments().getAccessInfo());
            connect(getArguments().getBaseURI());
        }
    }

    private ProviderFile createProviderFile(URI uri, HttpResponse<?> response) throws Exception {
        long size = client.getFileSize(response);
        if (size < 0) {
            return null;
        }
        return createProviderFile(uri.toString(), size, BaseHttpClient.getLastModifiedInMillis(response));
    }

    private HttpClientAuthConfig getAuthConfig() {
        if (getArguments().getAuthMethod().getValue() == null) {
            return null;
        }
        switch (getArguments().getAuthMethod().getValue()) {
        case BASIC:
            return new HttpClientAuthConfig(getArguments().getUser().getValue(), getArguments().getPassword().getValue());
        case NTLM: // TODO NTLM
        case NONE:
        default:
            return null;
        }
    }

}
