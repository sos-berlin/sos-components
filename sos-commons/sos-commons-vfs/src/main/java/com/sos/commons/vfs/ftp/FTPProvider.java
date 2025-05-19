package com.sos.commons.vfs.ftp;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPCmd;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPHTTPClient;
import org.apache.commons.net.ftp.FTPSClient;

import com.sos.commons.exception.SOSRequiredArgumentMissingException;
import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSPathUtils;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.beans.SOSCommandResult;
import com.sos.commons.util.beans.SOSEnv;
import com.sos.commons.util.beans.SOSTimeout;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.util.proxy.socket.ProxySocketFactory;
import com.sos.commons.util.ssl.SOSSSLContextFactory;
import com.sos.commons.vfs.commons.AProvider;
import com.sos.commons.vfs.commons.AProviderArguments.FileType;
import com.sos.commons.vfs.commons.AProviderArguments.Protocol;
import com.sos.commons.vfs.commons.IProvider;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.commons.vfs.commons.file.selection.ProviderFileSelection;
import com.sos.commons.vfs.exceptions.ProviderAuthenticationException;
import com.sos.commons.vfs.exceptions.ProviderClientNotInitializedException;
import com.sos.commons.vfs.exceptions.ProviderConnectException;
import com.sos.commons.vfs.exceptions.ProviderException;
import com.sos.commons.vfs.exceptions.ProviderInitializationException;
import com.sos.commons.vfs.exceptions.ProviderNoSuchFileException;
import com.sos.commons.vfs.ftp.commons.FTPProtocolCommandListener;
import com.sos.commons.vfs.ftp.commons.FTPProtocolReply;
import com.sos.commons.vfs.ftp.commons.FTPProviderArguments;
import com.sos.commons.vfs.ftp.commons.FTPProviderUtils;
import com.sos.commons.vfs.ftp.commons.FTPSProviderArguments;

/** TODO FTPS FileZilla<br/>
 * https://issues.apache.org/jira/browse/NET-408<br/>
 * - as Target (write files) - [425]425 Unable to build data connection: TLS session of data connection not resumed.<br/>
 * - as Source (list etc) - no result - Error: TLS session of data connection not resumed + 425<br/>
 */
public class FTPProvider extends AProvider<FTPProviderArguments> {

    private final boolean isFTPS;
    private FTPClient client;

    /** true<br/>
     * - automatically sends FEAT command before authentication...<br/>
     * -- can be a problem?<br/>
     * -- correctly internally sets UTF-8 if enabled<br/>
     * --- manually setting client.setControlEncoding(charsetUTF8) after login has no effect:<br/>
     * ---- apiNote: Please note that this has to be set before the connection is established.<br/>
     */
    private boolean autodetectUTF8Enabled = true;

