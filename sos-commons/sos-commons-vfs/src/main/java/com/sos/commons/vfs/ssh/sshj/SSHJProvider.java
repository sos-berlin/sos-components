package com.sos.commons.vfs.ssh.sshj;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.hierynomus.protocol.transport.TransportException;
import com.sos.commons.exception.SOSException;
import com.sos.commons.exception.SOSNoSuchFileException;
import com.sos.commons.exception.SOSRequiredArgumentMissingException;
import com.sos.commons.util.SOSClassUtil;
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
import com.sos.commons.vfs.commons.file.selection.ProviderFileSelection;
import com.sos.commons.vfs.exceptions.ProviderClientNotInitializedException;
import com.sos.commons.vfs.exceptions.ProviderConnectException;
import com.sos.commons.vfs.exceptions.ProviderException;
import com.sos.commons.vfs.exceptions.ProviderInitializationException;
import com.sos.commons.vfs.ssh.SSHProvider;
import com.sos.commons.vfs.ssh.commons.SSHProviderArguments;
import com.sos.commons.vfs.ssh.exceptions.SOSSSHCommandExitViolentlyException;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.common.SSHException;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.connection.channel.direct.Signal;
import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.FileMode;
import net.schmizz.sshj.sftp.OpenMode;
import net.schmizz.sshj.sftp.RemoteFile;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.xfer.FileSystemFile;

public class SSHJProvider extends SSHProvider {

    /** Maximum number of concurrent (unconfirmed) SFTP WRITE/READ requests allowed to be in flight.<br/>
     * Used to pipeline writes and improve throughput on high-latency connections. */
    private static final int REMOTE_FILE_MAX_UNCONFIRMED_WRITES = 16;
    private static final int REMOTE_FILE_MAX_UNCONFIRMED_READS = 16;

    private final Object clientLock = new Object();
    private volatile SSHClient sshClient;

    private Map<String, Command> commands = new ConcurrentHashMap<>();
    private boolean reusableResourceEnabled;

