package com.sos.commons.vfs.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;

import com.sos.commons.exception.SOSNoSuchFileException;
import com.sos.commons.exception.SOSRequiredArgumentMissingException;
import com.sos.commons.util.SOSClassUtil;
import com.sos.commons.util.SOSPathUtil;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.arguments.impl.SSLArguments;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.commons.AProvider;
import com.sos.commons.vfs.commons.AProviderArguments.Protocol;
import com.sos.commons.vfs.commons.IProvider;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.commons.vfs.commons.file.files.DeleteFilesResult;
import com.sos.commons.vfs.commons.file.files.RenameFilesResult;
import com.sos.commons.vfs.commons.file.selection.ProviderFileSelection;
import com.sos.commons.vfs.exceptions.SOSProviderClientNotInitializedException;
import com.sos.commons.vfs.exceptions.SOSProviderConnectException;
import com.sos.commons.vfs.exceptions.SOSProviderException;
import com.sos.commons.vfs.exceptions.SOSProviderInitializationException;
import com.sos.commons.vfs.http.commons.HTTPAuthConfig;
import com.sos.commons.vfs.http.commons.HTTPClient;
import com.sos.commons.vfs.http.commons.HTTPOutputStream;
import com.sos.commons.vfs.http.commons.HTTPProviderArguments;
import com.sos.commons.vfs.http.commons.HTTPSProviderArguments;
import com.sos.commons.vfs.http.commons.HTTPUtils;

public class HTTPProvider extends AProvider<HTTPProviderArguments> {

    private URI baseURI;
    private HTTPClient client;

    public HTTPProvider(ISOSLogger logger, HTTPProviderArguments arguments) throws SOSProviderInitializationException {
        super(logger, arguments);
        try {
            // can be redefined on connect if not found
            baseURI = info();
        } catch (URISyntaxException e) {
            throw new SOSProviderInitializationException(e);
        }
    }

    /** Overrides {@link IProvider#getPathSeparator()} */
    @Override
    public String getPathSeparator() {
        return SOSPathUtil.PATH_SEPARATOR_UNIX;
    }

    /** Overrides {@link IProvider#isAbsolutePath(String)} */
    @Override
    public boolean isAbsolutePath(String path) {
        return SOSPathUtil.isAbsoluteURIPath(path);
    }

    /** Overrides {@link IProvider#normalizePath(String)}<br/>
     * Normalizes the given path by resolving it against the base URI and ensuring proper encoding.
     * <p>
     * This method ensures that both relative and absolute paths are handled correctly.<br/>
     * It avoids using {@code new URI(String)} directly, as it would throw an exception<br/>
     * if the input contains invalid characters (e.g., spaces, special symbols).<br/>
     * Similarly, {@code new URL(String)} is not used for relative paths since it requires an absolute URL.
     * </p>
     *
     * @param path The input path, which can be relative or absolute.
     * @return A properly normalized and encoded URL string. */
    @Override
    public String normalizePath(String path) {
        // return baseURI.resolve(path).normalize().toString();
        try {
            // new URI(null, path, null) not throw an exception if the path contains invalid characters
            return baseURI.resolve(new URI(null, path, null)).toString();
        } catch (URISyntaxException e) {
            return baseURI.resolve(HTTPUtils.encodeURL(path)).normalize().toString();
        }
    }

