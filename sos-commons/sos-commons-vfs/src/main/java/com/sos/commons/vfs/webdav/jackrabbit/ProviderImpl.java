package com.sos.commons.vfs.webdav.jackrabbit;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.client.methods.HttpPropfind;

import com.sos.commons.exception.SOSRequiredArgumentMissingException;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.commons.IProvider;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.commons.vfs.commons.file.files.DeleteFilesResult;
import com.sos.commons.vfs.commons.file.files.RenameFilesResult;
import com.sos.commons.vfs.commons.file.selection.ProviderFileSelection;
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

            client = HTTPClient.createAuthenticatedClient(getLogger(), getBaseURI(), getAuthConfig(), getArguments().getProxy(), getSSLArguments(),
                    null);
            connect(getBaseURI());

            getLogger().info(getConnectedMsg());
        } catch (Throwable e) {
            throw new SOSProviderConnectException(String.format("[%s]", getAccessInfo()), e);
        }
    }

    /** Overrides {@link IProvider#isConnected()} */
    @Override
    public boolean isConnected() {
        // TODO Auto-generated method stub
        return false;
    }

    /** Overrides {@link IProvider#disconnect()} */
    @Override
    public void disconnect() {
        // TODO Auto-generated method stub

    }

    /** Overrides {@link IProvider#selectFiles(ProviderFileSelection)} */
    @Override
    public List<ProviderFile> selectFiles(ProviderFileSelection selection) throws SOSProviderException {
        // TODO Auto-generated method stub
        return null;
    }

    /** Overrides {@link IProvider#exists(String)} */
    @Override
    public boolean exists(String path) {
        // TODO Auto-generated method stub
        return false;
    }

    /** Overrides {@link IProvider#createDirectoriesIfNotExists(String)} */
    @Override
    public boolean createDirectoriesIfNotExists(String path) throws SOSProviderException {
        // TODO Auto-generated method stub
        return false;
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
            String newPath = uri.getPath().substring(0, uri.getPath().lastIndexOf('/'));
            if (SOSString.isEmpty(newPath)) {
                throw new Exception(HTTPClient.getResponseStatus(uri, notFoundStatusLine));
            }
            // TODO info?
            getLogger().info("%s[connect][%s]using parent path %s ...", getLogPrefix(), HTTPClient.getResponseStatus(uri, notFoundStatusLine),
                    newPath);
            setBaseURI(new URI(uri.getScheme(), uri.getHost(), newPath, null, null));
            setAccessInfo(HTTPUtils.getAccessInfo(getBaseURI(), getArguments().getUser().getValue()));

            connect(getBaseURI());
        }
    }

}
