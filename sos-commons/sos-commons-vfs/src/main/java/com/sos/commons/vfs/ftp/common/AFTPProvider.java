package com.sos.commons.vfs.ftp.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPCmd;
import org.apache.commons.net.ftp.FTPFile;

import com.sos.commons.exception.SOSRequiredArgumentMissingException;
import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSPathUtil;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.SOSCommandResult;
import com.sos.commons.util.common.SOSEnv;
import com.sos.commons.util.common.SOSTimeout;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.AProvider;
import com.sos.commons.vfs.common.AProviderArguments.FileType;
import com.sos.commons.vfs.common.IProvider;
import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.commons.vfs.common.file.files.DeleteFilesResult;
import com.sos.commons.vfs.common.file.files.RenameFilesResult;
import com.sos.commons.vfs.common.file.selection.ProviderFileSelection;
import com.sos.commons.vfs.exceptions.SOSProviderClientNotInitializedException;
import com.sos.commons.vfs.exceptions.SOSProviderConnectException;
import com.sos.commons.vfs.exceptions.SOSProviderException;
import com.sos.commons.vfs.exceptions.SOSProviderInitializationException;

public abstract class AFTPProvider extends AProvider<FTPProviderArguments> {

    private FTPClient client;

    public AFTPProvider(ISOSLogger logger, FTPProviderArguments args) throws SOSProviderInitializationException {
        super(logger, args, fileRepresentator -> {
            if (fileRepresentator == null) {
                return false;
            }
            FTPFile f = (FTPFile) fileRepresentator;
            return (f.isFile() && args.getValidFileTypes().getValue().contains(FileType.REGULAR)) || (f.isSymbolicLink() && args.getValidFileTypes()
                    .getValue().contains(FileType.SYMLINK));
        });
        setAccessInfo(String.format("%s@%s:%s", getArguments().getUser().getDisplayValue(), getArguments().getHost().getDisplayValue(), getArguments()
                .getPort().getDisplayValue()));
    }

    /** FTP/FTPS client */
    public abstract FTPClient createClient() throws Exception;

