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
import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPCmd;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPHTTPClient;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.util.TrustManagerUtils;

import com.sos.commons.exception.SOSRequiredArgumentMissingException;
import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSJavaKeyStoreReader;
import com.sos.commons.util.SOSPathUtils;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.beans.SOSCommandResult;
import com.sos.commons.util.beans.SOSEnv;
import com.sos.commons.util.beans.SOSTimeout;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.commons.AProvider;
import com.sos.commons.vfs.commons.AProviderArguments.FileType;
import com.sos.commons.vfs.commons.AProviderArguments.Protocol;
import com.sos.commons.vfs.commons.IProvider;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.commons.vfs.commons.file.files.DeleteFilesResult;
import com.sos.commons.vfs.commons.file.files.RenameFilesResult;
import com.sos.commons.vfs.commons.file.selection.ProviderFileSelection;
import com.sos.commons.vfs.commons.proxy.ProxySocketFactory;
import com.sos.commons.vfs.exceptions.ProviderAuthenticationException;
import com.sos.commons.vfs.exceptions.ProviderClientNotInitializedException;
import com.sos.commons.vfs.exceptions.ProviderConnectException;
import com.sos.commons.vfs.exceptions.ProviderException;
import com.sos.commons.vfs.exceptions.ProviderInitializationException;
import com.sos.commons.vfs.ftp.commons.FTPProtocolCommandListener;
import com.sos.commons.vfs.ftp.commons.FTPProtocolReply;
import com.sos.commons.vfs.ftp.commons.FTPProviderArguments;
import com.sos.commons.vfs.ftp.commons.FTPSProviderArguments;
import com.sos.commons.vfs.ftp.commons.ProviderUtils;

public class FTPProvider extends AProvider<FTPProviderArguments> {

    private final boolean isFTPS;
    private FTPClient client;

    public FTPProvider(ISOSLogger logger, FTPProviderArguments args) throws ProviderInitializationException {
        super(logger, args);
        setAccessInfo(String.format("%s@%s:%s", getArguments().getUser().getDisplayValue(), getArguments().getHost().getDisplayValue(), getArguments()
                .getPort().getDisplayValue()));
        isFTPS = Protocol.FTPS.equals(getArguments().getProtocol().getValue());
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

            // FTP/FTPS client
            client = createClient();
            setProtocolCommandListener(client);

            client.setConnectTimeout(getArguments().getConnectTimeoutAsMs());

            // Connect
            client.connect(getArguments().getHost().getValue(), getArguments().getPort().getValue());
            FTPProtocolReply reply = new FTPProtocolReply(client);
            if (!reply.isPositiveReply()) {
                throw new Exception(String.format("%s[connect][FTP server refused connection]%s", getLogPrefix(), reply));
            }

            // Keep Alive
            client.setControlKeepAliveTimeout(getKeepAliveTimeout());
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
            // Post Login Commands
            postLoginCommands();

        } catch (Throwable e) {
            if (isConnected()) {
                disconnect();
            }
            throw new ProviderConnectException(String.format("[%s]", getAccessInfo()), e);
        } finally {
            getLogger().info(getConnectedMsg(getConnectedInfos()));
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
                client.sendCommand("NOOP");
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

        String directory = selection.getConfig().getDirectory() == null ? "." : selection.getConfig().getDirectory();
        List<ProviderFile> result = new ArrayList<>();
        try {
            ProviderUtils.selectFiles(this, selection, directory, result);
        } catch (ProviderException e) {
            throw e;
        }
        return result;
    }

    /** Overrides {@link IProvider#exists(String)}<br/>
     * Uses the SIZE command<br/>
     * - alternative to listFiles<br/>
     * TODO - which is better?<br/>
     * -- which retrieves the information when, for example, the current user does not have the permissions to read the file but the file still exists... */
    @Override
    public boolean exists(String path) {
        if (client == null || path == null) {
            return false;
        }

        FTPProtocolReply reply = null;
        try {
            client.sendCommand(FTPCmd.SIZE, path);
            reply = new FTPProtocolReply(client);
            if (reply.isFileStatusReply()) {
                return false;
            }
            if (reply.isFileUnavailableReply()) {
                return true;
            }
        } catch (Throwable e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("%s[exists=false][reply=%s]%s", getPathOperationPrefix(path), reply, e.toString());
            }
            return false;
        }
        return false;
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

            Deque<String> toCreate = new ArrayDeque<>();
            String parent = SOSPathUtils.getParentPath(dir, getPathSeparator());
            while (!SOSString.isEmpty(parent) && !parent.equals(dir) && !client.changeWorkingDirectory(parent)) {
                toCreate.push(parent);
                parent = SOSPathUtils.getParentPath(dir, getPathSeparator());
            }