    /** Overrides {@link IProvider#connect()} */
    @Override
    public void connect() throws SOSProviderConnectException {
        if (SOSString.isEmpty(getArguments().getHost().getValue())) {
            throw new SOSProviderConnectException(new SOSRequiredArgumentMissingException("host"));
        }

        try {
            getLogger().info(getConnectMsg());

            client = HTTPClient.createAuthenticatedClient(getLogger(), baseURI, getAuthConfig(), getProxyProvider(), getSSLArguments(), getArguments()
                    .getHTTPHeaders().getValue());
            connect(baseURI);

            getLogger().info(getConnectedMsg());
        } catch (Throwable e) {
            throw new SOSProviderConnectException(String.format("[%s]", getAccessInfo()), e);
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
    public List<ProviderFile> selectFiles(ProviderFileSelection selection) throws SOSProviderException {
        logNotImpementedMethod("selectFiles", selection == null ? "" : selection.toString());
        return List.of();
    }

    /** Overrides {@link IProvider#exists(String)} */
    @Override
    public boolean exists(String path) {
        if (client == null || path == null) {
            return false;
        }
        URI uri = null;
        try {
            uri = new URI(normalizePath(path));
            try (CloseableHttpResponse response = client.execute(new HttpGet(uri))) {
                StatusLine sl = response.getStatusLine();
                if (!HTTPClient.isSuccessful(sl)) {
                    throw new IOException(HTTPClient.getResponseStatus(uri, sl));
                }
            }
            return true;
        } catch (Throwable e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("%s[uri=%s][exists=false]%s", getPathOperationPrefix(path), uri, e.toString());
            }
        }
        return false;
    }

    /** Overrides {@link IProvider#createDirectoriesIfNotExists(String)} */
    @Override
    public boolean createDirectoriesIfNotExists(String path) throws SOSProviderException {
        logNotImpementedMethod("createDirectoriesIfNotExists", path);
        return false;
    }

    /** Overrides {@link IProvider#deleteIfExists(String)} */
    @Override
    public boolean deleteIfExists(String path) throws SOSProviderException {
        checkBeforeOperation("deleteIfExists", path, "path");

        try {
            URI uri = new URI(normalizePath(path));
            try (CloseableHttpResponse response = client.execute(new HttpDelete(uri))) {
                StatusLine sl = response.getStatusLine();
                if (!HTTPClient.isSuccessful(sl)) {
                    if (HTTPClient.isNotFound(sl)) {
                        return false;
                    }
                    throw new IOException(HTTPClient.getResponseStatus(uri, sl));
                }
            }
            return true;
        } catch (Throwable e) {
            throw new SOSProviderException(getPathOperationPrefix(path), e.getCause());
        }
    }

    /** Overrides {@link IProvider#deleteFilesIfExists(Collection, boolean)} */
    // TODO check if isDirectory
    @Override
    public DeleteFilesResult deleteFilesIfExists(Collection<String> files, boolean stopOnSingleFileError) throws SOSProviderException {
        if (files == null) {
            return null;
        }
        checkBeforeOperation("deleteFilesIfExists");

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
            new SOSProviderException(e);
        }
        return r;
    }

