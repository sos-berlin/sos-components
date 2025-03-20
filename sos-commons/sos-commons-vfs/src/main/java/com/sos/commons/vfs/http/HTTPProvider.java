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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.sos.commons.exception.SOSNoSuchFileException;
import com.sos.commons.exception.SOSRequiredArgumentMissingException;
import com.sos.commons.util.SOSClassUtil;
import com.sos.commons.util.SOSPathUtils;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.commons.AProvider;
import com.sos.commons.vfs.commons.IProvider;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.commons.vfs.commons.file.files.DeleteFilesResult;
import com.sos.commons.vfs.commons.file.files.RenameFilesResult;
import com.sos.commons.vfs.commons.file.selection.ProviderFileSelection;
import com.sos.commons.vfs.exceptions.ProviderClientNotInitializedException;
import com.sos.commons.vfs.exceptions.ProviderConnectException;
import com.sos.commons.vfs.exceptions.ProviderException;
import com.sos.commons.vfs.exceptions.ProviderInitializationException;
import com.sos.commons.vfs.http.commons.HTTPAuthConfig;
import com.sos.commons.vfs.http.commons.HTTPAuthMethod;
import com.sos.commons.vfs.http.commons.HTTPClient;
import com.sos.commons.vfs.http.commons.HTTPClient.ExecuteResult;
import com.sos.commons.vfs.http.commons.HTTPOutputStream;
import com.sos.commons.vfs.http.commons.HTTPProviderArguments;
import com.sos.commons.vfs.http.commons.HTTPUtils;
import com.sos.commons.vfs.webdav.WebDAVProvider;

/** TODO - parallelism<br/>
 * - HTTPProvider is a Source - seems to work<br/>
 * - HTTPProvider is a Target - doesn't work */
public class HTTPProvider extends AProvider<HTTPProviderArguments> {

    private HTTPClient client;
    private URI baseURI;