            boolean created = false;
            while (!toCreate.isEmpty()) {
                String dirToCreate = toCreate.pop();
                if (!client.makeDirectory(dirToCreate)) {
                    throw new Exception(String.format("[failed to create directory][%s]%s", dirToCreate, new FTPProtocolReply(client)));
                }
                created = true;
            }
            return created;
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    @SuppressWarnings("unused")
    // TODO to remove
    private boolean createDirectoriesIfNotExistsX(String path) throws ProviderException {
        validatePrerequisites("createDirectoriesIfNotExists", path, "path");

        try {
            if (client.changeWorkingDirectory(path)) {
                return false; // already exists
            }

            StringBuilder currentPath = new StringBuilder();
            boolean created = false;
            for (String dir : path.split(getPathSeparator())) {
                if (dir.isEmpty()) {
                    continue;
                }
                currentPath.append(getPathSeparator()).append(dir);
                if (!client.changeWorkingDirectory(currentPath.toString())) {
                    if (!client.makeDirectory(currentPath.toString())) {
                        throw new Exception(String.format("[failed to create directory][%s]%s", currentPath, new FTPProtocolReply(client)));
                    }
                    created = true;
                }
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
            FTPFile[] files = client.listFiles(path);
            if (SOSCollection.isEmpty(files)) {
                return false;
            }

            FTPFile file = files[0];
            boolean deleted = false;
            if (file.isDirectory()) {
                ProviderUtils.deleteDirectoryFilesRecursively(client, getPathSeparator(), path);
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
            throw new ProviderException(getPathOperationPrefix(path), e.getCause());
        }
    }

    /** Overrides {@link IProvider#deleteFilesIfExists(Collection, boolean)} */
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
                    if (!client.deleteFile(file)) {
                        FTPProtocolReply reply = new FTPProtocolReply(client);
                        if (isReplyBasedOnFileNotFound(reply, file)) {
                            r.addNotFound(file);
                        } else {
                            throw new Exception(String.format("[failed to delete file]%s", reply));
                        }
                    }
                    r.addSuccess();
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
                    if (!client.rename(source, target)) {
                        FTPProtocolReply reply = new FTPProtocolReply(client);
                        if (isReplyBasedOnFileNotFound(reply, target)) {
                            r.addNotFound(source);
                        } else {
                            throw new Exception(String.format("[failed to rename file]%s", reply));
                        }
                    }
                    r.addSuccess();
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
            return createProviderFile(path, ProviderUtils.getFTPFile("getFileIfExists", client, path));
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
        try (InputStream is = client.retrieveFileStream(path); Reader r = new InputStreamReader(is, StandardCharsets.UTF_8); BufferedReader br =
                new BufferedReader(r)) {
            reply = new FTPProtocolReply(client);
            if (is == null || reply.isFileUnavailableReply()) {
                return null;
            }
            br.lines().forEach(content::append);
            return client.completePendingCommand() ? content.toString() : null;
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path) + (reply == null ? "" : reply), e);
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
    public void setFileLastModifiedFromMillis(String path, long milliseconds) throws ProviderException {
        validatePrerequisites("setFileLastModifiedFromMillis", path, path);
        validateModificationTime(path, milliseconds);

        try {
            // FTP servers expect the timestamp in UTC and exactly in this format
            SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmss");
            f.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
            if (!client.setModificationTime(path, f.format(new Date(milliseconds)))) {
                throw new ProviderException(String.format("%s[failed to set modification time]%s", getPathOperationPrefix(path), new FTPProtocolReply(
                        client)));
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

    /** Overrides {@link IProvider#getOutputStream(String, boolean)} */
    @Override
    public OutputStream getOutputStream(String path, boolean append) throws ProviderException {
        validatePrerequisites("getOutputStream", path, "path");

        try {
            OutputStream os = append ? client.appendFileStream(path) : client.storeFileStream(path);
            if (os == null) {
                throw new ProviderException(String.format("%s[failed to open OutputStream]%s", getPathOperationPrefix(path), new FTPProtocolReply(
                        client)));
            }
            return os;
        } catch (ProviderException e) {
            throw e;
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

    private FTPClient createClient() throws Exception {
        /** FTPS */
        if (isFTPS) {
            FTPSProviderArguments args = (FTPSProviderArguments) getArguments();
            // FTPSClient
            FTPSClient client = new FTPSClient(args.getSecureSocketProtocol().getValue(), args.isSecurityModeImplicit());
            // PROXY
            if (getProxyProvider() != null) {
                client.setProxy(getProxyProvider().getProxy());
            }
            // TRUST MANAGER#
            if (args.getJavaKeyStore() != null) {
                KeyStore ks = SOSJavaKeyStoreReader.load(args.getJavaKeyStore().getFile().getValue(), args.getJavaKeyStore().getPassword().getValue(),
                        args.getJavaKeyStore().getType().getValue());
                if (ks != null) {
                    getLogger().info(String.format("%s[setTrustManager][keystore]type=%s,file=%s", getLogPrefix(), args.getJavaKeyStore().getType()
                            .getValue(), args.getJavaKeyStore().getFile().getValue()));
                    client.setTrustManager(TrustManagerUtils.getDefaultTrustManager(ks));
                }
            }
            return client;
        }
        /** FTP */
        else {
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
    }

    private String getConnectedInfos() {
        List<String> l = new ArrayList<String>();
        if (client.getConnectTimeout() > 0) {
            l.add("ConnectTimeout=" + AProvider.millis2string(client.getConnectTimeout()));
        }
        // + passive, + binary
        l.add("KeepAliveTimeout=" + getArguments().getServerAliveInterval().getValue() + "s");

        String serverInfo = getServerInfo();
        return SOSString.isEmpty(serverInfo) ? String.join(",", l) : ("[" + serverInfo + "]" + String.join(",", l));
    }

    private String getServerInfo() {
        try {
            return "Server " + client.getSystemType();
        } catch (IOException e) {
            return "";
        }
    }

    private void postLoginCommands() throws Exception {
        /** FTP/FTPS */
        // Passive Mode
        if (getArguments().getPassiveMode().isTrue()) {
            client.pasv();
            FTPProtocolReply reply = new FTPProtocolReply(client);
            if (reply.isPositiveReply()) {
                client.enterLocalPassiveMode();// TODO check - FTPS below
            } else {
                throw new ProviderException(String.format("%s[passv]%s", getLogPrefix(), reply));
            }
        }

        // Transfer Mode
        if (getArguments().isBinaryTransferMode()) {
            if (!client.setFileType(FTP.BINARY_FILE_TYPE)) {
                throw new ProviderException(String.format("%s[binary]%s", getLogPrefix(), new FTPProtocolReply(client)));
            }
        } else {
            if (!client.setFileType(FTP.ASCII_FILE_TYPE)) {
                throw new ProviderException(String.format("%s[ascii]%s", getLogPrefix(), new FTPProtocolReply(client)));
            }
        }

        // Commands
        sendCommand("FEAT");
        FTPProtocolReply replay = new FTPProtocolReply(client);
        if (replay.isSystemStatusReply()) {
            String[] lines = client.getReplyStrings();
            for (int i = 1; i < lines.length - 1; i++) {
                String line = lines[i].trim().toUpperCase();
                if ("UTF8".equals(line)) {
                    client.setControlEncoding("UTF-8");
                    break;
                }
            }
        } else {
            getLogger().info("%s[FEAT][no valid response received]%s", getLogPrefix(), replay);
        }

        // ?
        sendCommand("NOOP");

        /** FTPS */
        if (isFTPS) {
            try {
                ((FTPSClient) client).execPBSZ(0);
                debugCommand("execPBSZ(0)");
            } catch (Throwable e) {
                getLogger().warn("[execPBSZ(0)]" + e);
            }
            try {
                ((FTPSClient) client).execPROT("P");
                debugCommand("execPROT(P)");
            } catch (Throwable e) {
                getLogger().warn("[execPROT(P)]" + e);
            }
            client.enterLocalPassiveMode();
            debugCommand("enterLocalPassiveMode");
        }
    }

    private void sendCommand(final String command) throws ProviderException {
        try {
            client.sendCommand(command);
        } catch (IOException e) {
            throw new ProviderException("[sendCommand][" + command + "]" + e.toString(), e);
        }
        debugCommand("sendCommand][" + command);
    }

    private void validatePrerequisites(String method, String argValue, String msg) throws ProviderException {
        validatePrerequisites(method);
        validateArgument(method, argValue, msg);
    }

    /** Attempt to determine if NOT_FOUND is truly the cause in the case of FTPReply.FILE_UNAVAILABLE, rather than issues like permissions, etc. */
    private boolean isReplyBasedOnFileNotFound(FTPProtocolReply reply, String path) {
        if (reply.isFileUnavailableReply()) {
            // Reply text is not analyzed due to different implementations/languages
            return exists(path);
        }
        return false;
    }

    private Duration getKeepAliveTimeout() {
        return Duration.ofSeconds(getArguments().getServerAliveInterval().getValue());
    }

    private void setProtocolCommandListener(FTPClient client) {
        if (getArguments().getProtocolCommandListener().isTrue() || ProviderUtils.isCommandListenerEnvVarSet()) {
            client.addProtocolCommandListener(new FTPProtocolCommandListener(getLogger()));
            getLogger().debug(getLogPrefix() + "ProtocolCommandListener added");
        }
    }

}
