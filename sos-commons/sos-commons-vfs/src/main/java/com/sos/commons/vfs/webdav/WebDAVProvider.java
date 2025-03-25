package com.sos.commons.vfs.webdav;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.commons.IProvider;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.commons.vfs.commons.file.selection.ProviderFileSelection;
import com.sos.commons.vfs.exceptions.ProviderException;
import com.sos.commons.vfs.exceptions.ProviderInitializationException;
import com.sos.commons.vfs.http.HTTPProvider;
import com.sos.commons.vfs.http.commons.HTTPClient;
import com.sos.commons.vfs.http.commons.HTTPClient.ExecuteResult;
import com.sos.commons.vfs.http.commons.HTTPUtils;
import com.sos.commons.vfs.webdav.commons.WebDAVProviderArguments;
import com.sos.commons.vfs.webdav.commons.WebDAVProviderUtils;
import com.sos.commons.vfs.webdav.commons.WebDAVResource;

public class WebDAVProvider extends HTTPProvider {

    public WebDAVProvider(ISOSLogger logger, WebDAVProviderArguments args) throws ProviderInitializationException {
        super(logger, args);
    }

    /** Overrides {@link IProvider#selectFiles(ProviderFileSelection)} */
    @Override
    public List<ProviderFile> selectFiles(ProviderFileSelection selection) throws ProviderException {
        validatePrerequisites("selectFiles");

        selection = ProviderFileSelection.createIfNull(selection);
        String directory = selection.getConfig().getDirectory() == null ? "" : selection.getConfig().getDirectory();
        List<ProviderFile> result = new ArrayList<>();
        try {
            WebDAVProviderUtils.selectFiles(this, selection, directory, result);
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
            return WebDAVProviderUtils.exists(getClient(), new URI(normalizePathEncoded(path)));
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
            URI uri = new URI(normalizePathEncoded(path));
            if (WebDAVProviderUtils.directoryExists(this, uri)) {
                return false; // already exists
            }

            Deque<URI> parentsToCreate = new ArrayDeque<>();
            URI parent = HTTPUtils.getParentURI(uri);
            while (parent != null && !parent.equals(uri) && !WebDAVProviderUtils.directoryExists(this, parent)) {
                parentsToCreate.push(parent);
                parent = HTTPUtils.getParentURI(parent);
            }
            // create parent directories
            while (!parentsToCreate.isEmpty()) {
                WebDAVProviderUtils.createDirectory(this, parentsToCreate.pop());
            }
            // create given directory
            WebDAVProviderUtils.createDirectory(this, uri);
            return true;
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#renameFileIfSourceExists(String, String)} */
    @Override
    public boolean renameFileIfSourceExists(String source, String target) throws ProviderException {
        validatePrerequisites("renameFileIfSourceExists", source, "source");
        validateArgument("renameFileIfSourceExists", target, "target");
        try {
            URI sourceURI = new URI(normalizePathEncoded(source));
            URI targetURI = new URI(normalizePathEncoded(target));

            HttpRequest.Builder builder = getClient().createRequestBuilder(sourceURI);
            builder.header("Destination", targetURI.toString());
            ExecuteResult<Void> result = getClient().executeWithoutResponseBody(builder.method("MOVE", BodyPublishers.noBody()).build());
            int code = result.response().statusCode();
            if (!HTTPUtils.isSuccessful(code)) {
                if (HTTPUtils.isNotFound(code)) {
                    return false;
                }
                throw new IOException(HTTPClient.getResponseStatus(result));
            }
            return true;
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(source + "->" + target), e);
        }
    }

    /** Overrides {@link IProvider#getFileIfExists(String)} */
    @Override
    public ProviderFile getFileIfExists(String path) throws ProviderException {
        validatePrerequisites("getFileIfExists", path, "path");

        try {
            return createProviderFile(WebDAVProviderUtils.getResource(this, new URI(normalizePathEncoded(path))));
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#writeFile(String, String)}<br/>
     * 
     * @apiNote this method is implemented in the same way as http {@link HTTPProvider#writeFile(String,String)}.<br/>
     */
    @Override
    public void writeFile(String path, String content) throws ProviderException {
        writeFile(path, content, true);
    }

    /** Overrides {@link HTTPProvider#upload(String, InputStream, long, boolean)} */
    @Override
    public long upload(String path, InputStream source, long sourceSize) throws ProviderException {
        return upload(path, source, sourceSize, true);
    }

    public ProviderFile createProviderFile(WebDAVResource resource) {
        if (resource == null) {
            return null;
        }

        // TODO chunked?
        if (resource.getSize() < 0) {
            return null;
        }
        return createProviderFile(HTTPUtils.decode(resource.getURI()), resource.getSize(), resource.getLastModifiedInMillis());
        // return createProviderFile(resource.getURI().toString(), resource.getSize(), resource.getLastModifiedInMillis());
    }

}
