package com.sos.commons.vfs.http;

import java.io.BufferedReader;
import java.io.FilterInputStream;
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
import java.util.function.Supplier;

import com.sos.commons.exception.SOSNoSuchFileException;
import com.sos.commons.exception.SOSRequiredArgumentMissingException;
import com.sos.commons.httpclient.BaseHttpClient;
import com.sos.commons.httpclient.BaseHttpClient.Builder;
import com.sos.commons.httpclient.commons.HttpExecutionResult;
import com.sos.commons.httpclient.commons.HttpOutputStream;
import com.sos.commons.httpclient.commons.auth.HttpClientAuthConfig;
import com.sos.commons.httpclient.commons.auth.HttpClientAuthMethod;
import com.sos.commons.util.SOSClassUtil;
import com.sos.commons.util.SOSPathUtils;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.http.HttpUtils;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.util.proxy.ProxyConfig;
import com.sos.commons.util.proxy.ProxyConfigArguments;
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
 * - How to implement not chunked transfer?<br/>
 * -- set Content-Lenght throws the java.lang.IllegalArgumentException: restricted header name: "Content-Length" Exception...<br/>
 * - When using an HTTP Proxy, the exception "[411]Length Required" is thrown....<br/>
 * -- [411]Length Required - Server rejected the request because the Content-Length header field is not defined and the server requires<br/>
 * 
 * @implNote HTTPProvider class must avoid throwing custom or new IOException instances, since IOException is reserved for signaling underlying connection or
 *           transport errors */
public class HTTPProvider extends AProvider<HTTPProviderArguments, Object> {

    private final Object clientLock = new Object();
    private volatile BaseHttpClient client;
    private volatile boolean connected = false;
    private volatile boolean connectivityFaultSimulationActive;

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

        synchronized (clientLock) {
            try {
                Builder builder = BaseHttpClient.withBuilder();
                builder = builder.withLogger(getLogger());
                builder = builder.withConnectTimeout(Duration.ofSeconds(getArguments().getConnectTimeoutAsSeconds()));
                builder = builder.withDefaultHeaders(getArguments().getHttpHeaders().getValue());
                builder = builder.withAuth(getAuthConfig());
                builder = builder.withProxyConfig(getProxyConfig());
                if (isSecureConnectionEnabled()) {
                    builder = builder.withSSL(getArguments().getSsl());
                    logIfHostnameVerificationDisabled(getArguments().getSsl());
                }
                client = builder.build();

                getLogger().info(getConnectMsg());
                HttpExecutionResult<Void> result = connect(client, getArguments().getBaseURI());
                connected = true;

                getLogger().info(getConnectedMsg(client.getServerInfo(result.response())));
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
        synchronized (clientLock) {
            connected = false;
            try {
                ProxyConfigArguments args = new ProxyConfigArguments();
                args.getHost().setValue("yade-connectivity-fault-proxy");
                client = BaseHttpClient.withBuilder().withLogger(getLogger()).withProxyConfig(ProxyConfig.createInstance(args)).build();
                getLogger().info(getInjectConnectivityFaultMsg());
            } catch (Exception e) {
                getLogger().info(getInjectConnectivityFaultMsg(e));
            }
        }
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
        validateArgument("exists", path, "path");

        URI uri = null;
        try {
            uri = new URI(normalizePath(path));

            BaseHttpClient client = requireHTTPClient();
            HttpExecutionResult<Void> result = client.executeHEADOrGETNoResponseBody(uri);
            int code = result.response().statusCode();
            if (!HttpUtils.isSuccessful(code)) {
                if (HttpUtils.isNotFound(code)) {
                    return false;
                }
                throw new Exception(BaseHttpClient.formatExecutionResult(result));
            }

            return true;
        } catch (IOException e) {
            throwProviderConnectException(path, uri, e);
            return false;
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path, uri), e);
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
        validateArgument("deleteIfExists", path, "path");

        URI uri = null;
        try {
            uri = new URI(normalizePath(path));

            BaseHttpClient client = requireHTTPClient();
            HttpExecutionResult<Void> result = client.executeDELETENoResponseBody(uri);
            int code = result.response().statusCode();
            if (!HttpUtils.isSuccessful(code)) {
                if (HttpUtils.isNotFound(code)) {
                    return false;
                }
                throw new Exception(BaseHttpClient.formatExecutionResult(result));
            }

            return true;
        } catch (IOException e) {
            throwProviderConnectException(path, uri, e);
            return false;
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path, uri), e);
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
            deleteIfExists(target);

            URI sourceURI = new URI(normalizePath(source));
            URI targetURI = new URI(normalizePath(target));

            HttpExecutionResult<Void> result;
            int code;
            long sourceSize = -1L;
            BaseHttpClient client = requireHTTPClient();
            if (!client.isChunkedTransfer()) {
                result = client.executeHEADOrGETNoResponseBody(sourceURI);
                code = result.response().statusCode();
                if (!HttpUtils.isSuccessful(code)) {
                    return false;
                }
                sourceSize = client.getFileSize(result.response());
            }
            is = client.getHTTPInputStream(sourceURI);

