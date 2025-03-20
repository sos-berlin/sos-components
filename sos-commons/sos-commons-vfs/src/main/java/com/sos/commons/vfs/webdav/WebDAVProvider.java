package com.sos.commons.vfs.webdav;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map;

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
import com.sos.commons.vfs.http.HTTPProvider;
import com.sos.commons.vfs.http.commons.HTTPAuthConfig;
import com.sos.commons.vfs.http.commons.HTTPClient;
import com.sos.commons.vfs.http.commons.HTTPClient.ExecuteResult;
import com.sos.commons.vfs.http.commons.HTTPOutputStream;
import com.sos.commons.vfs.http.commons.HTTPUtils;
import com.sos.commons.vfs.webdav.commons.ProviderUtils;
import com.sos.commons.vfs.webdav.commons.WebDAVAuthMethod;
import com.sos.commons.vfs.webdav.commons.WebDAVProviderArguments;
import com.sos.commons.vfs.webdav.commons.WebDAVResource;
import com.sos.commons.vfs.webdav.commons.WebDAVSProviderArguments;

public class WebDAVProvider extends AProvider<WebDAVProviderArguments> {

    private URI baseURI;
    private HTTPClient client;

    public WebDAVProvider(ISOSLogger logger, WebDAVProviderArguments args) throws ProviderInitializationException {
        super(logger, args);
        try {
            // if baseURI not found, can be redefined when connecting
            baseURI = HTTPUtils.getBaseURI(getArguments().getHost(), getArguments().getPort());
            setAccessInfo(HTTPUtils.getAccessInfo(baseURI, getArguments().getUser().getValue()));
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

    /** Overrides {@link IProvider#renameFileIfSourceExists(String, String)} */
    @Override
    public boolean renameFileIfSourceExists(String source, String target) throws ProviderException {
        validatePrerequisites("renameFileIfSourceExists", source, "source");
        validateArgument("renameFileIfSourceExists", target, "target");
        try {
            URI sourceURI = new URI(normalizePath(source));
            URI targetURI = new URI(normalizePath(target));

            HttpRequest.Builder builder = client.createRequestBuilder(sourceURI);
            builder.header("Destination", targetURI.toString());
            ExecuteResult<Void> result = client.executeWithoutResponseBody(builder.method("MOVE", BodyPublishers.noBody()).build());
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
            return createProviderFile(ProviderUtils.getResource(client, new URI(normalizePath(path))));
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
     * @apiNote this method is implemented in the same way as http {@link HTTPProvider#writeFile(String,String)}.<br/>
     */
    @Override
    public void writeFile(String path, String content) throws ProviderException {
        validatePrerequisites("writeFile", path, "path");

        try {
            URI uri = new URI(normalizePath(path));

            ExecuteResult<Void> result = client.executePUT(uri, content, true);
            int code = result.response().statusCode();
            if (!HTTPUtils.isSuccessful(code)) {
                throw new IOException(HTTPClient.getResponseStatus(result));
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
     * @apiNote this method is implemented in the same way as http {@link HTTPProvider#getInputStream(String)}.<br/>
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
     * @apiNote this method is implemented in the same way http {@link HTTPProvider#getOutputStream(String,boolean)}.<br/>
     */
    @Override
    public OutputStream getOutputStream(String path, boolean append) throws ProviderException {
        validatePrerequisites("getOutputStream", path, "path");

        try {
            return new HTTPOutputStream(client, new URI(normalizePath(path)), true);
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

    public URI getBaseURI() {
        return baseURI;
    }

    public void setBaseURI(URI val) {
        baseURI = val;
    }

    public SSLArguments getSSLArguments() {
        return Protocol.WEBDAVS.equals(getArguments().getProtocol().getValue()) ? ((WebDAVSProviderArguments) getArguments()).getSSL() : null;
    }

    public HTTPAuthConfig getAuthConfig() {
        if (WebDAVAuthMethod.NTLM.equals(getArguments().getAuthMethod().getValue())) {
            return new HTTPAuthConfig(getLogger(), getArguments().getUser().getValue(), getArguments().getPassword().getValue(), getArguments()
                    .getWorkstation().getValue(), getArguments().getDomain().getValue());
        }
        // BASIC
        return new HTTPAuthConfig(getArguments().getUser().getValue(), getArguments().getPassword().getValue());
    }

    public HTTPClient getClient() {
        return client;
    }

    public ProviderFile createProviderFile(WebDAVResource resource) {
        if (resource == null) {
            return null;
        }

        // TODO chunked?
        if (resource.getSize() < 0) {
            return null;
        }
        return createProviderFile(resource.getHref(), resource.getSize(), resource.getLastModifiedInMillis());
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

    private void validatePrerequisites(String method, String argValue, String msg) throws ProviderException {
        validatePrerequisites(method);
        validateArgument(method, argValue, msg);
    }

}
