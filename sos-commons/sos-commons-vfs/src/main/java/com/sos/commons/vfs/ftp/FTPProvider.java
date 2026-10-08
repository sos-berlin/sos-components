package com.sos.commons.vfs.ftp;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPCmd;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPHTTPClient;
import org.apache.commons.net.ftp.FTPSClient;

import com.sos.commons.exception.SOSException;
import com.sos.commons.exception.SOSRequiredArgumentMissingException;
import com.sos.commons.util.SOSClassUtil;
import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSPathUtils;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.beans.SOSCommandResult;
import com.sos.commons.util.beans.SOSEnv;
import com.sos.commons.util.beans.SOSTimeout;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.util.proxy.ProxySocketFactory;
import com.sos.commons.util.socket.HostnamePreservingSocketFactory;
import com.sos.commons.vfs.commons.AProvider;
import com.sos.commons.vfs.commons.AProviderArguments.FileType;
import com.sos.commons.vfs.commons.AProviderArguments.Protocol;
import com.sos.commons.vfs.commons.IProvider;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.commons.vfs.commons.file.selection.ProviderFileSelection;
import com.sos.commons.vfs.exceptions.ProviderAuthenticationException;
import com.sos.commons.vfs.exceptions.ProviderClientNotInitializedException;
import com.sos.commons.vfs.exceptions.ProviderConnectException;
import com.sos.commons.vfs.exceptions.ProviderDirectoryCreationException;
import com.sos.commons.vfs.exceptions.ProviderException;
import com.sos.commons.vfs.exceptions.ProviderInitializationException;
import com.sos.commons.vfs.ftp.commons.FTPFTPSClient;
import com.sos.commons.vfs.ftp.commons.FTPFqdnFTPClient;
import com.sos.commons.vfs.ftp.commons.FTPProtocolCommandListener;
import com.sos.commons.vfs.ftp.commons.FTPProtocolReply;
import com.sos.commons.vfs.ftp.commons.FTPProviderArguments;
import com.sos.commons.vfs.ftp.commons.FTPProviderUtils;

/** TODO FTPS FileZilla<br/>
 * https://issues.apache.org/jira/browse/NET-408<br/>
 * - as Target (write files) - [425]425 Unable to build data connection: TLS session of data connection not resumed.<br/>
 * - as Source (list etc) - no result - Error: TLS session of data connection not resumed + 425<br/>
 * <br/>
 * TODO: FTP path handling with non-chrooted setup<br/>
 * - In a non-chrooted FTP server configuration (e.g. vsftpd), FTPClient.printWorkingDirectory() may return an absolute filesystem path<br/>
 * -- like "/home/vsftpd/myuser" instead of just "/".<br/>
 * -- This means paths such as "/my_dir" (which work with curl, assuming a chrooted FTP root)<br/>
 * --- may fail in Apache FTPClient because they are interpreted as absolute paths from the real filesystem root.<br/>
 * -- To ensure compatibility, always:<br/>
 * --- 1. Use printWorkingDirectory() to determine the session's FTP root.<br/>
 * --- 2. If it’s not "/", prepend or adjust other FTP paths accordingly.<br/>
 */
public class FTPProvider extends AProvider<FTPProviderArguments, Object> {

    private final boolean isFTPS;
    private final Object clientLock = new Object();
    private volatile FTPClient client;

    /** true<br/>
     * - automatically sends FEAT command before authentication...<br/>
     * -- can be a problem?<br/>
     * -- correctly internally sets UTF-8 if enabled<br/>
     * --- manually setting client.setControlEncoding(charsetUTF8) after login has no effect:<br/>
     * ---- apiNote: Please note that this has to be set before the connection is established.<br/>
     */
    private boolean autodetectUTF8Enabled = true;

    /** see {@link #setReadBufferSize(int)}, {@link #calculateWriteBufferSize(int)} */
    private int readBufferSize = 32 * 1_024; // 32KB
    private int writeBufferSize = readBufferSize * 2;