    public HTTPProvider(ISOSLogger logger, HTTPProviderArguments args) throws ProviderInitializationException {
        super(logger, args);
        try {
            // if baseURI not found, can be redefined when connecting
            setBaseURI(HTTPUtils.getBaseURI(getArguments().getHost(), getArguments().getPort()));
            setAccessInfo(HTTPUtils.getAccessInfo(getBaseURI(), getArguments().getUser().getValue()));
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
        return HTTPUtils.normalizePath(baseURI, path);
    }

    /** Overrides {@link IProvider#connect()} */
    @Override
    public void connect() throws ProviderConnectException {
        if (SOSString.isEmpty(getArguments().getHost().getValue())) {
            throw new ProviderConnectException(new SOSRequiredArgumentMissingException("host"));
        }

        try {
            getLogger().info(getConnectMsg());

            client = HTTPClient.createAuthenticatedClient(getLogger(), getBaseURI(), createAuthConfig(), getProxyProvider(), getArguments().getSSL(),
                    getArguments().getHTTPHeaders().getValue());
            connect(getBaseURI());

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
            if (!HTTPUtils.isSuccessful(code)) {
                if (HTTPUtils.isNotFound(code)) {
                    return false;
                }
                throw new IOException(HTTPClient.getResponseStatus(result));
            }

            return true;
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#createDirectoriesIfNotExists(String)} */
    @Override
    public boolean createDirectoriesIfNotExists(String path) throws ProviderException {
        if (exists(path)) {
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
            if (!HTTPUtils.isSuccessful(code)) {
                if (HTTPUtils.isNotFound(code)) {
                    return false;
                }
                throw new IOException(HTTPClient.getResponseStatus(result));
            }

            return true;
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#deleteFilesIfExists(Collection, boolean)} */
    // TODO check if isDirectory
    @Override
    public DeleteFilesResult deleteFilesIfExists(Collection<String> files, boolean stopOnSingleFileError) throws ProviderException {
        if (files == null) {
            return null;
        }
        validatePrerequisites("deleteFilesIfExists");

        DeleteFilesResult r = new DeleteFilesResult(files.size());
        try {
            l: for (String file : files) {
                try {
                    if (deleteIfExists(file)) {
                        r.addSuccess();
                    } else {
                        r.addNotFound(file);
                    }
                } catch (Throwable e) {
                    r.addError(file, e);
                    if (stopOnSingleFileError) {
                        break l;
                    }
                }
            }
        } catch (Throwable e) {
            new ProviderException(e);
        }
        return r;
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
                if (!HTTPUtils.isSuccessful(code)) {
                    return false;
                }
                sourceSize = getFileSize(sourceURI, result.response());
            }
            is = client.getHTTPInputStream(sourceURI);

            result = client.executePUT(targetURI, is, sourceSize, false);
            code = result.response().statusCode();
            if (!HTTPUtils.isSuccessful(code)) {
                if (HTTPUtils.isNotFound(code)) {
                    return false;
                }
                throw new IOException(HTTPClient.getResponseStatus(result));
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

    /** Overrides {@link IProvider#renameFilesIfSourceExists(Map, boolean)} */
    @Override
    public RenameFilesResult renameFilesIfSourceExists(Map<String, String> files, boolean stopOnSingleFileError) throws ProviderException {
        if (files == null) {
            return null;
        }
        validatePrerequisites("renameFilesIfSourceExists");

        RenameFilesResult r = new RenameFilesResult(files.size());
        try {
            l: for (Map.Entry<String, String> entry : files.entrySet()) {
                String source = entry.getKey();
                String target = entry.getValue();
                try {
                    if (renameFileIfSourceExists(source, target)) {
                        r.addSuccess();
                    } else {
                        r.addNotFound(source);
                    }
                } catch (Throwable e) {
                    r.addError(source, e);
                    if (stopOnSingleFileError) {
                        break l;
                    }
                }
            }
        } catch (Throwable e) {
            new ProviderException(e);
        }
        return r;
    }

    /** Overrides {@link IProvider#getFileIfExists(String)} */
    @Override
    public ProviderFile getFileIfExists(String path) throws ProviderException {
        validatePrerequisites("getFileIfExists", path, "path");

        try {
            URI uri = new URI(normalizePath(path));

            ExecuteResult<Void> result = client.executeGET(uri);
            int code = result.response().statusCode();
            if (!HTTPUtils.isSuccessful(code)) {
                if (HTTPUtils.isNotFound(code)) {
                    return null;
                }
                throw new IOException(HTTPClient.getResponseStatus(result));
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
        writeFile(path, content, false);
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
            return new HTTPOutputStream(client, new URI(normalizePath(path)), false);
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

        try {
            URI uri = new URI(normalizePath(path));
            ExecuteResult<Void> result = client.executePUT(uri, source, sourceSize, isWebDAV);
            int code = result.response().statusCode();
            if (!HTTPUtils.isSuccessful(code)) {
                throw new IOException(HTTPClient.getResponseStatus(result));
            }
            // PUT not returns file size
            result = client.executeHEADOrGET(uri);
            code = result.response().statusCode();
            if (!HTTPUtils.isSuccessful(code)) {
                throw new IOException(HTTPClient.getResponseStatus(result));
            }
            return getFileSize(uri, result.response());
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

    public void writeFile(String path, String content, boolean isWebDAV) throws ProviderException {
        validatePrerequisites("writeFile", path, "path");

        try {
            URI uri = new URI(normalizePath(path));

            ExecuteResult<Void> result = client.executePUT(uri, content, isWebDAV);
            int code = result.response().statusCode();
            if (!HTTPUtils.isSuccessful(code)) {
                throw new IOException(HTTPClient.getResponseStatus(result));
            }
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    public HTTPAuthConfig createAuthConfig() {
        if (HTTPAuthMethod.NTLM.equals(getArguments().getAuthMethod().getValue())) {
            return new HTTPAuthConfig(getLogger(), getArguments().getUser().getValue(), getArguments().getPassword().getValue(), getArguments()
                    .getWorkstation().getValue(), getArguments().getDomain().getValue());
        }
        // BASIC
        return new HTTPAuthConfig(getArguments().getUser().getValue(), getArguments().getPassword().getValue());
    }

    public URI getBaseURI() {
        return baseURI;
    }

    public void setBaseURI(URI val) {
        baseURI = val;
    }

    public HTTPClient getClient() {
        return client;
    }

    public void validatePrerequisites(String method, String argValue, String msg) throws ProviderException {
        validatePrerequisites(method);
        validateArgument(method, argValue, msg);
    }

    private void connect(URI uri) throws Exception {
        String notFoundMsg = null;

        ExecuteResult<Void> result = client.executeHEADOrGET(uri);
        int code = result.response().statusCode();
        if (HTTPUtils.isServerError(code)) {
            throw new Exception(HTTPClient.getResponseStatus(result));
        }
        if (HTTPUtils.isNotFound(code)) {
            notFoundMsg = HTTPClient.getResponseStatus(result);
        }

        // Connection successful but baseURI not found - try redefining baseURI
        // - recursive attempt with parent path
        if (notFoundMsg != null) {
            if (SOSString.isEmpty(uri.getPath())) {
                throw new Exception(notFoundMsg);
            }
            URI parentURI = HTTPUtils.getParentURI(uri);
            if (parentURI == null || parentURI.equals(uri)) {
                throw new Exception(notFoundMsg);
            }
            // TODO info?
            getLogger().info("%s[connect][%s]using parent path %s ...", getLogPrefix(), notFoundMsg, parentURI);

            setBaseURI(parentURI);
            setAccessInfo(HTTPUtils.getAccessInfo(getBaseURI(), getArguments().getUser().getValue()));
            connect(getBaseURI());
        }
    }

    private ProviderFile createProviderFile(URI uri, HttpResponse<?> response) throws Exception {
        long size = getFileSize(uri, response);
        if (size < 0) {
            return null;
        }
        return createProviderFile(uri.toString(), size, HTTPClient.getLastModifiedInMillis(response));
    }

    private long getFileSize(URI uri, HttpResponse<?> response) throws Exception {
        long size = response.headers().firstValueAsLong(HTTPUtils.HEADER_CONTENT_LENGTH).orElse(-1);
        if (size < 0) {// e.g. Transfer-Encoding: chunked
            if (getLogger().isDebugEnabled()) {
                getLogger().debug(String.format("%s[reread][%s][size=%s]use InputStream", getLogPrefix(), uri, size));
            }
            size = client.getFileSizeIfChunkedTransferEncoding(uri);
        }
        return size;
    }
}