    public SSHJProvider(ISOSLogger logger, SSHProviderArguments args) throws ProviderInitializationException {
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

            connectInternal();
            // default: disable_auto_detect_shell=false
            // - executes the "uname" command once to retrieve advanced server information (e.g., OS, Shell)
            getServerInfo();

            // executing "uname" may disconnect the SSH client
            // - e.g., if the server only supports the SFTP subsystem and closes the connection instead of reporting an "exec" channel failure
            if (needsReconnectAfterServerInfo()) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("%s[sshClient not connected anymore after getServerInfo=%s]trying to reconnect ...", getLogPrefix(),
                            getServerInfo());
                }
                disconnectInternal();
                connectInternal();
            }

            getLogger().info(getConnectedMsg(SSHJProviderUtils.getConnectedInfos(sshClient)));

            // creates a shared SFTPClient by default
            enableReusableResource();
        } catch (Exception e) {
            // Do not call disconnect() here. it sets the client to null and may cause a ProviderClientNotInitializedException instead of a real connection
            // error in methods executed after connect() - e.g. if retry, roll back...
            // Call disconnect() in the application's finally block.
            // if (isConnected()) {
            // disconnect();
            // }
            throw new ProviderConnectException(String.format("%s[%s]", getLogPrefix(), getAccessInfo()), e);
        }
    }

    /** Overrides {@link IProvider#isConnected()}
     * 
     * @apiNote The sshClient.isConnected() method is not particularly reliable because it checks internal sshj flags instead of the actual connection (e.g.,
     *          socket). */
    @Override
    public boolean isConnected() {
        synchronized (clientLock) {
            return sshClient == null ? false : sshClient.isConnected();
        }
    }

    /** Overrides {@link IProvider#disconnect()} */
    @Override
    public void disconnect() {
        if (disconnectInternal()) {
            getLogger().info(getDisconnectedMsg());
        }
    }

    /** Overrides {@link IProvider#injectConnectivityFault()} */
    @Override
    public void injectConnectivityFault() {
        synchronized (clientLock) {
            if (sshClient != null) {
                try {
                    sshClient.disconnect();
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
        selection = ProviderFileSelection.createIfNull(selection);
        selection.setFileTypeChecker(fileRepresentator -> {
            if (fileRepresentator == null) {
                return false;
            }
            FileAttributes r = (FileAttributes) fileRepresentator;
            return (FileMode.Type.REGULAR.equals(r.getType()) && getArguments().getValidFileTypes().getValue().contains(FileType.REGULAR))
                    || (FileMode.Type.SYMLINK.equals(r.getType()) && getArguments().getValidFileTypes().getValue().contains(FileType.SYMLINK));
        });

        String directory = selection.getConfig().getDirectory() == null ? "." : selection.getConfig().getDirectory();
        try {
            List<ProviderFile> result = new ArrayList<>();
            SSHJProviderUtils.selectFiles(this, selection, directory, result);
            return result;
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(directory), e);
        }
    }

    /** Overrides {@link IProvider#exists(String)} */
    @Override
    public boolean exists(String path) throws ProviderException {
        validateArgument("exists", path, "path");

        try {
            SSHJProviderReusableResource reusable = getReusableResource();
            if (reusable == null) {
                try (SFTPClient sftp = requireSSHClient().newSFTPClient()) {
                    return SSHJProviderUtils.exists(sftp, path);
                }
            } else {
                return SSHJProviderUtils.exists(reusable.getSFTPClient(), path);
            }
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#createDirectoriesIfNotExists(String)} */
    @Override
    public boolean createDirectoriesIfNotExists(String path) throws ProviderException {
        validateArgument("createDirectoriesIfNotExists", path, "path");

        try {
            SSHJProviderReusableResource reusable = getReusableResource();
            if (reusable == null) {
                try (SFTPClient sftp = requireSSHClient().newSFTPClient()) {
                    if (SSHJProviderUtils.exists(sftp, path)) {
                        return false;
                    }
                    sftp.mkdirs(path);
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("%s[createDirectoriesIfNotExists][%s]created", getLogPrefix(), path);
                    }
                    return true;
                }
            } else {
                if (SSHJProviderUtils.exists(reusable.getSFTPClient(), path)) {
                    return false;
                }
                reusable.getSFTPClient().mkdirs(path);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("%s[createDirectoriesIfNotExists][%s]created", getLogPrefix(), path);
                }
                return true;
            }
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }

    }

    // TODO test if not exists ....
    /** Overrides {@link IProvider#deleteIfExists(String)} */
    @Override
    public boolean deleteIfExists(String path) throws ProviderException {
        validateArgument("deleteIfExists", path, "path");

        try {
            SSHJProviderReusableResource reusable = getReusableResource();
            if (reusable == null) {
                try (SFTPClient sftp = requireSSHClient().newSFTPClient()) {
                    SSHJProviderUtils.delete(sftp, path);
                    return true;
                }
            } else {
                SSHJProviderUtils.delete(reusable.getSFTPClient(), path);
                return true;
            }
        } catch (SOSNoSuchFileException e) {
            return false;
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path), e.getCause() == null ? e : e.getCause());
        }
    }

    /** Overrides {@link IProvider#deleteFileIfExists(String)} */
    @Override
    public boolean deleteFileIfExists(String path) throws ProviderException {
        validateArgument("deleteIfExists", path, "path");

        try {
            SSHJProviderReusableResource reusable = getReusableResource();
            if (reusable == null) {
                try (SFTPClient sftp = requireSSHClient().newSFTPClient()) {
                    SSHJProviderUtils.deleteFile(sftp, path);
                    return true;
                }
            } else {
                SSHJProviderUtils.deleteFile(reusable.getSFTPClient(), path);
                return true;
            }
        } catch (SOSNoSuchFileException e) {
            return false;
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path), e.getCause() == null ? e : e.getCause());
        }
    }

    /** Overrides {@link IProvider#moveFileIfExists(String, String)} */
    @Override
    public boolean moveFileIfExists(String source, String target) throws ProviderException {
        validateArgument("moveFileIfExists", source, "source");
        validateArgument("moveFileIfExists", target, "target");

        try {
            SSHJProviderReusableResource reusable = getReusableResource();
            if (reusable == null) {
                try (SFTPClient sftp = requireSSHClient().newSFTPClient()) {
                    if (SSHJProviderUtils.exists(sftp, source)) {
                        SSHJProviderUtils.rename(sftp, source, target);
                        return true;
                    }
                    return false;
                }
            } else {
                SFTPClient sftp = reusable.getSFTPClient();
                if (SSHJProviderUtils.exists(sftp, source)) {
                    SSHJProviderUtils.rename(sftp, source, target);
                    return true;
                }
                return false;
            }
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(source + "->" + target), e);
        }
    }

    /** Overrides {@link IProvider#getFileIfExists(String)} */
    @Override
    public ProviderFile getFileIfExists(String path) throws ProviderException {
        validateArgument("getFileIfExists", path, "path");

        try {
            SSHJProviderReusableResource reusable = getReusableResource();
            if (reusable == null) {
                try (SFTPClient sftp = requireSSHClient().newSFTPClient()) {
                    return createProviderFile(path, sftp.statExistence(path));
                }
            } else {
                return createProviderFile(path, reusable.getSFTPClient().statExistence(path));
            }
        } catch (NoSuchFileException e) {
            return null;
        } catch (Exception e) { // IOException| IllegalStateException
            throw new ProviderException(getPathOperationPrefix(path), e);
        }

    }

    /** Overrides {@link IProvider#getFileContentIfExists(String)} */
    @Override
    public String getFileContentIfExists(String path) throws ProviderException {
        validateArgument("getFileContentIfExists", path, "path");

        try {
            SSHJProviderReusableResource reusable = getReusableResource();
            if (reusable == null) {
                try (SFTPClient sftp = requireSSHClient().newSFTPClient()) {
                    return SSHJProviderUtils.getFileContentIfExists(sftp, path);
                }
            } else {
                return SSHJProviderUtils.getFileContentIfExists(reusable.getSFTPClient(), path);
            }
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#writeFile(String,String)} */
    @Override
    public void writeFile(String path, String content) throws ProviderException {
        validateArgument("writeFile", path, "path");

        try {
            SSHJProviderReusableResource reusable = getReusableResource();
            if (reusable == null) {
                try (SFTPClient sftp = requireSSHClient().newSFTPClient()) {
                    SSHJProviderUtils.uploadContent(sftp, path, content);
                }
            } else {
                SSHJProviderUtils.uploadContent(reusable.getSFTPClient(), path, content);
            }
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#setFileLastModifiedFromMillis(String,long)} */
    @Override
    public void setFileLastModifiedFromMillis(String path, long milliseconds) throws ProviderException {
        validateArgument("setFileLastModifiedFromMillis", path, path);
        validateModificationTime(path, milliseconds);

        try {
            SSHJProviderReusableResource reusable = getReusableResource();
            if (reusable == null) {
                try (SFTPClient sftp = requireSSHClient().newSFTPClient()) {
                    SSHJProviderUtils.setFileLastModifiedFromMillis(sftp, path, milliseconds);
                }
            } else {
                SSHJProviderUtils.setFileLastModifiedFromMillis(reusable.getSFTPClient(), path, milliseconds);
            }
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

        SSHJProviderReusableResource reusable = getReusableResource();
        final boolean closeSFTPClient = reusable == null;

        final AtomicReference<SFTPClient> sftpRef = new AtomicReference<>();
        final AtomicReference<RemoteFile> remoteFileRef = new AtomicReference<>();
        try {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("%s[getInputStream][supportsReadOffset=%s, offset=%s]%s", getLogPrefix(), supportsReadOffset(), offset, path);
            }

            SFTPClient sftpClient = reusable == null ? requireSSHClient().newSFTPClient() : reusable.getSFTPClient();
            sftpRef.set(sftpClient);

            RemoteFile remoteFile = sftpRef.get().open(path);
            remoteFileRef.set(remoteFile);
            return remoteFile.new ReadAheadRemoteFileInputStream(REMOTE_FILE_MAX_UNCONFIRMED_READS, offset) {

                private final AtomicBoolean closed = new AtomicBoolean();

                @Override
                public void close() throws IOException {
                    if (closed.getAndSet(true)) {
                        return;
                    }

                    IOException exception = null;
                    // 1) close the stream (ReadAheadRemoteFileInputStream)
                    try {
                        super.close();
                    } catch (IOException e) {
                        exception = SOSException.mergeException(exception, e);
                    }
                    // 2) close remote file
                    try {
                        SOSClassUtil.close(remoteFileRef.getAndSet(null));
                    } catch (IOException e) {
                        exception = SOSException.mergeException(exception, e);
                    }
                    // 3) close sftp client
                    if (closeSFTPClient) {
                        try {
                            SOSClassUtil.close(sftpRef.getAndSet(null));
                        } catch (IOException e) {
                            exception = SOSException.mergeException(exception, e);
                        }
                    }

                    if (exception != null) {
                        throw exception;
                    }
                }
            };
        } catch (Exception e) {
            try {
                SOSClassUtil.close(remoteFileRef.getAndSet(null));
            } catch (IOException ex) {
                e = SOSException.mergeException(e, ex);
            }
            if (closeSFTPClient) {
                try {
                    SOSClassUtil.close(sftpRef.getAndSet(null));
                } catch (IOException ex) {
                    e = SOSException.mergeException(e, ex);
                }
            }
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#getOutputStream(String,boolean)} */
    @Override
    public OutputStream getOutputStream(String path, boolean append) throws ProviderException {
        validateArgument("getOutputStream", path, "path");

        SSHJProviderReusableResource reusable = getReusableResource();
        final boolean closeSFTPClient = reusable == null;

        final AtomicReference<SFTPClient> sftpRef = new AtomicReference<>();
        final AtomicReference<RemoteFile> remoteFileRef = new AtomicReference<>();
        try {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("%s[getOutputStream][append=%s]%s", getLogPrefix(), append, path);
            }

            SFTPClient sftpClient = reusable == null ? requireSSHClient().newSFTPClient() : reusable.getSFTPClient();
            sftpRef.set(sftpClient);

            EnumSet<OpenMode> mode = EnumSet.of(OpenMode.WRITE, OpenMode.CREAT);
            if (append) {
                mode.add(OpenMode.APPEND);
            } else {
                mode.add(OpenMode.TRUNC);
            }
            RemoteFile remoteFile = sftpRef.get().open(path, mode);
            remoteFileRef.set(remoteFile);
            return remoteFile.new RemoteFileOutputStream(0, REMOTE_FILE_MAX_UNCONFIRMED_WRITES) {

                private final AtomicBoolean closed = new AtomicBoolean();

                @Override
                public void close() throws IOException {
                    if (closed.getAndSet(true)) {
                        return;
                    }

                    // 1) close the stream (RemoteFileOutputStream)
                    IOException exception = null;
                    try {
                        super.close();
                    } catch (IOException e) {
                        exception = SOSException.mergeException(exception, e);
                    }
                    // 2) close remote file
                    try {
                        SOSClassUtil.close(remoteFileRef.getAndSet(null));
                    } catch (IOException e) {
                        exception = SOSException.mergeException(exception, e);
                    }
                    // 3) close sftp client
                    if (closeSFTPClient) {
                        try {
                            SOSClassUtil.close(sftpRef.getAndSet(null));
                        } catch (IOException e) {
                            exception = SOSException.mergeException(exception, e);
                        }
                    }

                    if (exception != null) {
                        throw exception;
                    }
                }
            };
        } catch (Exception e) {
            try {
                SOSClassUtil.close(remoteFileRef.getAndSet(null));
            } catch (IOException ex) {
                e = SOSException.mergeException(e, ex);
            }
            if (closeSFTPClient) {
                try {
                    SOSClassUtil.close(sftpRef.getAndSet(null));
                } catch (IOException ex) {
                    e = SOSException.mergeException(e, ex);
                }
            }
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#executeCommand(String,SOSTinmeout,SOSEnv)} */
    @Override
    public SOSCommandResult executeCommand(String command, SOSTimeout timeout, SOSEnv env) {
        SOSCommandResult result = new SOSCommandResult(command);

        String uuid = createCommandIdentifier();
        try (Session session = requireSSHClient().startSession()) {
            if (getArguments().getSimulateShell().getValue()) {
                session.allocateDefaultPTY();
            }
            result.setCommand(handleCommandEnvs(command, session, env));
            try (final Command cmd = session.exec(result.getCommand())) {
                commands.put(uuid, cmd);
                // TODO use Charset?
                // String cs = getArguments().getRemoteCharset().getValue().name();
                result.setStdOut(IOUtils.readFully(cmd.getInputStream()).toString());
                result.setStdErr(IOUtils.readFully(cmd.getErrorStream()).toString());

                if (timeout == null) {
                    cmd.join();
                } else {
                    cmd.join(timeout.getInterval(), timeout.getTimeUnit());
                }

                result.setExitCode(cmd.getExitStatus());
                if (!result.hasExitCode()) {
                    if (cmd.getExitSignal() != null) {
                        throw new SOSSSHCommandExitViolentlyException(cmd.getExitSignal(), cmd.getExitErrorMessage());
                    }
                }
            }
        } catch (ConnectionException | TransportException e) {
            result.setException(e);

            // - disconnect sshClient (do not use the SSHJProvider.disconnect() method, as it sets the client to null)
            // - the disconnect via sshj is necessary because the sshClient.isConnected() method doesn't actually check whether the connection (socket, etc.) is
            // established.
            // -- It checks the internal sshj flags. set these flags.
            synchronized (clientLock) {
                SOSClassUtil.closeQuietly(sshClient);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("%s[executeCommand][sshClient disconnected]%s", getLogPrefix(), result);
                }
            }

            // TODO: implement an automatic reconnection here?
            // Observed behavior with CompleteFTP 25.0.6 (Windows):
            // - When SSH is disabled (SFTP-only mode): PRO
            // -- reconnect logic works correctly because a ConnectionException is thrown immediately.
            // - When SSH is enabled: CONTRA
            // -- reconnect logic does NOT trigger immediately, because no ConnectionException is thrown.
            // -- The sshClient is actually disconnected, but this happens a few milliseconds later on an asynchronous sshj thread.
            // -- The server sends an additional message to the channel that has already been closed.
            // --- sshj does not accept this message and only then marks the connection as disconnected.
            // Therefore, relying on isConnected() or catching exceptions in the same thread does not reliably detect this disconnect.

        } catch (Exception e) {
            result.setException(e);
        }
        resetCommand(uuid);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("%s[executeCommand][after][isConnected]%s", getLogPrefix(), isConnected());
        }
        return result;
    }

    // other thread
    /** Overrides {@link IProvider#cancelCommands()} */
    @Override
    public SOSCommandResult cancelCommands() {
        SOSCommandResult r = null;
        if (commands != null && commands.size() > 0) {
            r = new SOSCommandResult("Signal.KILL");
            Iterator<Entry<String, Command>> iterator = commands.entrySet().iterator();
            while (iterator.hasNext()) {
                try {
                    Command command = iterator.next().getValue();
                    command.signal(Signal.KILL);
                    r.setExitCode(command.getExitStatus());
                } catch (SSHException e) {
                    r.setException(e);
                }
                iterator.remove();
            }
        }
        return r;
    }

    /** Overrides {@link AProvider#enableReusableResource()} */
    @Override
    public void enableReusableResource() {
        if (reusableResourceEnabled) {
            return;
        }
        if (Protocol.SSH.equals(getArguments().getProtocol().getValue())) {
            reusableResourceEnabled = false;
            return;
        }
        try {
            super.enableReusableResource(new SSHJProviderReusableResource(this));
            reusableResourceEnabled = true;
        } catch (Exception e) {
            reusableResourceEnabled = false;
            getLogger().warn(getLogPrefix() + "[enableReusableResource=false]" + e);
        }
    }

    /** Overrides {@link AProvider#getReusableResource()} */
    @Override
    public SSHJProviderReusableResource getReusableResource() {
        if (!reusableResourceEnabled) {
            return null;
        }
        return (SSHJProviderReusableResource) super.getReusableResource();
    }

    @Override
    public void disableReusableResource() {
        super.disableReusableResource();
        reusableResourceEnabled = false;
    }

    /** SSH Provider specific methods */

    /** Overrides {@link ASSHProvider#put(String, String, int)} */
    @Override
    public void put(String source, String target, int perm) throws ProviderException {
        try {
            SSHJProviderReusableResource reusable = getReusableResource();
            if (reusable == null) {
                try (SFTPClient sftp = requireSSHClient().newSFTPClient()) {
                    SSHJProviderUtils.put(sftp, source, target);
                    sftp.chmod(target, perm);
                }
            } else {
                SSHJProviderUtils.put(reusable.getSFTPClient(), source, target);
                reusable.getSFTPClient().chmod(target, perm);
            }
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(source) + "[" + target + "][perm=" + perm + "]", e);
        }
    }

    /** Overrides {@link SSHProvider#put(String, String)} */
    @Override
    public void put(String source, String target) throws ProviderException {
        try {
            SSHJProviderReusableResource reusable = getReusableResource();
            if (reusable == null) {
                try (SFTPClient sftp = requireSSHClient().newSFTPClient()) {
                    SSHJProviderUtils.put(sftp, source, target);
                }
            } else {
                SSHJProviderUtils.put(reusable.getSFTPClient(), source, target);
            }
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(source) + "[" + target + "]", e);
        }
    }

    /** Overrides {@link SSHProvider#get(String, String)} */
    @Override
    public void get(String source, String target) throws ProviderException {
        try {
            SSHJProviderReusableResource reusable = getReusableResource();
            if (reusable == null) {
                try (SFTPClient sftp = requireSSHClient().newSFTPClient()) {
                    sftp.get(sftp.canonicalize(source), new FileSystemFile(target));
                }
            } else {
                reusable.getSFTPClient().get(reusable.getSFTPClient().canonicalize(source), new FileSystemFile(target));
            }
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(source) + "[" + target + "]", e);
        }
    }

    /** Overrides {@link SSHProvider#deleteDirectory(String)} */
    @Override
    public boolean deleteDirectory(String directory) throws ProviderException {
        if (getServerInfo().isWindowsShell()) {
            return deleteWindowsDirectory(directory);
        }
        return deleteUnixDirectory(directory);
    }

    /** Overrides {@link SSHProvider#deleteUnixDirectory(String)} */
    @Override
    public boolean deleteUnixDirectory(String directory) throws ProviderException {
        if (SOSString.isEmpty(directory)) {
            return false;
        }
        String dir = toPathStyle(directory);
        // String command = "[ -d \"" + dir + "\" ] && rm -rf \"" + dir + "\"";
        SOSCommandResult r = executeCommand("rm -f -R \"" + dir + "\"");
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(getPathOperationPrefix(dir) + "[deleteUnixDirectory]" + r);
        }
        if (r.hasError()) {
            return deleteIfExists(dir);
        }
        return true;
    }

    /** Overrides {@link SSHProvider#deleteWindowsDirectory(String)} */
    @Override
    public boolean deleteWindowsDirectory(String directory) throws ProviderException {
        if (SOSString.isEmpty(directory)) {
            return false;
        }
        String dir = SOSPathUtils.toWindowsStyle(directory);
        if (dir.startsWith("\\")) {// Windows OpenSSH after SOSPathUtils.toWindowsStyle
            dir = dir.substring(1);
        }
        // String command = "if exist \"" + dir + "\" ( rd /s /q \"" + dir + "\" )";
        SOSCommandResult r = executeCommand("rmdir /s /q \"" + dir + "\"");
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(getPathOperationPrefix(dir) + "[deleteWindowsDirectory]" + r);
        }
        if (r.hasError()) {
            return deleteIfExists(dir);
        }
        return true;
    }

    public SSHClient requireSSHClient() throws ProviderException {
        synchronized (clientLock) {
            if (sshClient == null) {
                // 0 - getStackTrace
                // 1 - requireClient
                // 2 - caller
                throw new ProviderClientNotInitializedException(getLogPrefix(), SSHClient.class, SOSClassUtil.getMethodName(2));
            }
            return sshClient;
        }
    }

    protected ProviderFile createProviderFile(String path, FileAttributes attr) {
        if (attr == null) {
            return null;
        }
        return createProviderFile(path, attr.getSize(), SSHJProviderUtils.getFileLastModifiedMillis(attr));
    }

    private void connectInternal() throws Exception {
        synchronized (clientLock) {
            // Creates the client unconditionally (no null check), because SSHJClientFactory.createAuthenticatedClient() performs the connect and
            // authentication.
            // If a previous sshClient instance exists, it must be closed via disconnect().
            sshClient = SSHJClientFactory.createAuthenticatedClient(getLogger(), getArguments(), getProxyConfig());
            setServerVersion(sshClient.getTransport().getServerVersion());
        }
    }

    // without logging
    private boolean disconnectInternal() {
        synchronized (clientLock) {
            if (sshClient == null) {
                commands.clear();
                return false;
            }
            commands.clear();

            disableReusableResource();
            SOSClassUtil.closeQuietly(sshClient);
            sshClient = null;
        }
        return true;
    }

    /** @apiNote The second check part (... || hasConnectionException ...) is not really necessary after the "executeCommand" method has been modified to
     *          disconnect the client connection on a ConnectionException.
     * @return */
    private boolean needsReconnectAfterServerInfo() {
        if (getArguments().getDisableAutoDetectShell().isTrue()) {
            return false;
        }

        boolean isConnected = isConnected();
        boolean hasConnectionException = hasConnectionException(getServerInfo().getCommandResult());

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("%s[needsReconnectAfterServerInfo]isConnected=%s, hasConnectionException=%s", getLogPrefix(), isConnected,
                    hasConnectionException);
        }

        return !isConnected || hasConnectionException;
    }

    private boolean hasConnectionException(SOSCommandResult result) {
        if (result == null) {
            return false;
        }
        return result.hasException() && result.getException() instanceof ConnectionException;
    }

    private synchronized String createCommandIdentifier() {
        return UUID.randomUUID().toString();
    }

    private void resetCommand(String uuid) {
        commands.remove(uuid);
    }

    private String handleCommandEnvs(String command, Session session, SOSEnv env) throws Exception {
        if (env == null) {
            return command;
        }
        // global/ssh server env vars
        if (env.getGlobalEnvs() != null && env.getGlobalEnvs().size() > 0) {
            for (Map.Entry<String, String> entry : env.getGlobalEnvs().entrySet()) {
                try {
                    session.setEnvVar(entry.getKey(), entry.getValue());
                } catch (Exception e) {
                    throw new Exception(String.format("[can't set ssh session environment variable][%s=%s]%s", entry.getKey(), entry.getValue(), e
                            .toString()), e);
                }
            }
        }

        // local/system env vars
        if (env.getLocalEnvs() != null && env.getLocalEnvs().size() > 0) {
            getServerInfo();

            StringBuilder envs = new StringBuilder();
            for (Map.Entry<String, String> entry : env.getLocalEnvs().entrySet()) {
                if (getServerInfo().isWindowsShell()) {
                    envs.append(String.format("set %s=%s&", entry.getKey(), entry.getValue()));
                } else {
                    envs.append(String.format("export \"%s=%s\";", entry.getKey(), entry.getValue()));
                }
            }
            command = envs.toString() + command;
        }
        return command;
    }
}