    public FTPProvider(ISOSLogger logger, FTPProviderArguments args) throws ProviderInitializationException {
        super(logger, args);
        isFTPS = Protocol.FTPS.equals(getArguments().getProtocol().getValue());
        setAccessInfo(args.getAccessInfo());
        if (logger.isDebugEnabled()) {
            args.getProtocolCommandListener().setValue(true);
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
        return SOSPathUtils.isAbsoluteUnixPath(path);
    }

    /** Overrides {@link IProvider#normalizePath(String)} */
    @Override
    public String normalizePath(String path) throws ProviderException {
        try {
            // do not use an absolute NIO path as this will add the Windows letter such as C:/ when YADE is running in a Windows environment.
            return toPathStyle(Path.of(path).normalize().toString());
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#connect()} */
    @Override
    public void connect() throws ProviderConnectException {
        if (SOSString.isEmpty(getArguments().getHost().getValue())) {
            throw new ProviderConnectException(new SOSRequiredArgumentMissingException("host"));
        }

        // All reply/responses are logged by the FTPProtocolCommandListener at the DEBUG level
        synchronized (clientLock) {
            try {
                getLogger().info(getConnectMsg());

                // create/connect FTP/FTPS client
                if (client == null) {
                    client = createClient();
                }
                // Connect
                client.connect(getArguments().getHost().getValue(), getArguments().getPort().getValue());

                FTPProtocolReply reply = new FTPProtocolReply(client);
                if (!reply.isPositiveReply()) {
                    throw new Exception(String.format("%s[connect][FTP server refused connection]%s", getLogPrefix(), reply));
                }
                postConnectOperations(client);

                // Login
                try {
                    client.login(getArguments().getUser().getValue(), getArguments().getPassword().getValue());
                } catch (IOException e) {
                    throw new ProviderAuthenticationException(e);
                }
                reply = new FTPProtocolReply(client);
                if (!reply.isPositiveReply()) {
                    throw new ProviderAuthenticationException(String.format("%s[login]%s", getLogPrefix(), reply));
                }

                postLoginOperations(client);

                getLogger().info(getConnectedMsg(getConnectedInfos(client)));
            } catch (ProviderConnectException e) {
                throwConnectException(e);
            } catch (Exception e) {
                throwConnectException(e);
            }
        }
    }

    @Override
    public String getConfiguredConnectInfos() {
        List<String> l = new ArrayList<>();
        if (!getArguments().getKeepAliveTimeout().isEmpty()) {
            l.add("KeepAliveTimeout=" + getArguments().getKeepAliveTimeout().getValue());
        }
        if (!isFTPS) {
            l.add(getArguments().getPassiveMode().getName() + "=" + getArguments().getPassiveMode().getValue());
        }
        l.add(getArguments().getTransferMode().getName() + "=" + getArguments().getTransferModeValue());

        return getConfiguredConnectInfos(l);
    }

    /** Overrides {@link IProvider#isConnected()} */
    @Override
    public boolean isConnected() {
        synchronized (clientLock) {
            if (client == null) {
                return false;
            }
            if (client.isConnected()) {
                sendNoOp();
            }
            return false;
        }
    }

    /** Overrides {@link IProvider#disconnect()} */
    @Override
    public void disconnect() {
        synchronized (clientLock) {
            if (client == null) {
                return;
            }

            try {
                client.logout();
            } catch (IOException e) {

            }
            try {
                client.disconnect();
                client = null;
            } catch (IOException e) {

            }
            getLogger().info(getDisconnectedMsg());
        }
    }

    /** Overrides {@link IProvider#injectConnectivityFault()} */
    @Override
    public void injectConnectivityFault() {
        synchronized (clientLock) {
            if (client != null) {
                try {
                    client.disconnect();
                    getLogger().info(getInjectConnectivityFaultMsg());
                } catch (IOException e) {
                    getLogger().info(getInjectConnectivityFaultMsg(e));
                }
            }
        }
    }

    /** Overrides {@link IProvider#selectFiles(ProviderFileSelection)} */
    @Override
    public List<ProviderFile> selectFiles(ProviderFileSelection selection) throws ProviderException {
        selection = ProviderFileSelection.createIfNull(getLogger(), selection);
        selection.setFileTypeChecker(fileRepresentator -> {
            if (fileRepresentator == null) {
                return false;
            }
            FTPFile r = (FTPFile) fileRepresentator;
            return (r.isFile() && getArguments().getValidFileTypes().getValue().contains(FileType.REGULAR)) || (r.isSymbolicLink() && getArguments()
                    .getValidFileTypes().getValue().contains(FileType.SYMLINK));
        });

        String directory = SOSString.isEmpty(selection.getConfig().getDirectory()) ? "/" : selection.getConfig().getDirectory();
        try {
            FTPClient client = requireFTPClient();
            if (!client.changeWorkingDirectory(directory)) {
                // the cause (not found, permissions etc) can't be clearly identified - throw a generic directory exception
                throwDirectoryException(directory, new FTPProtocolReply(client).toString());
            }

            List<ProviderFile> result = new ArrayList<>();
            FTPProviderUtils.selectFiles(this, selection, directory, result);
            return result;
        } catch (SocketException e) {
            throwConnectException(e);
            return null;
        } catch (ProviderException e) {
            throw e;
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(directory), e);
        }
    }

    /** Overrides {@link IProvider#exists(String)}<br/>
     * Uses the SIZE command<br/>
     * - alternative to listFiles<br/>
     * TODO - which is better?<br/>
     * -- which retrieves the information when, for example, the current user does not have the permissions to read the file but the file still exists... */
    @Override
    public boolean exists(String path) throws ProviderException {
        validateArgument("exists", path, "path");

        try {
            FTPClient client = requireFTPClient();
            client.sendCommand(FTPCmd.SIZE, path);
            FTPProtocolReply reply = new FTPProtocolReply(client);
            if (reply.isFileStatusReply()) {
                return true;
            }
            if (!reply.isFileUnavailableReply()) {
                if (!reply.isPositiveReply()) {
                    throw new ProviderException(getPathOperationPrefix(path) + reply);
                }
            }
            return false;
        } catch (ProviderException e) {
            throw e;
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#createDirectoriesIfNotExists(String)}<br/>
     * Check if exists - reverse order:<br/>
     * - /home/test/1/2/3<br/>
     * - /home/test/1/2<br/>
     * - /home/test/1<br/>
     * Creates:<br/>
     * - /home/test/1<br/>
     * - /home/test/1/2<br/>
     * - /home/test/1/2/3<br/>
     */
    @Override
    public boolean createDirectoriesIfNotExists(String path) throws ProviderDirectoryCreationException {

        try {
            validateArgument("createDirectoriesIfNotExists", path, "path");

            String dir = normalizePath(path);
            FTPClient client = requireFTPClient();
            if (client.changeWorkingDirectory(dir)) {
                return false; // already exists
            }

            Deque<String> parentsToCreate = new ArrayDeque<>();
            String parent = SOSPathUtils.getParentPath(dir, getPathSeparator());

            while (!SOSString.isEmpty(parent) && !parent.equals(dir) && !client.changeWorkingDirectory(parent)) {
                parentsToCreate.push(parent);
                parent = SOSPathUtils.getParentPath(parent, getPathSeparator());
            }
            // create parent directories
            while (!parentsToCreate.isEmpty()) {
                createDirectory(parentsToCreate.pop());
            }
            // create given directory
            createDirectory(path);
            return true;
        } catch (ProviderDirectoryCreationException e) {
            throw e;
        } catch (Exception e) {
            throwDirectoryCreationException(path, e);
            return false;
        }
    }

    /** Overrides {@link IProvider#deleteIfExists(String)} */
    @Override
    public boolean deleteIfExists(String path) throws ProviderException {
        validateArgument("deleteIfExists", path, "path");

        try {
            FTPClient client = requireFTPClient();
            FTPFile[] files = client.listFiles(path);
            FTPProtocolReply reply = new FTPProtocolReply(client);
            if (!reply.isPositiveReply()) {
                throw new ProviderException(getPathOperationPrefix(path) + reply.toString());
            }
            if (SOSCollection.isEmpty(files)) {
                return false;
            }

            FTPFile file = files[0];
            boolean deleted = false;
            if (file.isDirectory()) {
                FTPProviderUtils.deleteDirectoryFilesRecursively(client, getPathSeparator(), path);
                deleted = client.removeDirectory(path);
                if (!deleted) {
                    throw new ProviderException(getPathOperationPrefix(path) + "[failed to remove directory]" + new FTPProtocolReply(client));
                }
            } else if (file.isFile()) {
                deleted = client.deleteFile(path);
                if (!deleted) {
                    throw new ProviderException(getPathOperationPrefix(path) + "[failed to delete file]" + new FTPProtocolReply(client));
                }
            }
            return deleted;
        } catch (ProviderException e) {
            throw e;
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#deleteFileIfExists(String)} */
    @Override
    public boolean deleteFileIfExists(String path) throws ProviderException {
        validateArgument("deleteFileIfExists", path, "path");

        try {
            FTPClient client = requireFTPClient();
            if (!client.deleteFile(path)) {// not positive reply
                FTPProtocolReply reply = new FTPProtocolReply(client);
                if (isReplyBasedOnFileNotFound(reply, path)) {
                    return false;
                } else {
                    throw new ProviderException(getPathOperationPrefix(path) + "[failed]" + reply);
                }
            }
            return true;
        } catch (ProviderException e) {
            throw e;
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#moveFileIfExists(String, String)} */
    @Override
    public boolean moveFileIfExists(String source, String target) throws ProviderException {
        validateArgument("moveFileIfExists", source, "source");
        validateArgument("moveFileIfExists", target, "target");

        try {
            FTPClient client = requireFTPClient();
            if (!client.rename(source, target)) {// not positive reply
                FTPProtocolReply reply = new FTPProtocolReply(client);
                if (isReplyBasedOnFileNotFound(reply, target)) {
                    return false;
                } else {
                    throw new ProviderException(getPathOperationPrefix(source + "->" + target) + "[failed]" + reply);
                }
            }
            return true;
        } catch (ProviderException e) {
            throw e;
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(source + "->" + target), e);
        }
    }

    /** Overrides {@link IProvider#getFileIfExists(String)} */
    @Override
    public ProviderFile getFileIfExists(String path) throws ProviderException {
        validateArgument("getFileIfExists", path, "path");

        try {
            FTPClient client = requireFTPClient();
            return createProviderFile(path, FTPProviderUtils.getFTPFileIfExists("getFileIfExists", client, path));
        } catch (ProviderException e) {
            throw e;
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#getFileContentIfExists(String, String)} */
    @Override
    public String getFileContentIfExists(String path) throws ProviderException {
        validateArgument("getFileContentIfExists", path, "path");

        StringBuilder content = new StringBuilder();
        FTPProtocolReply reply = null;
        FTPClient client = requireFTPClient();
        try (InputStream is = client.retrieveFileStream(path)) {
            if (is == null) {
                return null;
            }
            try (Reader r = new InputStreamReader(is, StandardCharsets.UTF_8); BufferedReader br = new BufferedReader(r)) {
                br.lines().forEach(content::append);
                reply = new FTPProtocolReply(client);
                return client.completePendingCommand() ? content.toString() : null;
            }
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path) + (reply == null ? "" : reply), e);
        } finally {
            if (reply == null) {
                try {
                    client.completePendingCommand();
                } catch (Exception ex) {
                }
            }
        }
    }

    /** Overrides {@link IProvider#writeFile(String, String)} */
    @Override
    public void writeFile(String path, String content) throws ProviderException {
        validateArgument("writeFile", path, "path");

        FTPClient client = requireFTPClient();
        try (InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
            // Store file (overwrites if it exists)
            if (!client.storeFile(path, inputStream)) {
                throw new ProviderException(String.format("%s[failed to write file]%s", getPathOperationPrefix(path), new FTPProtocolReply(client)));
            }
        } catch (ProviderException e) {
            throw e;
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#setFileLastModifiedFromMillis(String, long)} */
    @Override
    // FTP: MFMT 20210127122653 /yade/target/test.txt
    public void setFileLastModifiedFromMillis(String path, long milliseconds) throws ProviderException {
        validateArgument("setFileLastModifiedFromMillis", path, path);
        validateModificationTime(path, milliseconds);

        try {
            FTPClient client = requireFTPClient();
            if (!client.setModificationTime(path, FTPProviderUtils.millisecondsToModificationTimeString(milliseconds))) {
                FTPProtocolReply mfmtReply = new FTPProtocolReply(client);

                String featReplyMessage;
                if (client.features()) {
                    FTPProtocolReply featuresReply = new FTPProtocolReply(client);
                    featReplyMessage = String.format("[FEAT]Server supports the following features: %s", featuresReply);
                } else {
                    featReplyMessage =
                            "[FEAT]Server was queried to check support for the MFMT command, but did not respond with any supported features. It is likely that MFMT is not supported.";
                }
                throw new ProviderException(String.format("%s[failed][MFMT]%s %s", getPathOperationPrefix(path), mfmtReply, featReplyMessage));
            }
        } catch (ProviderException e) {
            throw e;
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#supportsReadOffset()} */
    public boolean supportsReadOffset() {
        return true;
    }

    /** Overrides {@link IProvider#getInputStream(String)} */
    @Override
    public InputStream getInputStream(String path) throws ProviderException {
        return getInputStream(path, 0L);
    }

    /** Overrides {@link IProvider#getInputStream(String, long)} */
    @Override
    public InputStream getInputStream(String path, long offset) throws ProviderException {
        validateArgument("getInputStream", path, "path");

        try {
            // if requireFTPClient() used - it not blocks the connectivity fault simulation execution
            FTPClient client = requireFTPClient();
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("%s[getInputStream][supportsReadOffset=%s, offset=%s]%s", getLogPrefix(), supportsReadOffset(), offset, path);
            }

            if (offset > 0) {
                // client.setRestartOffset docs: ... The restart marker is reset to zero after use...
                // so - no manually reset needed
                client.setRestartOffset(offset);
            }
            InputStream is = client.retrieveFileStream(path);
            if (is == null) {
                throw new ProviderException(String.format("%s[failed to open InputStream]%s", getPathOperationPrefix(path), new FTPProtocolReply(
                        client)));
            }
            // FilterInputStream - forwards all calls directly, no extra processing, no overhead
            return new FilterInputStream(is) {

                private final AtomicBoolean closed = new AtomicBoolean(false);

                @Override
                public void close() throws IOException {
                    if (closed.getAndSet(true)) {
                        return;
                    }

                    IOException exception = null;
                    // 1) close stream - super.close() closes "is"
                    try {
                        super.close();
                    } catch (IOException e) {
                        exception = SOSException.mergeException(exception, e);
                    }
                    // 2) check completion
                    try {
                        boolean completed = client.completePendingCommand(); // can self thrown an IOException
                        if (!completed) {
                            exception = SOSException.mergeException(exception, new IOException(new FTPProtocolReply(client).toString()));
                            if (getLogger().isDebugEnabled()) {
                                getLogger().debug("%s[getInputStream.close][%s][completed=false]%s", getLogPrefix(), path, exception);
                            }
                        } else {
                            if (getLogger().isDebugEnabled()) {
                                getLogger().debug("%s[getInputStream.close][%s]completed=true", getLogPrefix(), path);
                            }
                        }
                    } catch (IOException e) {
                        exception = SOSException.mergeException(exception, e);
                    } finally {
                        // reset restart offset because the docs (see above) do not guarantee (unclear) it on error ...
                        client.setRestartOffset(0L);
                    }

                    if (exception != null) {
                        throw exception;
                    }
                }
            };
        } catch (ProviderException e) {
            throw e;
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#getOutputStream(String, boolean)} */
    @Override
    // FTP: APPE /yade/target/test.txt
    // FTP: STOR /yade/target/test.txt
    public OutputStream getOutputStream(String path, boolean append) throws ProviderException {
        validateArgument("getOutputStream", path, "path");

        try {
            // if requireFTPClient() used - it blocks the connectivity fault simulation execution
            FTPClient client = getArguments().isConnectivityFaultSimulationEnabled() ? this.client : requireFTPClient();
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("%s[getOutputStream][append=%s]%s", getLogPrefix(), append, path);
            }

            OutputStream os = append ? client.appendFileStream(path) : client.storeFileStream(path);
            if (os == null) {
                throw new ProviderException(String.format("%s[failed to open OutputStream]%s", getPathOperationPrefix(path), new FTPProtocolReply(
                        client)));
            }

            // Use BufferedOutputStream instead of FilterOutputStream, because FilterOutputStream is too slow
            // (it recalculates and delegates every byte internally, causing overhead)
            return new BufferedOutputStream(os, writeBufferSize) {

                private final AtomicBoolean closed = new AtomicBoolean(false);

                @Override
                public void close() throws IOException {
                    if (closed.getAndSet(true)) {
                        return;
                    }

                    IOException exception = null;

                    /** 1) Flush buffered data before shutting down the TLS output. */
                    try {
                        flush();
                    } catch (IOException e) {
                        exception = SOSException.mergeException(exception, e);
                    }

                    /** 2) For FTPS, shut down the TLS output before closing the stream.<br />
                     * This prevents the TLSv1.3 "user_canceled" alert during SSLSocket.close().<br />
                     * Without this explicit shutdown, FileZilla Server may report:<br/>
                     * - [Error] Received TLS alert from the client: User canceled (90) and the transfer may fail with:<br />
                     * - [425] Error while downloading data: ECONNABORTED - Connection aborted.
                     * 
                     * For TLSv1.2, this explicit shutdown is not required because the underlying Java socket implementation handles the connection shutdown
                     * differently. Calling dataSslSocketShutdownOutput() is safe for both TLSv1.2 and TLSv1.3. */
                    if (isFTPS) {
                        try {
                            ((FTPFTPSClient) client).dataSslSocketShutdownOutput();
                        } catch (IOException e) {
                            exception = SOSException.mergeException(exception, e);
                        }
                    }

                    /** 3) Close the stream. super.close() closes "os". */
                    try {
                        super.close();
                    } catch (IOException e) {
                        exception = SOSException.mergeException(exception, e);
                    }

                    /** 4) Check whether the FTP transfer completed successfully. */
                    try {
                        boolean completed = client.completePendingCommand(); // can thrown an IOException
                        if (!completed) {
                            exception = SOSException.mergeException(exception, new IOException(new FTPProtocolReply(client).toString()));
                            if (getLogger().isDebugEnabled()) {
                                getLogger().debug("%s[getOutputStream.close][%s][completed=false]%s", getLogPrefix(), path, exception);
                            }
                        } else {
                            if (getLogger().isDebugEnabled()) {
                                getLogger().debug("%s[getOutputStream.close][%s]completed=true", getLogPrefix(), path);
                            }
                        }
                    } catch (IOException e) {
                        exception = SOSException.mergeException(exception, e);
                    }

                    if (exception != null) {
                        throw exception;
                    }
                }
            };
        } catch (ProviderException e) {
            throw e;
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#executeCommand(String, SOSTimeout, SOSEnv)} */
    @Override
    public SOSCommandResult executeCommand(String command, SOSTimeout timeout, SOSEnv env) {
        synchronized (clientLock) {
            SOSCommandResult result = new SOSCommandResult(command);
            if (client == null) {
                return result;
            }

            try {
                if (timeout != null) {
                    client.setControlKeepAliveTimeout(timeout.toDuration());
                }
                client.sendCommand(command);
                FTPProtocolReply reply = new FTPProtocolReply(client);
                if (!reply.isPositiveReply()) {
                    throw new Exception(reply.toString());
                }
                result.setStdOut(reply.getText());
            } catch (Exception e) {
                result.setException(e);
            } finally {
                if (timeout != null) {
                    // restore configured Keep Alive
                    client.setControlKeepAliveTimeout(getKeepAliveTimeout());
                }
            }
            return result;
        }
    }

    /** Overrides {@link IProvider#cancelCommands()} */
    @Override
    public SOSCommandResult cancelCommands() {
        // TODO Auto-generated method stub
        return null;
    }

    public void debugCommand(String command) {
        if (!getLogger().isDebugEnabled() || getArguments().getProtocolCommandListener().isTrue()) {
            return;
        }
        getLogger().debug("%s[%s]%s", getLogPrefix(), command, new FTPProtocolReply(client));
    }

    public ProviderFile createProviderFile(String path, FTPFile file) {
        if (file == null) {
            return null;
        }
        return createProviderFile(path, file.getSize(), file.getTimestamp() == null ? -1L : file.getTimestamp().getTimeInMillis());
    }

    public FTPClient requireFTPClient() throws ProviderException {
        synchronized (clientLock) {
            if (client == null) {
                // 0 - getStackTrace
                // 1 - requireClient
                // 2 - caller
                throw new ProviderClientNotInitializedException(getLogPrefix(), FTPClient.class, SOSClassUtil.getMethodName(2));
            }
            return client;
        }
    }

    /** Attempt to determine if NOT_FOUND is truly the cause in the case of FTPReply.FILE_UNAVAILABLE, rather than issues like permissions, etc. */
    public boolean isReplyBasedOnFileNotFound(FTPProtocolReply reply, String path) throws ProviderException {
        if (reply.isFileUnavailableReply()) {
            // Reply text is not analyzed due to different implementations/languages
            return exists(path);
        }
        return false;
    }

    public void setReadBufferSize(int val) {
        readBufferSize = val;
        calculateWriteBufferSize(readBufferSize);
    }

    private FTPClient createClient() throws Exception {
        FTPClient client = isFTPS ? FTPFTPSClient.create(this) : createFTPClient();
        applyPreConnectSettings(client);
        return client;
    }

    private FTPClient createFTPClient() throws Exception {
        FTPClient client = null;
        if (getProxyConfig() == null) {
            client = new FTPClient();
        } else {
            if (getProxyConfig().isSOCKS()) { // SOCKS PROXY
                ProxySocketFactory factory = null;
                if (getProxyConfig().shouldResolveSocksHostname()) {
                    client = new FTPClient();
                    factory = new ProxySocketFactory(getProxyConfig());
                } else {
                    String host = getArguments().getHost().getValue();
                    client = new FTPFqdnFTPClient(host);
                    factory = new ProxySocketFactory(new HostnamePreservingSocketFactory(getLogger(), host, getProxyConfig().getProxy()));
                }
                client.setSocketFactory(factory);
            } else { // HTTP PROXY
                if (SOSString.isEmpty(getProxyConfig().getUser())) {
                    client = new FTPHTTPClient(getProxyConfig().getHost(), getProxyConfig().getPort());
                } else {
                    client = new FTPHTTPClient(getProxyConfig().getHost(), getProxyConfig().getPort(), getProxyConfig().getUser(), getProxyConfig()
                            .getPassword());
                }
            }
        }
        return client;
    }

    private void applyPreConnectSettings(FTPClient client) {
        setProtocolCommandListener(client);
        client.setConnectTimeout(getArguments().getConnectTimeoutAsMillis());
        // setDefaultTimeout -
        // client.setDefaultTimeout(client.getConnectTimeout());
        // setDataTimeout - Sets the timeout to use when reading from the data connection.
        // - This timeout will be set immediately after opening the data connection, provided that thevalue is ≥ 0.
        // - client.setDataTimeout(Duration.ofMillis(client.getConnectTimeout()));

        client.setAutodetectUTF8(autodetectUTF8Enabled);
    }

    private void postConnectOperations(FTPClient client) throws Exception {
        // Keep Alive
        client.setControlKeepAliveTimeout(getKeepAliveTimeout());
    }

    /** 1) Prepare passive mode.<br />
     * - See {@link FTPFTPSClient#_openDataConnection_} , which sends EPSV/PASV and prefers EPSV for passive connections.<br />
     * -- EPSV is required for IPv6 and is also preferred for IPv4.<br />
     * 
     * 2) Encrypt the data connection. */
    private void postLoginOperationsIfFTPS(FTPClient client) throws Exception {
        if (isFTPS) {
            // 1) Prepare passive mode.
            client.enterLocalPassiveMode();
            client.setUseEPSVwithIPv4(true);

            // 2) Encrypt the data connection.
            FTPSClient ftps = (FTPSClient) client;
            try {
                ftps.execPBSZ(0);
                debugCommand("execPBSZ(0)"); // configure protection for the data connection (set the protection buffer size to 0)
            } catch (Exception e) {
                getLogger().warn("[execPBSZ(0)]" + e);
            }
            try {
                ftps.execPROT("P"); // encrypt the data connection (the control connection is already protected by TLS)
                debugCommand("execPROT(P)");
            } catch (Exception e) {
                getLogger().warn("[execPROT(P)]" + e);
            }

            if (getLogger().isDebugEnabled()) {
                getLogger().debug("%s[getEnabledProtocols]%s", getLogPrefix(), Arrays.asList(((FTPSClient) client).getEnabledProtocols()));
            }
        }
    }

    /** Sets passive mode if enabled.<br />
     * - Sends EPSV/PASV and prefers EPSV for passive connections.<br />
     * -- EPSV is required for IPv6 and is also preferred for IPv4.<br />
     * 
     * @implNote PASV may be rejected when the FTP server sees the control connection as IPv6.<br />
     *           This can also occur when the client uses IPv4 but the SOCKS proxy establishes the connection to the FTP server over IPv6.<br />
     *           For example, FileZilla reports: 500 You are connected using IPv6. PASV is only for IPv4. You have to use the EPSV command instead.
     * 
     * @param client
     * @throws Exception */
    private void postLoginOperationsIfFTP(FTPClient client) throws Exception {
        // Passive Mode
        if (getArguments().getPassiveMode().isTrue()) {
            client.enterLocalPassiveMode();
            client.setUseEPSVwithIPv4(true);

            String command = "EPSV";
            client.epsv();
            FTPProtocolReply reply = new FTPProtocolReply(client); // a positive EPSV reply contains the data connection port, e.g. "|||54227|".
            if (!reply.isPositiveReply()) {
                String firstNegativeReplay = reply.toString();

                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("%s[%s=true][%s][failed]%s", getLogPrefix(), getArguments().getPassiveMode().getName(), command,
                            firstNegativeReplay);
                    getLogger().debug("%s[%s=true]sent PASV ...", getLogPrefix(), getArguments().getPassiveMode().getName());
                }

                command = "PASV";
                client.pasv();
                reply = new FTPProtocolReply(client);
                if (!reply.isPositiveReply()) {
                    throw new ProviderException(String.format("%s[%s=true]EPSV=%s, PASV=%s", getLogPrefix(), getArguments().getPassiveMode()
                            .getName(), firstNegativeReplay, reply));
                }
            }
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("%s[%s=true][%s]%s", getLogPrefix(), getArguments().getPassiveMode().getName(), command, reply);
            }
        }
    }

    private void postLoginOperations(FTPClient client) throws Exception {
        /** FTP/FTPS */
        features(client);

        postLoginOperationsIfFTPS(client);
        postLoginOperationsIfFTP(client);

        /** FTP/FTPS */
        // Transfer Mode
        if (getArguments().isBinaryTransferMode()) {
            if (!client.setFileType(FTP.BINARY_FILE_TYPE)) {
                throw new ProviderException(String.format("%s[%s]%s", getLogPrefix(), getArguments().getTransferMode().getValue(),
                        new FTPProtocolReply(client)));
            }
        } else {
            if (!client.setFileType(FTP.ASCII_FILE_TYPE)) {
                throw new ProviderException(String.format("%s[%s]%s", getLogPrefix(), getArguments().getTransferMode().getValue(),
                        new FTPProtocolReply(client)));
            }
        }
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("%s[%s=%s]%s", getLogPrefix(), getArguments().getTransferMode().getName(), getArguments().getTransferModeValue(),
                    new FTPProtocolReply(client));
        }

        sendNoOp();
    }

    // https://change.sos-berlin.com/browse/YADE-645
    private void sendNoOp() {
        try {
            client.sendNoOp();// NOOP command
        } catch (IOException e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("%s[sendNoOp][exception]%s", getLogPrefix(), e);
            }
        }
    }

    // see notes: autodetectUTF8Enabled
    private void features(FTPClient client) {
        if (!autodetectUTF8Enabled) {
            // apiNote: Please note that this has to be set before the connection is established.
            // client.setControlEncoding(charsetUTF8);
            try {
                String charsetUTF8 = StandardCharsets.UTF_8.name();
                if (client.hasFeature("UTF8") || client.hasFeature(charsetUTF8)) {
                    client.setControlEncoding(charsetUTF8);
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("%s[setControlEncoding]%s", getLogPrefix(), charsetUTF8);
                    }
                }
            } catch (IOException e) {
                getLogger().debug("%s[setControlEncoding][FEAT][exception]%s", getLogPrefix(), e);
            }
        }
    }

    private String getConnectedInfos(FTPClient client) {
        if (client == null) {
            return "";
        }
        List<String> l = new ArrayList<String>();
        if (client.getConnectTimeout() > 0) {
            l.add("ConnectTimeout=" + AProvider.millis2string(client.getConnectTimeout()));
        }
        if (client.getControlKeepAliveTimeoutDuration() != null) {
            // l.add("KeepAliveInterval=" + getArguments().getServerAliveInterval().getValue() + "s");
            // ControlKeepAliveTimeoutDuration is set from getServerAliveInterval
            l.add("KeepAliveTimeout=" + SOSDate.getDuration(client.getControlKeepAliveTimeoutDuration()));
            // TODO ControlKeepAliveReplyTimeoutDuration is currently not configurable
            // client.getControlKeepAliveReplyTimeoutDuration();
        }
        if (!isFTPS) {
            l.add(getArguments().getPassiveMode().getName() + "=" + getArguments().getPassiveMode().getValue());
        }
        l.add(getArguments().getTransferMode().getName() + "=" + getArguments().getTransferModeValue());

        String serverInfo = getServerInfo();
        return SOSString.isEmpty(serverInfo) ? String.join(", ", l) : ("[" + serverInfo + "]" + String.join(", ", l));
    }

    private String getServerInfo() {
        try {
            return client.getSystemType();
        } catch (IOException e) {
            return "";
        }
    }

    private Duration getKeepAliveTimeout() {
        return Duration.ofSeconds(getArguments().getKeepAliveTimeoutAsSeconds());
    }

    private void setProtocolCommandListener(FTPClient client) {
        if (getArguments().getProtocolCommandListener().isTrue() || FTPProviderUtils.isCommandListenerEnvVarSet()) {
            client.addProtocolCommandListener(new FTPProtocolCommandListener(getLogger(), getLogPrefix()));
            getLogger().debug(getLogPrefix() + "ProtocolCommandListener added");
        }
    }

    private void createDirectory(String path) throws Exception {
        if (!client.makeDirectory(path)) {
            throw new Exception(String.format("%s[failed to create directory][%s]%s", getLogPrefix(), path, new FTPProtocolReply(client)));
        }
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("%s[createDirectory][%s]created", getLogPrefix(), path);
        }
    }

    public static int calculateWriteBufferSize(int inputBufferSize) {
        final int MIN = 32 * 1024;   // 32 KB
        final int MAX = 256 * 1024;  // 256 KB

        // 2x input buffer, but bounded
        int writeBufferSize = inputBufferSize * 2;

        if (writeBufferSize < MIN) {
            return MIN;
        }
        if (writeBufferSize > MAX) {
            return MAX;
        }
        return writeBufferSize;
    }

}
