package com.sos.commons.vfs.webdav.jackrabbit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.HttpMove;
import org.apache.jackrabbit.webdav.client.methods.HttpPropfind;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertySet;

import com.sos.commons.exception.SOSNoSuchFileException;
import com.sos.commons.exception.SOSRequiredArgumentMissingException;
import com.sos.commons.util.SOSClassUtil;
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
import com.sos.commons.vfs.http.HTTPProvider;
import com.sos.commons.vfs.http.apache.HTTPClient;
import com.sos.commons.vfs.http.apache.HTTPClient.ExecuteResult;
import com.sos.commons.vfs.http.apache.HTTPOutputStream;
import com.sos.commons.vfs.http.commons.HTTPUtils;
import com.sos.commons.vfs.webdav.WebDAVProvider;
import com.sos.commons.vfs.webdav.commons.WebDAVProviderArguments;

public class ProviderImpl extends WebDAVProvider {

    private HTTPClient client;

    public ProviderImpl(ISOSLogger logger, WebDAVProviderArguments args) throws ProviderInitializationException {
        super(logger, args);
    }

    /** Overrides {@link IProvider#connect()} */
    @Override
    public void connect() throws ProviderConnectException {
        if (SOSString.isEmpty(getArguments().getHost().getValue())) {
            throw new ProviderConnectException(new SOSRequiredArgumentMissingException("host"));
        }

        try {
            getLogger().info(getConnectMsg());

            client = HTTPClient.createAuthenticatedClient(getLogger(), getBaseURI(), getAuthConfig(), getProxyProvider(), getSSLArguments(), null);
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
        validatePrerequisites("selectFiles");

        selection = ProviderFileSelection.createIfNull(selection);
        // selection.setFileTypeChecker();

        String directory = selection.getConfig().getDirectory() == null ? "" : selection.getConfig().getDirectory();
        List<ProviderFile> result = new ArrayList<>();
        try {
            ProviderUtils.selectFiles(this, selection, directory, result);
        } catch (ProviderException e) {
            throw e;
        }
        return result;
    }

    /** Overrides {@link IProvider#exists(String)} */
    @Override
    public boolean exists(String path) throws ProviderException {
        validatePrerequisites("exists", path, "path");

        try {
            return ProviderUtils.exists(client, new URI(normalizePath(path)));
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#createDirectoriesIfNotExists(String)}<br/>
     * Check if exists - reverse order:<br/>
     * - https://example.com/test/1/2/3<br/>
     * - https://example.com/test/1/2<br/>
     * - https://example.com/test/1<br/>
     * Creates:<br/>
     * - https://example.com/test/1<br/>
     * - https://example.com/test/1/2<br/>
     * - https://example.com/test/1/2/3<br/>
     */
    @Override
    public boolean createDirectoriesIfNotExists(String path) throws ProviderException {
        validatePrerequisites("createDirectoriesIfNotExists", path, "path");

        try {
            URI uri = new URI(normalizePath(path));
            if (ProviderUtils.directoryExists(client, uri)) {
                return false; // already exists
            }

            Deque<URI> toCreate = new ArrayDeque<>();
            URI parent = HTTPUtils.getParentURI(uri);
            while (parent != null && !parent.equals(uri) && !ProviderUtils.directoryExists(client, parent)) {
                toCreate.push(parent);
                parent = HTTPUtils.getParentURI(parent);
            }

            boolean created = false;
            while (!toCreate.isEmpty()) {
                ProviderUtils.createDirectory(client, toCreate.pop());
                created = true;
            }
            return created;
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#deleteIfExists(String)} */
    @Override
    public boolean deleteIfExists(String path) throws ProviderException {
        validatePrerequisites("deleteIfExists", path, "path");

        try {
            HttpDelete request = new HttpDelete(new URI(normalizePath(path)));
            try (ExecuteResult result = client.execute(request); CloseableHttpResponse response = result.getResponse()) {
                int code = response.getStatusLine().getStatusCode();
                if (!HTTPUtils.isSuccessful(code)) {
                    if (HTTPUtils.isNotFound(code)) {
                        return false;
                    }
                    throw new IOException(HTTPClient.getResponseStatus(result));
                }
            }
            return true;
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#deleteFilesIfExists(Collection, boolean))} */
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

    /** Overrides {@link IProvider#renameFilesIfSourceExists(Map, boolean))} */
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
            HttpPropfind request = ProviderUtils.createFilePropertiesRequest(uri);
            try (ExecuteResult result = client.execute(request); CloseableHttpResponse response = result.getResponse()) {
                int code = response.getStatusLine().getStatusCode();
                if (!HTTPUtils.isSuccessful(code)) {
                    if (HTTPUtils.isNotFound(code)) {
                        return null;
                    }
                    throw new IOException(HTTPClient.getResponseStatus(result));
                }
                MultiStatus status = ProviderUtils.getMuiltiStatus(request, response);
                if (status == null) {
                    return null;
                }
                for (MultiStatusResponse resource : status.getResponses()) {
                    return createProviderFile(resource.getHref(), response, resource.getProperties(HttpStatus.SC_OK));
                }
                return null;
            }
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#getFileContentIfExists(String, String)}<br />
     * 
     * @apiNote this method is implemented in the same way as {@link HTTPProvider#getFileContentIfExists(String)}.<br/>
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
     * @apiNote this method is implemented in the same way(except DavConstants.HEADER_OVERWRITE) as {@link HTTPProvider#writeFile(String,String)}.<br/>
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
            request.setHeader(DavConstants.HEADER_OVERWRITE, "T");
            request.setHeader("Content-Type", "application/octet-stream");
            // The 'Content-Length' Header is automatically set when an HttpEntity is used

            try (ExecuteResult result = client.execute(request); CloseableHttpResponse response = result.getResponse()) {
                int code = response.getStatusLine().getStatusCode();
                if (!HTTPUtils.isSuccessful(code)) {
                    throw new IOException(HTTPClient.getResponseStatus(result));
                }
            }
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#setFileLastModifiedFromMillis(String, long))} */
    @Override
    public void setFileLastModifiedFromMillis(String path, long milliseconds) throws ProviderException {
        logNotImpementedMethod("setFileLastModifiedFromMillis", "path=" + path + ",milliseconds=" + milliseconds);
    }

    /** Overrides {@link IProvider#getInputStream(String)}<br/>
     * 
     * @apiNote this method is implemented in the same way as {@link HTTPProvider#getInputStream(String)}.<br/>
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
     * @apiNote this method is implemented in the same way as {@link HTTPProvider#getOutputStream(String,boolean)}.<br/>
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

    public HTTPClient getClient() {
        return client;
    }

    public ProviderFile createProviderFile(String href, CloseableHttpResponse response, DavPropertySet prop) throws Exception {
        if (response == null || prop == null) {
            return null;
        }
        long size = getFileSize(href, response, prop);
        if (size < 0) {
            return null;
        }
        return createProviderFile(href, size, getLastModifiedInMillis(prop));
    }

    private void connect(URI uri) throws Exception {
        String notFoundMsg = null;

        HttpPropfind request = new HttpPropfind(uri, null, DavConstants.DEPTH_0);
        try (ExecuteResult result = client.execute(request); CloseableHttpResponse response = result.getResponse()) {
            int code = response.getStatusLine().getStatusCode();
            if (HTTPUtils.isServerError(code)) {
                throw new Exception(HTTPClient.getResponseStatus(result));
            }
            if (HTTPUtils.isNotFound(code)) {
                notFoundMsg = HTTPClient.getResponseStatus(result);
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

            setBaseURI(parentURI);
            setAccessInfo(HTTPUtils.getAccessInfo(getBaseURI(), getArguments().getUser().getValue()));
            connect(getBaseURI());
        }
    }

    private boolean renameFileIfExists(String source, String target) throws ProviderException {
        try {
            URI sourceURI = new URI(normalizePath(source));
            URI targetURI = new URI(normalizePath(target));

            HttpMove request = new HttpMove(sourceURI, targetURI, true);
            try (ExecuteResult result = client.execute(request); CloseableHttpResponse response = result.getResponse()) {
                int code = response.getStatusLine().getStatusCode();
                if (!HTTPUtils.isSuccessful(code)) {
                    if (HTTPUtils.isNotFound(code)) {
                        return false;
                    }
                    throw new IOException(HTTPClient.getResponseStatus(result));
                }
            }
            return true;
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(source + "->" + target), e);
        }
    }

    private void validatePrerequisites(String method, String argValue, String msg) throws ProviderException {
        validatePrerequisites(method);
        validateArgument(method, argValue, msg);
    }

    private long getFileSize(String href, CloseableHttpResponse response, DavPropertySet prop) throws Exception {
        DavProperty<?> p = prop.get(DavConstants.PROPERTY_GETCONTENTLENGTH);
        long size = p == null ? -1L : Long.parseLong(p.getValue().toString());

        if (size < 0) {// e.g. Transfer-Encoding: chunked
            if (getLogger().isDebugEnabled()) {
                getLogger().debug(String.format("%s[getSizeFromEntity][%s][size=%s]use InputStream", getLogPrefix(), href, size));
            }
            size = HTTPClient.getFileSizeIfChunkedTransferEncoding(response.getEntity());
        }
        return size;
    }

    private long getLastModifiedInMillis(DavPropertySet prop) {
        DavProperty<?> p = prop.get(DavConstants.PROPERTY_GETLASTMODIFIED);
        String value = (p == null || p.getValue() == null) ? null : p.getValue().toString();
        return HTTPClient.getLastModifiedInMillis(value);
    }

}