    public FTPProvider(ISOSLogger logger, FTPProviderArguments args) throws ProviderInitializationException {
        super(logger, args);
        isFTPS = Protocol.FTPS.equals(getArguments().getProtocol().getValue());
        setAccessInfo(args.getAccessInfo());
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
    public String normalizePath(String path) {
        // do not use an absolute NIO path as this will add the Windows letter such as C:/ when YADE is running in a Windows environment.
        return toPathStyle(Path.of(path).normalize().toString());
    }

    /** Overrides {@link IProvider#connect()} */
    @Override
    public void connect() throws ProviderConnectException {
        if (SOSString.isEmpty(getArguments().getHost().getValue())) {
            throw new ProviderConnectException(new SOSRequiredArgumentMissingException("host"));
        }
        try {
            getLogger().info(getConnectMsg());

            // create/connect FTP/FTPS client
            client = createClient();
            // Connect
            client.connect(getArguments().getHost().getValue(), getArguments().getPort().getValue());
            FTPProtocolReply reply = new FTPProtocolReply(client);
            if (!reply.isPositiveReply()) {
                throw new Exception(String.format("%s[connect][FTP server refused connection]%s", getLogPrefix(), reply));
            }
            postConnectOperations();

            // Login
            try {
                client.login(getArguments().getUser().getValue(), getArguments().getPassword().getValue());
            } catch (IOException e) {
                throw new ProviderAuthenticationException(e);
            }
            reply = new FTPProtocolReply(client);
            if (!reply.isPositiveReply()) {
                throw new Exception(String.format("%s[login]%s", getLogPrefix(), reply));
            }

            postLoginOperations();

            getLogger().info(getConnectedMsg(getConnectedInfos()));
        } catch (Throwable e) {
            if (isConnected()) {
                disconnect();
            }
            throw new ProviderConnectException(String.format("[%s]", getAccessInfo()), e);
        }

    }

    /** Overrides {@link IProvider#isConnected()} */
    @Override
    public boolean isConnected() {
        if (client == null) {
            return false;
        }
        if (client.isConnected()) {
            try {
                client.sendNoOp();// NOOP command
                return true;
            } catch (IOException e) {
            }
        }
        return false;
    }

    /** Overrides {@link IProvider#disconnect()} */
    @Override
    public void disconnect() {
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

    /** Overrides {@link IProvider#selectFiles(ProviderFileSelection)} */
    @Override
    public List<ProviderFile> selectFiles(ProviderFileSelection selection) throws ProviderException {
        validatePrerequisites("selectFiles");

        selection = ProviderFileSelection.createIfNull(selection);
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
            if (!client.changeWorkingDirectory(directory)) {
                throw new ProviderNoSuchFileException(getDirectoryNotFoundMsg(directory));
            }

            List<ProviderFile> result = new ArrayList<>();
            FTPProviderUtils.selectFiles(this, selection, directory, result);
            return result;
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
        validatePrerequisites("exists", path, "path");

        try {
            client.sendCommand(FTPCmd.SIZE, path);
            FTPProtocolReply reply = new FTPProtocolReply(client);
            if (reply.isFileStatusReply()) {
                return true;
            }
            if (!reply.isFileUnavailableReply()) {
                if (!reply.isPositiveReply()) {
                    throw new Exception(String.format("%s[exists]%s", getLogPrefix(), reply));
                }
            }
            return false;
        } catch (Throwable e) {
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
    public boolean createDirectoriesIfNotExists(String path) throws ProviderException {
        validatePrerequisites("createDirectoriesIfNotExists", path, "path");

        try {
            String dir = normalizePath(path);
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
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#deleteIfExists(String)} */
    @Override
    public boolean deleteIfExists(String path) throws ProviderException {
        validatePrerequisites("deleteIfExists", path, "path");

        try {
            FTPFile[] files = client.listFiles(path);
            FTPProtocolReply reply = new FTPProtocolReply(client);
            if (!reply.isPositiveReply()) {
                throw new IOException(reply.toString());
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
                    throw new Exception(String.format("[failed to remove directory][%s]%s", path, new FTPProtocolReply(client)));
                }
            } else if (file.isFile()) {
                deleted = client.deleteFile(path);
                if (!deleted) {
                    throw new Exception(String.format("[failed to delete file][%s]%s", path, new FTPProtocolReply(client)));
                }
            }
            return deleted;
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#deleteFileIfExists(String)} */
    @Override
    public boolean deleteFileIfExists(String path) throws ProviderException {
        validatePrerequisites("deleteFileIfExists", path, "path");
        try {

            if (!client.deleteFile(path)) {// not positive reply
                FTPProtocolReply reply = new FTPProtocolReply(client);
                if (isReplyBasedOnFileNotFound(reply, path)) {
                    return false;
                } else {
                    throw new Exception(String.format("[failed]%s", reply));
                }
            }
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
            if (!client.rename(source, target)) {// not positive reply
                FTPProtocolReply reply = new FTPProtocolReply(client);
                if (isReplyBasedOnFileNotFound(reply, target)) {
                    return false;
                } else {
                    throw new Exception(String.format("[failed]%s", reply));
                }
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
            return createProviderFile(path, FTPProviderUtils.getFTPFile("getFileIfExists", client, path));
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#getFileContentIfExists(String, String)} */
    @Override
    public String getFileContentIfExists(String path) throws ProviderException {
        validatePrerequisites("getFileContentIfExists", path, "path");

        StringBuilder content = new StringBuilder();
        FTPProtocolReply reply = null;
        try (InputStream is = client.retrieveFileStream(path)) {
            if (is == null) {
                return null;
            }
            try (Reader r = new InputStreamReader(is, StandardCharsets.UTF_8); BufferedReader br = new BufferedReader(r)) {
                br.lines().forEach(content::append);
                reply = new FTPProtocolReply(client);
                return client.completePendingCommand() ? content.toString() : null;
            }
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path) + (reply == null ? "" : reply), e);
        } finally {
            if (reply == null) {
                try {
                    client.completePendingCommand();
                } catch (IOException ex) {
                }
            }
        }
    }

    /** Overrides {@link IProvider#writeFile(String, String)} */
    @Override
    public void writeFile(String path, String content) throws ProviderException {
        validatePrerequisites("writeFile", path, "path");

        try (InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
            // Store file (overwrites if it exists)
            if (!client.storeFile(path, inputStream)) {
                throw new ProviderException(String.format("%s[failed to write file]%s", getPathOperationPrefix(path), new FTPProtocolReply(client)));
            }
        } catch (ProviderException e) {
            throw e;
        } catch (IOException e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#setFileLastModifiedFromMillis(String, long)} */
    @Override
    // FTP: MFMT 20210127122653 /yade/target/test.txt
    public void setFileLastModifiedFromMillis(String path, long milliseconds) throws ProviderException {
        validatePrerequisites("setFileLastModifiedFromMillis", path, path);
        validateModificationTime(path, milliseconds);

        try {
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
        } catch (IOException e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#getInputStream(String)} */
    @Override
    public InputStream getInputStream(String path) throws ProviderException {
        validatePrerequisites("getInputStream", path, "path");

        try {
            InputStream is = client.retrieveFileStream(path);
            if (is == null) {
                throw new ProviderException(String.format("%s[failed to open InputStream]%s", getPathOperationPrefix(path), new FTPProtocolReply(
                        client)));
            }
            return is;
        } catch (ProviderException e) {
            throw e;
        } catch (IOException e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#onInputStreamClosed(String)} */
    @Override
    public void onInputStreamClosed(String path) throws ProviderException {
        validatePrerequisites("onInputStreamClosed", path, "path");

        try {
            if (!client.completePendingCommand()) {
                throw new IOException(new FTPProtocolReply(client).toString());
            }
        } catch (IOException e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#getOutputStream(String, boolean)} */
    @Override
    // FTP: APPE /yade/target/test.txt
    // FTP: STOR /yade/target/test.txt
    public OutputStream getOutputStream(String path, boolean append) throws ProviderException {
        validatePrerequisites("getOutputStream", path, "path");

        try {
            // return new FTPOutputStream(client, normalizePath(path), append);
            return append ? client.appendFileStream(path) : client.storeFileStream(path);
        } catch (IOException e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#onOutputStreamClosed(String)} */
    @Override
    public void onOutputStreamClosed(String path) throws ProviderException {
        validatePrerequisites("onOutputStreamClosed", path, "path");

        try {
            if (!client.completePendingCommand()) {
                throw new IOException(new FTPProtocolReply(client).toString());
            }
        } catch (IOException e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#executeCommand(String, SOSTimeout, SOSEnv)} */
    @Override
    public SOSCommandResult executeCommand(String command, SOSTimeout timeout, SOSEnv env) {
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
        } catch (Throwable e) {
            result.setException(e);
        } finally {
            if (timeout != null) {
                // restore configured Keep Alive
                client.setControlKeepAliveTimeout(getKeepAliveTimeout());
            }
        }
        return result;
    }

    /** Overrides {@link IProvider#cancelCommands()} */
    @Override
    public SOSCommandResult cancelCommands() {
        // TODO Auto-generated method stub
        return null;
    }

    /** Overrides {@link AProvider#validatePrerequisites(String)} */
    @Override
    public void validatePrerequisites(String method) throws ProviderException {
        if (client == null) {
            throw new ProviderClientNotInitializedException(getLogPrefix() + method + "FTPClient");
        }
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

    public FTPClient getClient() {
        return client;
    }

    /** Attempt to determine if NOT_FOUND is truly the cause in the case of FTPReply.FILE_UNAVAILABLE, rather than issues like permissions, etc. */
    public boolean isReplyBasedOnFileNotFound(FTPProtocolReply reply, String path) throws ProviderException {
        if (reply.isFileUnavailableReply()) {
            // Reply text is not analyzed due to different implementations/languages
            return exists(path);
        }
        return false;
    }

    private FTPClient createClient() throws Exception {
        FTPClient client = isFTPS ? createFTPSClient() : createFTPClient();
        applyPreConnectSettings(client);
        return client;
    }

    private FTPClient createFTPClient() throws Exception {
        FTPClient client = null;
        if (getProxyProvider() == null) {
            client = new FTPClient();
        } else {
            // SOCKS PROXY
            if (java.net.Proxy.Type.SOCKS.equals(getProxyProvider().getProxy().type())) {
                client = new FTPClient();
                client.setSocketFactory(new ProxySocketFactory(getProxyProvider()));
            }
            // HTTP PROXY
            else {
                if (getProxyProvider().getUser().isEmpty()) {
                    client = new FTPHTTPClient(getProxyProvider().getHost(), getProxyProvider().getPort());
                } else {
                    client = new FTPHTTPClient(getProxyProvider().getHost(), getProxyProvider().getPort(), getProxyProvider().getUser(),
                            getProxyProvider().getPassword());
                }
            }
        }
        return client;
    }

    // FileZilla - [425]425 Unable to build data connection: TLS session of data connection not resumed.
    private FTPClient createFTPSClient() throws Exception {
        // System.setProperty("jdk.tls.client.protocols", "TLSv1.2");
        // System.setProperty("jdk.tls.allowLegacyResumption", "true");
        // System.setProperty("jdk.tls.useExtendedMasterSecret", "false");
        // System.setProperty("jdk.tls.client.enableSessionTicketExtension", "false");
        FTPSProviderArguments args = (FTPSProviderArguments) getArguments();
        FTPSClient client = null;
        if (args.getSSL().getTrustedSSL().isEnabled()) {
            // tmp
            // args.getSSL().getProtocols().setValue(List.of("TLSv1.2"));
            // if (!args.getSSL().getJavaKeyStore().isEnabled()) {
            // args.getSSL().getAcceptUntrustedCertificate().setValue(true);
            // }
            client = new FTPSClient(args.isSecurityModeImplicit(), SOSSSLContextFactory.create(getLogger(), args.getSSL()));
        } else {
            client = new FTPSClient(args.isSecurityModeImplicit());
            if (!args.getSSL().getUntrustedSSLVerifyCertificateHostname().isTrue()) {
                client.setHostnameVerifier(null);
                logIfHostnameVerificationDisabled(args.getSSL());
            }
        }

        if (getProxyProvider() != null) {
            client.setProxy(getProxyProvider().getProxy());
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

    private void postConnectOperations() throws Exception {
        // Keep Alive
        client.setControlKeepAliveTimeout(getKeepAliveTimeout());
    }

    private void postLoginOperations() throws Exception {
        features();

        postLoginOperationsIfFTPS();

        /** FTP/FTPS */
        // Passive Mode
        if (getArguments().getPassiveMode().isTrue()) {
            client.pasv();
            FTPProtocolReply reply = new FTPProtocolReply(client);
            if (reply.isPositiveReply()) {
                client.enterLocalPassiveMode();// TODO check - FTPS below
            } else {
                throw new ProviderException(String.format("%s[pasv]%s", getLogPrefix(), reply));
            }
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("%s[%s=true]pasv executed successfully", getLogPrefix(), getArguments().getPassiveMode().getName());
            }
        }
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
            getLogger().debug("%s[%s=%s]successfully set", getLogPrefix(), getArguments().getTransferMode().getName(), getArguments()
                    .getTransferModeValue());
        }

        client.sendNoOp();// NOOP command
    }

    // see notes: autodetectUTF8Enabled
    private void features() {
        if (autodetectUTF8Enabled) {
            if (getLogger().isDebugEnabled()) {
                try {
                    if (client.features()) {
                        getLogger().debug("%s[FEAT][Server supports the following features]%s", getLogPrefix(), new FTPProtocolReply(client));
                    } else {
                        getLogger().debug("%s[FEAT]Server did not return any supported features in response to FEAT.", getLogPrefix());
                    }
                } catch (IOException e) {
                    getLogger().debug("%s[FEAT][exception]%s", getLogPrefix(), e);
                }
            }
        } else {
            // apiNote: Please note that this has to be set before the connection is established.
            // client.setControlEncoding(charsetUTF8);
            try {
                if (client.features()) {
                    getLogger().debug("%s[setControlEncoding][FEAT][Server supports the following features]%s", getLogPrefix(), new FTPProtocolReply(
                            client));
                    String charsetUTF8 = StandardCharsets.UTF_8.name();
                    if (client.hasFeature("UTF8") || client.hasFeature(charsetUTF8)) {
                        client.setControlEncoding(charsetUTF8);
                        if (getLogger().isDebugEnabled()) {
                            getLogger().debug("%s[setControlEncoding]%s", getLogPrefix(), charsetUTF8);
                        }
                    }
                } else {
                    getLogger().debug("%s[setControlEncoding][FEAT]Server did not return any supported features in response to FEAT.",
                            getLogPrefix());
                }
            } catch (IOException e) {
                getLogger().debug("%s[setControlEncoding][FEAT][exception]%s", getLogPrefix(), e);
            }
        }
    }

    private void postLoginOperationsIfFTPS() throws Exception {
        if (isFTPS) {
            client.enterLocalPassiveMode();
            debugCommand("enterLocalPassiveMode");

            // SSL login and data connection - execPBSZ(0),execPROT(P)
            FTPSClient ftps = (FTPSClient) client;
            try {
                ftps.execPBSZ(0);
                debugCommand("execPBSZ(0)");
            } catch (Throwable e) {
                getLogger().warn("[execPBSZ(0)]" + e);
            }
            try {
                ftps.execPROT("P");
                debugCommand("execPROT(P)");
            } catch (Throwable e) {
                getLogger().warn("[execPROT(P)]" + e);
            }

            if (getLogger().isDebugEnabled()) {
                getLogger().debug("%s[getEnabledProtocols]%s", getLogPrefix(), Arrays.asList(((FTPSClient) client).getEnabledProtocols()));
            }
        }
    }

    private String getConnectedInfos() {
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
            return "Server " + client.getSystemType();
        } catch (IOException e) {
            return "";
        }
    }

    private void validatePrerequisites(String method, String argValue, String msg) throws ProviderException {
        validatePrerequisites(method);
        validateArgument(method, argValue, msg);
    }

    private Duration getKeepAliveTimeout() {
        return Duration.ofSeconds(getArguments().getKeepAliveTimeoutAsSeconds());
    }

    private void setProtocolCommandListener(FTPClient client) {
        if (getArguments().getProtocolCommandListener().isTrue() || FTPProviderUtils.isCommandListenerEnvVarSet()) {
            client.addProtocolCommandListener(new FTPProtocolCommandListener(getLogger()));
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

}
