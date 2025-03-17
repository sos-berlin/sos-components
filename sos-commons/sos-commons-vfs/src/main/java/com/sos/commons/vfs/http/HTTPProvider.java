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
import com.sos.commons.util.SOSPathUtils;
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
import com.sos.commons.vfs.exceptions.ProviderClientNotInitializedException;
import com.sos.commons.vfs.exceptions.ProviderConnectException;
import com.sos.commons.vfs.exceptions.ProviderException;
import com.sos.commons.vfs.exceptions.ProviderInitializationException;
import com.sos.commons.vfs.http.commons.HTTPAuthConfig;
import com.sos.commons.vfs.http.commons.HTTPClient;
import com.sos.commons.vfs.http.commons.HTTPOutputStream;
import com.sos.commons.vfs.http.commons.HTTPProviderArguments;
import com.sos.commons.vfs.http.commons.HTTPSProviderArguments;
import com.sos.commons.vfs.http.commons.HTTPUtils;
import com.sos.commons.vfs.webdav.jackrabbit.ProviderImpl;

public class HTTPProvider extends AProvider<HTTPProviderArguments> {

    private URI baseURI;
    private HTTPClient client;

    public HTTPProvider(ISOSLogger logger, HTTPProviderArguments arguments) throws ProviderInitializationException {
        super(logger, arguments);
        try {
            // if baseURI not found, can be redefined when connecting
            baseURI = HTTPUtils.getBaseURI(getArguments().getHost(), getArguments().getPort());
            setAccessInfo(HTTPUtils.getAccessInfo(baseURI, getArguments().getUser().getValue()));
        } catch (URISyntaxException e) {
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

            client = HTTPClient.createAuthenticatedClient(getLogger(), baseURI, getAuthConfig(), getProxyProvider(), getSSLArguments(), getArguments()
                    .getHTTPHeaders().getValue());
            connect(baseURI);

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
            HttpGet request = new HttpGet(uri);
            try (CloseableHttpResponse response = client.execute(request)) {
                StatusLine sl = response.getStatusLine();
                if (!HTTPClient.isSuccessful(sl)) {
                    throw new IOException(HTTPClient.getResponseStatus(request, response));
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
            HttpDelete request = new HttpDelete(uri);
            try (CloseableHttpResponse response = client.execute(request)) {
                StatusLine sl = response.getStatusLine();
                if (!HTTPClient.isSuccessful(sl)) {
                    if (HTTPClient.isNotFound(sl)) {
                        return false;
                    }
                    throw new IOException(HTTPClient.getResponseStatus(request, response));
                }
            }
            return true;
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e.getCause());
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
            HttpGet request = new HttpGet(uri);
            try (CloseableHttpResponse response = client.execute(request)) {
                StatusLine sl = response.getStatusLine();
                if (!HTTPClient.isSuccessful(sl)) {
                    if (HTTPClient.isNotFound(sl)) {
                        return null;
                    }
                    throw new IOException(HTTPClient.getResponseStatus(request, response));
                }
                return createProviderFile(uri, response);
            }
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e.getCause());
        }

    }

    /** Overrides {@link IProvider#getFileContentIfExists(String, String)} <br/>
     * 
     * @apiNote this method is implemented in the same way as webdav/jackrabbit {@link ProviderImpl#getFileContentIfExists(String)}.<br/>
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
     * @apiNote this method is implemented in the same way as webdav/jackrabbit (except DavConstants.HEADER_OVERWRITE)
     *          {@link ProviderImpl#writeFile(String,String)}.<br/>
     */
    @Override
    public void writeFile(String path, String content) throws ProviderException {
        validatePrerequisites("writeFile", path, "path");

        try {
            URI uri = new URI(normalizePath(path));

            HttpPut request = new HttpPut(uri);
            // request.setEntity(new StringEntity(content, StandardCharsets.UTF_8));
            // request.setHeader("Content-Type", "text/plain");
            request.setEntity(new ByteArrayEntity(content.getBytes(StandardCharsets.UTF_8)));
            request.setHeader("Content-Type", "application/octet-stream");
            // The 'Content-Length' Header is automatically set when an HttpEntity is used

            try (CloseableHttpResponse response = client.execute(request)) {
                StatusLine sl = response.getStatusLine();
                if (!HTTPClient.isSuccessful(sl)) {
                    throw new IOException(HTTPClient.getResponseStatus(request, response));
                }
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
     * @apiNote this method is implemented in the same way as webdav/jackrabbit {@link ProviderImpl#getInputStream(String)}.<br/>
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
     * @apiNote this method is implemented in the same way as webdav/jackrabbit {@link ProviderImpl#getOutputStream(String,boolean)}.<br/>
     */
    @Override
    public OutputStream getOutputStream(String path, boolean append) throws ProviderException {
        validatePrerequisites("getOutputStream", path, "path");

        try {
            return new HTTPOutputStream(client, new URI(normalizePath(path)));
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

    public HTTPAuthConfig getAuthConfig() {
        // BASIC
        return new HTTPAuthConfig(getArguments().getUser().getValue(), getArguments().getPassword().getValue());
    }

    public URI getBaseURI() {
        return baseURI;
    }

    private SSLArguments getSSLArguments() {
        return Protocol.HTTPS.equals(getArguments().getProtocol().getValue()) ? ((HTTPSProviderArguments) getArguments()).getSSL() : null;
    }

    private void connect(URI uri) throws Exception {
        String notFoundMsg = null;

        HttpGet request = new HttpGet(uri);
        try (CloseableHttpResponse response = client.execute(request)) {
            StatusLine sl = response.getStatusLine();
            if (HTTPClient.isServerError(sl)) {
                throw new Exception(HTTPClient.getResponseStatus(request, response));
            }
            if (HTTPClient.isNotFound(sl)) {
                notFoundMsg = HTTPClient.getResponseStatus(request, response);
            }
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
     * @throws ProviderException */
    private boolean renameFileIfExists(String source, String target) throws ProviderException {
        InputStream is = null;
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
                    if (HTTPClient.isNotFound(sl)) {
                        return false;
                    }
                    throw new IOException(HTTPClient.getResponseStatus(request, response));
                }
            }
            deleteIfExists(source);
            return true;
        } catch (SOSNoSuchFileException e) { // is = getInputStream(s);
            return false;
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(source + "->" + target), e.getCause());
        } finally {
            SOSClassUtil.closeQuietly(is);
        }

    }

    private void validatePrerequisites(String method, String argValue, String msg) throws ProviderException {
        validatePrerequisites(method);
        validateArgument(method, argValue, msg);
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
