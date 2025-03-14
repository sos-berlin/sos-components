package com.sos.commons.vfs.webdav.jackrabbit;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.client.methods.HttpPropfind;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertySet;

import com.sos.commons.exception.SOSRequiredArgumentMissingException;
import com.sos.commons.util.SOSClassUtil;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.commons.IProvider;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.commons.vfs.commons.file.files.DeleteFilesResult;
import com.sos.commons.vfs.commons.file.files.RenameFilesResult;
import com.sos.commons.vfs.commons.file.selection.ProviderFileSelection;
import com.sos.commons.vfs.exceptions.SOSProviderClientNotInitializedException;
import com.sos.commons.vfs.exceptions.SOSProviderConnectException;
import com.sos.commons.vfs.exceptions.SOSProviderException;
import com.sos.commons.vfs.exceptions.SOSProviderInitializationException;
import com.sos.commons.vfs.http.commons.HTTPClient;
import com.sos.commons.vfs.http.commons.HTTPUtils;
import com.sos.commons.vfs.webdav.commons.AWebDAVProvider;
import com.sos.commons.vfs.webdav.commons.WebDAVProviderArguments;

public class ProviderImpl extends AWebDAVProvider {

    private HTTPClient client;

    public ProviderImpl(ISOSLogger logger, WebDAVProviderArguments args) throws SOSProviderInitializationException {
        super(logger, args);
    }

    /** Overrides {@link IProvider#connect()} */
    @Override
    public void connect() throws SOSProviderConnectException {
        if (SOSString.isEmpty(getArguments().getHost().getValue())) {
            throw new SOSProviderConnectException(new SOSRequiredArgumentMissingException("host"));
        }

        try {
            getLogger().info(getConnectMsg());

            client = HTTPClient.createAuthenticatedClient(getLogger(), getBaseURI(), getAuthConfig(), getProxyProvider(), getSSLArguments(), null);
            connect(getBaseURI());

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
        checkBeforeOperation("selectFiles");

        selection = ProviderFileSelection.createIfNull(selection);
        // selection.setFileTypeChecker();

        String directory = selection.getConfig().getDirectory() == null ? "" : selection.getConfig().getDirectory();
        List<ProviderFile> result = new ArrayList<>();
        try {
            ProviderUtils.selectFiles(this, selection, directory, result);
        } catch (SOSProviderException e) {
            throw e;
        }
        return result;
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
            return ProviderUtils.exists(client, uri);
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
        checkBeforeOperation("createDirectoriesIfNotExists", path, "path");

        try {
            URI uri = new URI(normalizePath(path));
            if (ProviderUtils.directoryExists(client, uri)) {
                return false; // already exists
            }

            // check in reverse order:
            // https://example.com/test/1/2/3
            // https://example.com/test/1/2
            // https://example.com/test/1
            Deque<URI> toCreate = new ArrayDeque<>();
            URI parent = HTTPUtils.getParentURI(uri);
            while (parent != null && !ProviderUtils.directoryExists(client, parent)) {
                toCreate.push(parent);
                parent = HTTPUtils.getParentURI(parent);
            }

            // create:
            // https://example.com/test/1
            // https://example.com/test/1/2
            // https://example.com/test/1/2/3
            boolean created = false;
            while (!toCreate.isEmpty()) {
                ProviderUtils.createDirectory(client, toCreate.pop());
                created = true;
            }
            return created;
        } catch (Throwable e) {
            throw new SOSProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#deleteIfExists(String)} */
    @Override
    public boolean deleteIfExists(String path) throws SOSProviderException {
        // TODO Auto-generated method stub
        return false;
    }

    /** Overrides {@link IProvider#deleteFilesIfExists(Collection, boolean))} */
    @Override
    public DeleteFilesResult deleteFilesIfExists(Collection<String> files, boolean stopOnSingleFileError) throws SOSProviderException {
        // TODO Auto-generated method stub
        return null;
    }

    /** Overrides {@link IProvider#renameFilesIfSourceExists(Map, boolean))} */
    @Override
    public RenameFilesResult renameFilesIfSourceExists(Map<String, String> files, boolean stopOnSingleFileError) throws SOSProviderException {
        // TODO Auto-generated method stub
        return null;
    }

    /** Overrides {@link IProvider#getFileIfExists(String)} */
    @Override
    public ProviderFile getFileIfExists(String path) throws SOSProviderException {
        // TODO Auto-generated method stub
        return null;
    }

    /** Overrides {@link IProvider#rereadFileIfExists(ProviderFile)} */
    @Override
    public ProviderFile rereadFileIfExists(ProviderFile file) throws SOSProviderException {
        // TODO Auto-generated method stub
        return null;
    }

    /** Overrides {@link IProvider#getFileContentIfExists(String)} */
    @Override
    public String getFileContentIfExists(String path) throws SOSProviderException {
        // TODO Auto-generated method stub
        return null;
    }

    /** Overrides {@link IProvider#writeFile(String, String)} */
    @Override
    public void writeFile(String path, String content) throws SOSProviderException {
        // TODO Auto-generated method stub

    }

    /** Overrides {@link IProvider#setFileLastModifiedFromMillis(String, long))} */
    @Override
    public void setFileLastModifiedFromMillis(String path, long milliseconds) throws SOSProviderException {
        // TODO Auto-generated method stub

    }

    /** Overrides {@link IProvider#getInputStream(String)} */
    @Override
    public InputStream getInputStream(String path) throws SOSProviderException {
        // TODO Auto-generated method stub
        return null;
    }

    /** Overrides {@link IProvider#getOutputStream(String, boolean))} */
    @Override
    public OutputStream getOutputStream(String path, boolean append) throws SOSProviderException {
        // TODO Auto-generated method stub
        return null;
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
        return createProviderFile(href, size, HTTPClient.getLastModifiedInMillis(response));
    }

    private void connect(URI uri) throws Exception {
        StatusLine notFoundStatusLine = null;
        try (CloseableHttpResponse response = client.execute(new HttpPropfind(getBaseURI(), null, DavConstants.DEPTH_0))) {
            StatusLine sl = response.getStatusLine();
            if (HTTPClient.isServerError(notFoundStatusLine)) {
                throw new Exception(HTTPClient.getResponseStatus(getBaseURI(), sl));
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
            setBaseURI(parentURI);
            setAccessInfo(HTTPUtils.getAccessInfo(getBaseURI(), getArguments().getUser().getValue()));

            connect(getBaseURI());
        }
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

    private long getFileSize(String href, CloseableHttpResponse response, DavPropertySet prop) throws Exception {
        DavProperty<?> p = prop.get(DavConstants.PROPERTY_GETCONTENTLENGTH);
        long size = p == null ? -1L : Long.parseLong(p.getValue().toString());

        if (size < 0) {// e.g. Transfer-Encoding: chunked
            if (getLogger().isDebugEnabled()) {
                getLogger().debug(String.format("%s[getSizeFromEntity][%s][size=%s]use InputStream", getLogPrefix(), href, size));
            }
            size = HTTPUtils.getFileSizeIfChunkedTransferEncoding(response.getEntity());
        }
        return size;
    }

}