            result = client.executePUTNoResponseBody(targetURI, is, sourceSize, false);
            code = result.response().statusCode();
            if (!HttpUtils.isSuccessful(code)) {
                if (HttpUtils.isNotFound(code)) {
                    return false;
                }
                throw new Exception(BaseHttpClient.formatExecutionResult(result));
            }

            deleteIfExists(source);
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

        URI uri = null;
        try {
            uri = new URI(normalizePath(path));

            BaseHttpClient client = requireHTTPClient();
            HttpExecutionResult<Void> result = client.executeGETNoResponseBody(uri);
            int code = result.response().statusCode();
            if (!HttpUtils.isSuccessful(code)) {
                if (HttpUtils.isNotFound(code)) {
                    return null;
                }
                throw new Exception(BaseHttpClient.formatExecutionResult(result));
            }

            return createProviderFile(client, uri, result.response());
        } catch (IOException e) {
            throwProviderConnectException(path, uri, e);
            return null;
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path, uri), e);
        }

    }

    /** Overrides {@link IProvider#getFileContentIfExists(String, String)} <br/>
     * 
     * @apiNote this method is implemented in the same way as webdav {@link WebDAVProvider#getFileContentIfExists(String)}.<br/>
     */
    @Override
    public String getFileContentIfExists(String path) throws ProviderException {
        validateArgument("getFileContentIfExists", path, "path");

        StringBuilder content = new StringBuilder();
        BaseHttpClient client = requireHTTPClient();
        try (InputStream is = client.getHTTPInputStream(new URI(normalizePath(path))); Reader r = new InputStreamReader(is, StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(r)) {
            br.lines().forEach(content::append);
            return content.toString();
        } catch (SOSNoSuchFileException e) {
            return null;
        } catch (IOException e) {
            throwProviderConnectException(path, e);
            return null;
        } catch (Exception e) {
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
        validateArgument("getInputStream", path, "path");

        URI uri = null;
        try {
            uri = new URI(normalizePath(path));

            BaseHttpClient client = requireHTTPClient();
            InputStream is = client.getHTTPInputStream(uri);
            if (is == null) {
                throw new Exception("InputStream is null");
            }
            return is;
        } catch (IOException e) {
            throwProviderConnectException(path, uri, e);
            return null;
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path, uri), e);
        }
    }

    /** Overrides {@link IProvider#supportsReadOffset()} */
    public boolean supportsReadOffset() {
        return false;
    }

    /** Overrides {@link IProvider#getInputStream(String, long)}<br/>
     * 
     * @apiNote this method is implemented in the same way as webdav {@link WebDAVProvider#getInputStream(String, long)}.<br/>
     */
    @Override
    public InputStream getInputStream(String path, long offset) throws ProviderException {
        validateArgument("getInputStream", path, "path");

        URI uri = null;
        try {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("%s[getInputStream][supportsReadOffset=%s, offset=%s]%s", getLogPrefix(), supportsReadOffset(), offset, path);
            }

            uri = new URI(normalizePath(path));

            BaseHttpClient client = requireHTTPClient();
            InputStream is = client.getHTTPInputStream(uri, offset);
            if (is == null) {
                throw new Exception("InputStream is null");
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

            return is;
        } catch (IOException e) {
            throwProviderConnectException(path, uri, e);
            return null;
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path, uri), e);
        }
    }

    /** Overrides {@link IProvider#getOutputStream(String, boolean)}<br/>
     * 
     * @apiNote YADE - not used - see {@link #upload(String, InputStream, long, boolean)} methods<br/>
     *          HttpOutputStream is avoided since the current implementation buffers the entire content in memory and performs the PUT operation only upon
     *          closing the stream. This makes it impractical for large files due to excessive memory consumption.
     * @apiNote this method is implemented in the same way as webdav {@link WebDAVProvider#getOutputStream(String,boolean)}.<br/>
     */
    @Override
    public OutputStream getOutputStream(String path, boolean append) throws ProviderException {
        validateArgument("getOutputStream", path, "path");

        URI uri = null;
        try {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("%s[getOutputStream][append=%s]%s", getLogPrefix(), append, path);
            }

            uri = new URI(normalizePath(path));

            return new HttpOutputStream(requireHTTPClient(), uri, false);
        } catch (IOException e) {
            throwProviderConnectException(path, uri, e);
            return null;
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path, uri), e);
        }
    }

    public void upload(String path, Supplier<InputStream> supplier, long sourceSize) throws ProviderException {
        upload(path, supplier, sourceSize, false);
    }

    /** Uploads a file via HTTP PUT.
     * <p>
     * Note: Do <b>not</b> attempt to check the file size immediately after the upload using an extra request (e.g., HEAD or GET).<br />
     * Doing so is unreliable for several reasons:
     * <ul>
     * <li>The HTTP server may respond with a status like 204 No Content, which does not provide the file size.</li>
     * <li>For servers handling many small files in parallel, the file may not be fully committed to storage at the instant the client performs the check.</li>
     * <li>Such checks can introduce race conditions or unnecessary delays without improving correctness.</li>
     * </ul>
     * Instead, rely on the HTTP response code of the PUT request:
     * <ul>
     * <li>Any 2xx response indicates the file has been successfully received by the server.</li>
     * <li>No further size verification is needed in typical scenarios.</li>
     * </ul>
     * <p>
     * The input stream supplied to the PUT request should be fresh for each attempt (for example, via a {@link java.util.function.Supplier}), which allows
     * retries without reusing a consumed stream. */
    public void upload(String path, Supplier<InputStream> sourceSupplier, long sourceSize, boolean isWebDAV) throws ProviderException {
        validateArgument("upload", path, "path");
        validateArgument("upload", sourceSupplier, "Supplier InputStream source");

        URI uri = null;
        try {
            uri = new URI(normalizePath(path));
            // PUT file - PUT not returns file size
            BaseHttpClient client = requireHTTPClient();
            HttpExecutionResult<Void> result = client.executePUTNoResponseBody(uri, sourceSupplier, sourceSize, isWebDAV);
            int code = result.response().statusCode();
            if (!HttpUtils.isSuccessful(code)) {
                throw new Exception(BaseHttpClient.formatExecutionResult(result));
            }
        } catch (IOException e) {
            throwProviderConnectException(path, uri, e);
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path, uri), e);
        }
    }

    public void uploadContent(String path, String content, boolean isWebDAV) throws ProviderException {
        validateArgument("uploadContent", path, "path");

        URI uri = null;
        try {
            uri = new URI(normalizePath(path));

            BaseHttpClient client = requireHTTPClient();
            HttpExecutionResult<Void> result = client.executePUTNoResponseBody(uri, content, isWebDAV);
            int code = result.response().statusCode();
            if (!HttpUtils.isSuccessful(code)) {
                throw new Exception(BaseHttpClient.formatExecutionResult(result));
            }
        } catch (IOException e) {
            throwProviderConnectException(path, uri, e);
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path, uri), e);
        }
    }

    public BaseHttpClient requireHTTPClient() throws ProviderException {
        synchronized (clientLock) {
            if (client == null) {
                // 0 - getStackTrace
                // 1 - requireClient
                // 2 - caller
                throw new ProviderClientNotInitializedException(getLogPrefix(), BaseHttpClient.class, SOSClassUtil.getMethodName(2));
            }
            return client;
        }
    }

    public boolean isSecureConnectionEnabled() {
        return Protocol.HTTPS.equals(getArguments().getProtocol().getValue()) || Protocol.WEBDAVS.equals(getArguments().getProtocol().getValue());
    }

    public void throwProviderConnectException(String path, IOException e) throws ProviderException {
        throwProviderConnectException(path, null, e);
    }

    public void throwProviderConnectException(String path, URI uri, IOException e) throws ProviderException {
        connected = false;
        throw new ProviderConnectException(getPathOperationPrefix(path, uri), e);
    }

    public String getPathOperationPrefix(String path, URI uri) {
        return super.getPathOperationPrefix(uri == null ? path : uri.toString());
    }

    private HttpExecutionResult<Void> connect(BaseHttpClient client, URI uri) throws Exception {
        String notFoundMsg = null;

        HttpExecutionResult<Void> result = client.executeHEADOrGETNoResponseBody(uri);
        int code = result.response().statusCode();
        if (HttpUtils.isServerError(code)) {
            throw new Exception(BaseHttpClient.formatExecutionResult(result));
        }
        if (HttpUtils.isNotFound(code)) {
            notFoundMsg = BaseHttpClient.formatExecutionResult(result);
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
            connect(client, getArguments().getBaseURI());
        }
        return result;
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
            return true;
        }
    }

    private ProviderFile createProviderFile(BaseHttpClient client, URI uri, HttpResponse<?> response) throws Exception {
        long size = client.getFileSize(response);
        if (size < 0) {
            return null;
        }
        return createProviderFile(uri.toString(), size, client.getLastModifiedInMillis(response));
    }

    /** JS7 new - auth_method - not in the XML schema - currently only NONE,BASIC supported */
    private HttpClientAuthConfig getAuthConfig() {
        if (getArguments().getUser().isEmpty()) {
            getArguments().getAuthMethod().setValue(HttpClientAuthMethod.NONE);
            return null;
        }
        // BASIC
        return new HttpClientAuthConfig(getArguments().getUser().getValue(), getArguments().getPassword().getValue());
        // switch (getArguments().getAuthMethod().getValue()) {
        // case BASIC:
        // return new HttpClientAuthConfig(getArguments().getUser().getValue(), getArguments().getPassword().getValue());
        // case NTLM: // TODO NTLM
        // case NONE:
        // default:
        // return null;
        // }
    }

}