    /** Overrides {@link IProvider#renameFilesIfSourceExists(Map, boolean)} */
    @Override
    public RenameFilesResult renameFilesIfSourceExists(Map<String, String> files, boolean stopOnSingleFileError) throws SOSProviderException {
        if (files == null) {
            return null;
        }
        checkBeforeOperation("renameFilesIfSourceExists");

        RenameFilesResult r = new RenameFilesResult(files.size());
        try {
            l: for (Map.Entry<String, String> entry : files.entrySet()) {
                String source = entry.getKey();
                String target = entry.getValue();
                try {
                    if (renameFileIfExists(source, target)) {
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
            new SOSProviderException(e);
        }
        return r;
    }

    /** Overrides {@link IProvider#getFileIfExists(String)} */
    @Override
    public ProviderFile getFileIfExists(String path) throws SOSProviderException {
        checkBeforeOperation("getFileIfExists", path, "path");

        ProviderFile file = null;
        try {
            URI uri = new URI(normalizePath(path));
            try (CloseableHttpResponse response = client.execute(new HttpGet(uri))) {
                StatusLine sl = response.getStatusLine();
                if (!HTTPClient.isSuccessful(sl) || !HTTPClient.isNotFound(sl)) {
                    throw new IOException(HTTPClient.getResponseStatus(uri, sl));
                }
                file = createProviderFile(uri, response);
            }
        } catch (Throwable e) {
            throw new SOSProviderException(getPathOperationPrefix(path), e.getCause());
        }
        return file;
    }

    /** Overrides {@link IProvider#rereadFileIfExists(ProviderFile)} */
    @Override
    public ProviderFile rereadFileIfExists(ProviderFile file) throws SOSProviderException {
        checkBeforeOperation("rereadFileIfExists");

        try {
            return refreshFileMetadata(file, getFileIfExists(file.getFullPath()));
        } catch (Throwable e) {
            throw new SOSProviderException(getPathOperationPrefix(file.getFullPath()), e);
        }
    }

    /** Overrides {@link IProvider#getFileContentIfExists(String, String)} */
    @Override
    public String getFileContentIfExists(String path) throws SOSProviderException {
        checkBeforeOperation("getFileContentIfExists", path, "path");

        StringBuilder content = new StringBuilder();
        try (InputStream is = client.getHTTPInputStream(new URI(normalizePath(path))); Reader r = new InputStreamReader(is, StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(r)) {
            br.lines().forEach(content::append);
            return content.toString();
        } catch (SOSNoSuchFileException e) {
            return null;
        } catch (Throwable e) {
            throw new SOSProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#writeFile(String, String)} */
    @Override
    public void writeFile(String path, String content) throws SOSProviderException {
        checkBeforeOperation("writeFile", path, "path");

        try {
            URI uri = new URI(normalizePath(path));

            HttpPut httpPut = new HttpPut(uri);
            // httpPut.setEntity(new StringEntity(content, StandardCharsets.UTF_8));
            // httpPut.setHeader("Content-Type", "text/plain");
            httpPut.setEntity(new ByteArrayEntity(content.getBytes(StandardCharsets.UTF_8)));
            httpPut.setHeader("Content-Type", "application/octet-stream");
            // The 'Content-Length' Header is automatically set when an HttpEntity is used

            try (CloseableHttpResponse response = client.execute(httpPut)) {
                StatusLine sl = response.getStatusLine();
                if (!HTTPClient.isSuccessful(sl)) {
                    throw new IOException(HTTPClient.getResponseStatus(uri, sl));
                }
            }
        } catch (Throwable e) {
            throw new SOSProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#setFileLastModifiedFromMillis(String, long)} */
    @Override
    public void setFileLastModifiedFromMillis(String path, long milliseconds) throws SOSProviderException {
        logNotImpementedMethod("setFileLastModifiedFromMillis", "path=" + path + ",milliseconds=" + milliseconds);
    }

    /** Overrides {@link IProvider#getInputStream(String)} */
    @Override
    public InputStream getInputStream(String path) throws SOSProviderException {
        checkBeforeOperation("getInputStream", path, "path");

        try {
            return client.getHTTPInputStream(new URI(normalizePath(path)));
        } catch (Throwable e) {
            throw new SOSProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#getOutputStream(String, boolean)} */
    @Override
    public OutputStream getOutputStream(String path, boolean append) throws SOSProviderException {
        checkBeforeOperation("getOutputStream", path, "path");

        try {
            return new HTTPOutputStream(client, new HttpPut(new URI(normalizePath(path))));
        } catch (Throwable e) {
            throw new SOSProviderException(getPathOperationPrefix(path), e);
        }
    }

    public URI getBaseURI() {
        return baseURI;
    }

    private URI info() throws URISyntaxException {
        URI baseURI;
        String hostOrUrl = SOSPathUtil.getUnixStyleDirectoryWithoutTrailingSeparator(getArguments().getHost().getValue());
        if (isAbsolutePath(hostOrUrl)) {
            baseURI = HTTPUtils.toBaseURI(hostOrUrl);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("http://").append(hostOrUrl);
            if (getArguments().getPort().isDirty()) {
                sb.append(":").append(getArguments().getPort().getValue());
            }
            baseURI = HTTPUtils.toBaseURI(sb.toString());
        }
        setAccessInfo(HTTPUtils.getAccessInfo(baseURI, getArguments().getUser().getValue()));
        return baseURI;
    }

    private SSLArguments getSSLArguments() {
        return Protocol.HTTPS.equals(getArguments().getProtocol().getValue()) ? ((HTTPSProviderArguments) getArguments()).getSSL() : null;
    }

    public HTTPAuthConfig getAuthConfig() {
        // BASIC
        return new HTTPAuthConfig(getArguments().getUser().getValue(), getArguments().getPassword().getValue());
    }

    private void connect(URI uri) throws Exception {
        StatusLine notFoundStatusLine = null;
        try (CloseableHttpResponse response = client.execute(new HttpGet(baseURI))) {
            StatusLine sl = response.getStatusLine();
            if (HTTPClient.isServerError(notFoundStatusLine)) {
                throw new Exception(HTTPClient.getResponseStatus(baseURI, sl));
            }
            if (HTTPClient.isNotFound(sl)) {
                notFoundStatusLine = sl;
            }
        }
        // Connection successful but baseURI not found - try redefining baseURI
        // - recursive attempt with parent path
        if (notFoundStatusLine != null) {
            if (SOSString.isEmpty(uri.getPath())) {
                throw new Exception(HTTPClient.getResponseStatus(uri, notFoundStatusLine));
            }
            URI parentURI = HTTPUtils.getParentURI(uri);
            if (parentURI == null) {
                throw new Exception(HTTPClient.getResponseStatus(uri, notFoundStatusLine));
            }
            // TODO info?
            getLogger().info("%s[connect][%s]using parent path %s ...", getLogPrefix(), HTTPClient.getResponseStatus(uri, notFoundStatusLine),
                    parentURI);
            baseURI = parentURI;
            setAccessInfo(HTTPUtils.getAccessInfo(baseURI, getArguments().getUser().getValue()));

            connect(baseURI);
        }
    }

    /** PUT,DELETE implementation<br />
     * - alternative - MOVE (may not be supported by the server…)
     * 
     * @param source
     * @param target
     * @return
     * @throws SOSProviderException */
    private boolean renameFileIfExists(String source, String target) throws SOSProviderException {
        InputStream is = null;
        boolean result = false;
        try {
            URI sourceURI = new URI(normalizePath(source));
            URI targetURI = new URI(normalizePath(target));

            is = client.getHTTPInputStream(sourceURI);
            HttpPut request = new HttpPut(targetURI);
            request.setEntity(new InputStreamEntity(is));

            try (CloseableHttpResponse response = client.execute(request)) {
                SOSClassUtil.closeQuietly(is);
                is = null;

                StatusLine sl = response.getStatusLine();
                if (!HTTPClient.isSuccessful(sl)) {
                    if (!HTTPClient.isNotFound(sl)) {
                        throw new IOException(HTTPClient.getResponseStatus(targetURI, sl));
                    }
                }
            }
            deleteIfExists(source);
            result = true;
        } catch (SOSNoSuchFileException e) { // is = getInputStream(s);
        } catch (Throwable e) {
            throw new SOSProviderException(getPathOperationPrefix(source + "->" + target), e.getCause());
        } finally {
            SOSClassUtil.closeQuietly(is);
        }
        return result;
    }

    private void checkBeforeOperation(String method) throws SOSProviderException {
        if (client == null) {
            throw new SOSProviderClientNotInitializedException(getLogPrefix() + method + "HTTPPClient");
        }
    }

    private void checkBeforeOperation(String method, String paramValue, String msg) throws SOSProviderException {
        checkBeforeOperation(method);
        checkParam(method, paramValue, msg);
    }

    private ProviderFile createProviderFile(URI uri, HttpResponse response) throws Exception {
        HttpEntity entity = response.getEntity();
        if (entity == null) {
            return null;
        }
        long size = getFileSize(uri, entity);
        if (size < 0) {
            return null;
        }
        return createProviderFile(uri.toString(), size, HTTPClient.getLastModifiedInMillis(response));
    }

    private long getFileSize(URI uri, HttpEntity entity) throws Exception {
        long size = entity.getContentLength();
        if (size < 0) {// e.g. Transfer-Encoding: chunked
            if (getLogger().isDebugEnabled()) {
                getLogger().debug(String.format("%s[getSizeFromEntity][%s][size=%s]use InputStream", getLogPrefix(), uri, size));
            }
            size = HTTPUtils.getFileSizeIfChunkedTransferEncoding(entity);
        }
        return size;
    }

}