    /** Overrides {@link IProvider#connect()} */
    @Override
    public void connect() throws SOSProviderConnectException {
        if (SOSString.isEmpty(getArguments().getHost().getValue())) {
            throw new SOSProviderConnectException(new SOSRequiredArgumentMissingException("host"));
        }
        try {
            getLogger().info(getConnectMsg());

            // FTP/FTPS client
            client = createClient();
            client.setConnectTimeout(getArguments().getConnectTimeoutAsMs());

            // connect
            client.connect(getArguments().getHost().getValue(), getArguments().getPort().getValue());
            FTPProtocolReply reply = new FTPProtocolReply(client);
            if (!reply.isPositiveReply()) {
                throw new Exception(String.format("%s[connect][FTP server refused connection]%s", getLogPrefix(), reply));
            }

            // keep alive
            client.setControlKeepAliveTimeout(Duration.ofSeconds(getArguments().getServerAliveInterval().getValue()));
            // login
            client.login(getArguments().getUser().getValue(), getArguments().getPassword().getValue());
            reply = new FTPProtocolReply(client);
            if (!reply.isPositiveReply()) {
                throw new Exception(String.format("%s[login]%s", getLogPrefix(), reply));
            }
            // Post login commands
            postLoginCommands();
        } catch (Throwable e) {
            if (isConnected()) {
                disconnect();
            }
            throw new SOSProviderConnectException(String.format("[%s]", getAccessInfo()), e);
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

    /** Overrides {@link IProvider#createDirectoriesIfNotExists(String)} */
    @Override
    public boolean createDirectoriesIfNotExists(String path) throws SOSProviderException {
        checkBeforeOperation("createDirectoriesIfNotExists", path, "path");

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
            throw new SOSProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#deleteIfExists(String)} */
    @Override
    public boolean deleteIfExists(String path) throws SOSProviderException {
        checkBeforeOperation("deleteIfExists", path, "path");

        try {
            FTPFile[] files = client.listFiles(path);
            if (SOSCollection.isEmpty(files)) {
                return false;
            }

            FTPFile file = files[0];
            boolean deleted = false;
            if (file.isDirectory()) {
                deleteDirectoryFilesRecursively(path);
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
            throw new SOSProviderException(getPathOperationPrefix(path), e.getCause());
        }
    }

    /** Overrides {@link IProvider#deleteFilesIfExists(Collection, boolean)} */
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
            new SOSProviderException(e);
        }
        return r;
    }

    /** Overrides {@link IProvider#exists(String)}<br/>
     * Uses the SIZE command<br/>
     * - alternative to listFiles<br/>
     * TODO - which is better?<br/>
     * -- which retrieves the information when, for example, the current user does not have the permissions to read the file but the file still exists... */
    @Override
    public boolean exists(String path) {
        if (client == null) {
            return false;
        }

        FTPProtocolReply reply = null;
        try {
            checkParam("exists", path, "path"); // here because should not throw any errors

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

    /** Overrides {@link IProvider#isAbsolutePath(String)} */
    @Override
    public boolean isAbsolutePath(String path) {
        return SOSPathUtil.isAbsoluteUnixPath(path);
    }

    /** Overrides {@link IProvider#getPathSeparator()} */
    @Override
    public String getPathSeparator() {
        return SOSPathUtil.PATH_SEPARATOR_UNIX;
    }

    /** Overrides {@link IProvider#normalizePath(String)} */
    @Override
    public String normalizePath(String path) {
        return toPathStyle(SOSPath.toAbsoluteNormalizedPath(path).toString());
    }

    /** Overrides {@link IProvider#getFileIfExists(String)} */
    @Override
    public ProviderFile getFileIfExists(String path) throws SOSProviderException {
        checkBeforeOperation("getFileIfExists", path, "path");
        try {
            return createProviderFile(path, getFTPFile("getFileIfExists", path));
        } catch (Throwable e) {
            throw new SOSProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#rereadFileIfExists(ProviderFile)} */
    @Override
    public ProviderFile rereadFileIfExists(ProviderFile file) throws SOSProviderException {
        // TODO Auto-generated method stub
        return null;
    }

    /** Overrides {@link IProvider#selectFiles(ProviderFileSelection)} */
    @Override
    public List<ProviderFile> selectFiles(ProviderFileSelection selection) throws SOSProviderException {
        // TODO Auto-generated method stub
        return null;
    }

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

    /** Overrides {@link IProvider#setFileLastModifiedFromMillis(String, long)} */
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

    /** Overrides {@link IProvider#getOutputStream(String, boolean)} */
    @Override
    public OutputStream getOutputStream(String path, boolean append) throws SOSProviderException {
        // TODO Auto-generated method stub
        return null;
    }

    /** Overrides {@link IProvider#executeCommand(String, SOSTimeout, SOSEnv)} */
    @Override
    public SOSCommandResult executeCommand(String command, SOSTimeout timeout, SOSEnv env) {
        // TODO Auto-generated method stub
        return null;
    }

    /** Overrides {@link IProvider#cancelCommands()} */
    @Override
    public SOSCommandResult cancelCommands() {
        // TODO Auto-generated method stub
        return null;
    }

    public FTPClient getClient() {
        return client;
    }

    public void setProtocolCommandListener(FTPClient client) {
        if (getArguments().getProtocolCommandListener().isTrue() || isCommandListenerEnvVarSet()) {
            client.addProtocolCommandListener(new FTPProtocolCommandListener(getLogger()));
            getLogger().debug(getLogPrefix() + "ProtocolCommandListener added");
        }
    }

    private boolean isCommandListenerEnvVarSet() {
        String val = System.getenv("AddFTPProtocol");
        return val != null && "true".equalsIgnoreCase(val);
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
        // Passive Mode
        if (getArguments().getPassiveMode().isTrue()) {
            getClient().pasv();
            FTPProtocolReply reply = new FTPProtocolReply(client);
            if (reply.isPositiveReply()) {
                client.enterLocalPassiveMode();// TODO check - on connect and here
            } else {
                throw new SOSProviderException(String.format("%s[passv]%s", getLogPrefix(), reply));
            }
        }

        // Transfer Mode
        if (getArguments().isBinaryTransferMode()) {
            if (!client.setFileType(FTP.BINARY_FILE_TYPE)) {
                throw new SOSProviderException(String.format("%s[binary]%s", getLogPrefix(), new FTPProtocolReply(client)));
            }
        } else {
            if (!client.setFileType(FTP.ASCII_FILE_TYPE)) {
                throw new SOSProviderException(String.format("%s[ascii]%s", getLogPrefix(), new FTPProtocolReply(client)));
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
        sendCommand("NOOP");
    }

    private void sendCommand(final String command) throws SOSProviderException {
        try {
            client.sendCommand(command);
        } catch (IOException e) {
            throw new SOSProviderException("[sendCommand][" + command + "]" + e.toString(), e);
        }
        debugCommand("sendCommand][" + command);
    }

    public void debugCommand(String command) {
        if (!getLogger().isDebugEnabled() || getArguments().getProtocolCommandListener().isTrue()) {
            return;
        }
        getLogger().debug("%s[%s]%s", getLogPrefix(), command, new FTPProtocolReply(client));
    }

    private void checkBeforeOperation(String method) throws SOSProviderException {
        if (client == null) {
            throw new SOSProviderClientNotInitializedException(getLogPrefix() + method + "FTPClient");
        }
    }

    private void checkBeforeOperation(String method, String paramValue, String msg) throws SOSProviderException {
        checkBeforeOperation(method);
        checkParam(method, paramValue, msg);
    }

    private void deleteDirectoryFilesRecursively(String path) throws Exception {
        FTPFile[] children = client.listFiles(path);
        if (!SOSCollection.isEmpty(children)) {
            for (FTPFile child : children) {
                String childPath = path + getPathSeparator() + child.getName();
                if (child.isDirectory()) {
                    deleteDirectoryFilesRecursively(childPath);
                } else {
                    if (!client.deleteFile(childPath)) {
                        throw new Exception(String.format("[failed to delete file][%s]%s", childPath, new FTPProtocolReply(client)));
                    }
                }
            }
        }
    }

    /** Attempt to determine if NOT_FOUND is truly the cause in the case of FTPReply.FILE_UNAVAILABLE, rather than issues like permissions, etc. */
    private boolean isReplyBasedOnFileNotFound(FTPProtocolReply reply, String path) {
        if (reply.isFileUnavailableReply()) {
            // Reply text is not analyzed due to different implementations/languages
            return exists(path);
        }
        return false;
    }

    private FTPFile getFTPFile(String caller, String path) throws Exception {
        FTPFile[] files = client.listFiles(path);
        if (SOSCollection.isEmpty(files)) {
            return null;
        }
        if (files.length > 1) {
            throw new Exception("[the path is ambiguous, more than one file found][" + files.length + "]" + SOSString.join(files));
        }
        return files[0];
    }

    private ProviderFile createProviderFile(String path, FTPFile file) {
        if (file == null || !isValidFileType(file)) {
            return null;
        }
        return createProviderFile(path, file.getSize(), file.getTimestamp() == null ? -1L : file.getTimestamp().getTimeInMillis());
    }

}
