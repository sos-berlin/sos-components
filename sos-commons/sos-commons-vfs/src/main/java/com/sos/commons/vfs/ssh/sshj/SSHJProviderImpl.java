package com.sos.commons.vfs.ssh.sshj;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.sos.commons.exception.SOSNoSuchFileException;
import com.sos.commons.exception.SOSRequiredArgumentMissingException;
import com.sos.commons.util.SOSClassUtil;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.beans.SOSCommandResult;
import com.sos.commons.util.beans.SOSEnv;
import com.sos.commons.util.beans.SOSTimeout;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.commons.AProviderArguments.FileType;
import com.sos.commons.vfs.commons.IProvider;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.commons.vfs.commons.file.files.DeleteFilesResult;
import com.sos.commons.vfs.commons.file.files.RenameFilesResult;
import com.sos.commons.vfs.commons.file.selection.ProviderFileSelection;
import com.sos.commons.vfs.exceptions.SOSProviderClientNotInitializedException;
import com.sos.commons.vfs.exceptions.SOSProviderConnectException;
import com.sos.commons.vfs.exceptions.SOSProviderException;
import com.sos.commons.vfs.exceptions.SOSProviderInitializationException;
import com.sos.commons.vfs.ssh.commons.ASSHProvider;
import com.sos.commons.vfs.ssh.commons.SSHProviderArguments;
import com.sos.commons.vfs.ssh.exceptions.SOSSSHCommandExitViolentlyException;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.common.SSHException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.connection.channel.direct.Signal;
import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.FileMode;
import net.schmizz.sshj.sftp.OpenMode;
import net.schmizz.sshj.sftp.RemoteFile;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.xfer.FileSystemFile;

public class SSHJProviderImpl extends ASSHProvider {

    private SSHClient sshClient;
    private Map<String, Command> commands = new ConcurrentHashMap<>();

    public SSHJProviderImpl(ISOSLogger logger, SSHProviderArguments args) throws SOSProviderInitializationException {
        super(logger, args, fileRepresentator -> {
            if (fileRepresentator == null) {
                return false;
            }
            FileAttributes a = (FileAttributes) fileRepresentator;
            return (FileMode.Type.REGULAR.equals(a.getType()) && args.getValidFileTypes().getValue().contains(FileType.REGULAR))
                    || (FileMode.Type.SYMLINK.equals(a.getType()) && args.getValidFileTypes().getValue().contains(FileType.SYMLINK));
        });
    }

    /** Overrides {@link IProvider#connect()} */
    @Override
    public void connect() throws SOSProviderConnectException {
        if (SOSString.isEmpty(getArguments().getHost().getValue())) {
            throw new SOSProviderConnectException(new SOSRequiredArgumentMissingException("host"));
        }
        try {
            getLogger().info(getConnectMsg());

            sshClient = SSHClientFactory.createAuthenticatedClient(getArguments());
            setServerVersion(sshClient.getTransport().getServerVersion());
            getServerInfo();

            getLogger().info(getConnectedMsg(SSHJProviderUtil.getConnectedInfos(sshClient)));
        } catch (Throwable e) {
            throw new SOSProviderConnectException(String.format("[%s]", getAccessInfo()), e);
        }
    }

    /** Overrides {@link IProvider#isConnected()} */
    @Override
    public boolean isConnected() {
        if (sshClient == null) {
            return false;
        }
        return sshClient.isConnected();
    }

    /** Overrides {@link IProvider#disconnect()} */
    @Override
    public void disconnect() {
        commands.clear();
        SOSClassUtil.closeQuietly(sshClient);

        sshClient = null;
        getLogger().info(getDisconnectedMsg());
    }

    /** Overrides {@link IProvider#selectFiles(ProviderFileSelection)} */
    @Override
    public List<ProviderFile> selectFiles(ProviderFileSelection selection) throws SOSProviderException {
        checkBeforeOperation("selectFiles", sshClient);

        selection = ProviderFileSelection.createIfNull(selection);
        String directory = selection.getConfig().getDirectory() == null ? "." : selection.getConfig().getDirectory();
        List<ProviderFile> result = new ArrayList<>();
        try {
            SSHJProviderUtil.selectFiles(this, selection, directory, result);
        } catch (SOSProviderException e) {
            throw e;
        }
        return result;
    }

    /** Overrides {@link IProvider#exists(String)} */
    @Override
    public boolean exists(String path) {
        if (sshClient == null) {
            return false;
        }
        try (SFTPClient sftp = sshClient.newSFTPClient()) {
            checkParam("exists", path, "path"); // here because should not throw any errors
            return SSHJProviderUtil.exists(sftp, path);
        } catch (Throwable e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("%s[exists=false]%s", getPathOperationPrefix(path), e.toString());
            }
        }
        return false;
    }

    /** Overrides {@link IProvider#createDirectoriesIfNotExists(String)} */
    @Override
    public boolean createDirectoriesIfNotExists(String path) throws SOSProviderException {
        checkBeforeOperation("createDirectoriesIfNotExists", sshClient, path, "path");
        try (SFTPClient sftp = sshClient.newSFTPClient()) {
            if (SSHJProviderUtil.exists(sftp, path)) {
                return false;
            }
            sftp.mkdirs(path);
            return true;
        } catch (Throwable e) {
            throw new SOSProviderException(getPathOperationPrefix(path), e);
        }
    }

    // TODO test if not exists ....
    /** Overrides {@link IProvider#deleteIfExists(String)} */
    @Override
    public boolean deleteIfExists(String path) throws SOSProviderException {
        checkBeforeOperation("deleteIfExists", sshClient, path, "path");

        try {
            try (SFTPClient sftp = sshClient.newSFTPClient()) {
                SSHJProviderUtil.delete(sftp, path);
                return true;
            } catch (SOSProviderException e) {
                if (e.getCause() instanceof SOSNoSuchFileException) {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("%s[deleteIfExists]not exists", getPathOperationPrefix(path));
                    }
                    return false;
                }
                throw e;
            }
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
        checkBeforeOperation("deleteFilesIfExists", sshClient);

        DeleteFilesResult r = new DeleteFilesResult(files.size());
        try (SFTPClient sftp = sshClient.newSFTPClient()) {
            l: for (String file : files) {
                try {
                    if (SSHJProviderUtil.exists(sftp, file)) {
                        SSHJProviderUtil.delete(sftp, file);
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
        checkBeforeOperation("renameFilesIfSourceExists", sshClient);

        RenameFilesResult r = new RenameFilesResult(files.size());
        try (SFTPClient sftp = sshClient.newSFTPClient()) {
            l: for (Map.Entry<String, String> entry : files.entrySet()) {
                String source = entry.getKey();
                String target = entry.getValue();
                try {
                    if (SSHJProviderUtil.exists(sftp, source)) {
                        SSHJProviderUtil.rename(sftp, source, target);
                        r.addSuccess(source, target);
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
            new SOSProviderException(e);
        }
        return r;
    }

    /** Overrides {@link IProvider#getFileIfExists(String)} */
    @Override
    public ProviderFile getFileIfExists(String path) throws SOSProviderException {
        checkBeforeOperation("getFileIfExists", sshClient, path, "path");

        try (SFTPClient sftp = sshClient.newSFTPClient()) {
            return createProviderFile(path, sftp.stat(path));
        } catch (NoSuchFileException e) {
            return null;
        } catch (IOException e) {
            throw new SOSProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#rereadFileIfExists(ProviderFile)} */
    @Override
    public ProviderFile rereadFileIfExists(ProviderFile file) throws SOSProviderException {
        checkBeforeOperation("rereadFileIfExists", sshClient);

        try (SFTPClient sftp = sshClient.newSFTPClient()) {
            return refreshFileMetadata(file, createProviderFile(file.getFullPath(), sftp.stat(file.getFullPath())));
        } catch (NoSuchFileException e) {
            return null;
        } catch (IOException e) {
            throw new SOSProviderException(getPathOperationPrefix(file.getFullPath()), e);
        }
    }

    /** Overrides {@link IProvider#getFileContentIfExists(String)} */
    @Override
    public String getFileContentIfExists(String path) throws SOSProviderException {
        checkBeforeOperation("getFileContentIfExists", sshClient, path, "path");

        try (SFTPClient sftp = sshClient.newSFTPClient()) {
            if (!SSHJProviderUtil.exists(sftp, path)) {
                return null;
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (RemoteFile file = sftp.open(path); InputStream is = file.new RemoteFileInputStream(0)) {
                byte[] buffer = new byte[8_192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
            }
            return baos.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new SOSProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#writeFile(String,String)} */
    @Override
    public void writeFile(String path, String content) throws SOSProviderException {
        checkBeforeOperation("writeFile", sshClient, path, "path");

        EnumSet<OpenMode> mode = EnumSet.of(OpenMode.WRITE, OpenMode.CREAT, OpenMode.TRUNC);
        try (SFTPClient sftp = sshClient.newSFTPClient()) {
            try (RemoteFile remoteFile = sftp.open(path, mode)) {
                remoteFile.write(0, content.getBytes(StandardCharsets.UTF_8), 0, content.length());
            }
        } catch (Throwable e) {
            throw new SOSProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#setFileLastModifiedFromMillis(String,long)} */
    @Override
    public void setFileLastModifiedFromMillis(String path, long milliseconds) throws SOSProviderException {
        checkBeforeOperation("setFileLastModifiedFromMillis", sshClient, path, path);
        checkModificationTime(path, milliseconds);

        try (SFTPClient sftp = sshClient.newSFTPClient()) {
            FileAttributes attr = sftp.stat(path);
            long seconds = milliseconds / 1_000L;
            FileAttributes newAttr = new FileAttributes.Builder().withAtimeMtime(attr.getAtime(), seconds).build();
            sftp.setattr(path, newAttr);
        } catch (Throwable e) {
            throw new SOSProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#getInputStream(String)} */
    @Override
    public InputStream getInputStream(String path) throws SOSProviderException {
        checkBeforeOperation("getInputStream", sshClient, path, "path");

        final AtomicReference<SFTPClient> sftpRef = new AtomicReference<>();
        try {
            sftpRef.set(sshClient.newSFTPClient());
            RemoteFile remoteFile = sftpRef.get().open(path);
            return remoteFile.new ReadAheadRemoteFileInputStream(16) {

                private final AtomicBoolean close = new AtomicBoolean();

                @Override
                public void close() throws IOException {
                    if (close.get()) {
                        return;
                    }
                    try {
                        super.close();
                    } finally {
                        SOSClassUtil.closeQuietly(remoteFile);
                        SOSClassUtil.closeQuietly(sftpRef.get());
                        close.set(true);
                    }
                }
            };
        } catch (Throwable e) {
            SOSClassUtil.closeQuietly(sftpRef.get());
            throw new SOSProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#getOutputStream(String,boolean)} */
    @Override
    public OutputStream getOutputStream(String path, boolean append) throws SOSProviderException {
        checkBeforeOperation("getOutputStream", sshClient, path, "path");

        final AtomicReference<SFTPClient> sftpRef = new AtomicReference<>();
        try {
            sftpRef.set(sshClient.newSFTPClient());
            EnumSet<OpenMode> mode = EnumSet.of(OpenMode.WRITE, OpenMode.CREAT);
            if (append) {
                mode.add(OpenMode.APPEND);
            } else {
                // transferMode = ChannelSftp.RESUME; //TODO?
                mode.add(OpenMode.TRUNC);
            }
            RemoteFile remoteFile = sftpRef.get().open(path, mode);
            return remoteFile.new RemoteFileOutputStream(0, 16) {

                private final AtomicBoolean close = new AtomicBoolean();

                @Override
                public void close() throws IOException {
                    if (close.get()) {
                        return;
                    }
                    try {
                        super.close();
                    } finally {
                        SOSClassUtil.closeQuietly(remoteFile);
                        SOSClassUtil.closeQuietly(sftpRef.get());
                        close.set(true);
                    }
                }
            };
        } catch (Throwable e) {
            SOSClassUtil.closeQuietly(sftpRef.get());
            throw new SOSProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#executeCommand(String,SOSTinmeout,SOSEnv)} */
    @Override
    public SOSCommandResult executeCommand(String command, SOSTimeout timeout, SOSEnv env) {
        SOSCommandResult result = new SOSCommandResult(command);
        if (sshClient == null) {
            return result;
        }

        String uuid = createCommandIdentifier();
        try (Session session = sshClient.startSession()) {
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
                if (result.getExitCode() == null) {
                    if (cmd.getExitSignal() != null) {
                        throw new SOSSSHCommandExitViolentlyException(cmd.getExitSignal(), cmd.getExitErrorMessage());
                    }
                }
            }
        } catch (Throwable e) {
            result.setException(e);
        }
        resetCommand(uuid);
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

    /** SSH Provider specific methods */

    /** Overrides {@link ASSHProvider#put(String, String, int)} */
    @Override
    public void put(String source, String target, int perm) throws SOSProviderException {
        try (SFTPClient sftp = sshClient.newSFTPClient()) {
            SSHJProviderUtil.put(sftp, source, target);
            sftp.chmod(target, perm);
        } catch (Throwable e) {
            throw new SOSProviderException(getPathOperationPrefix(source) + "[" + target + "][perm=" + perm + "]", e);
        }
    }

    /** Overrides {@link ASSHProvider#put(String, String)} */
    @Override
    public void put(String source, String target) throws SOSProviderException {
        try (SFTPClient sftp = sshClient.newSFTPClient()) {
            SSHJProviderUtil.put(sftp, source, target);
        } catch (Throwable e) {
            throw new SOSProviderException(getPathOperationPrefix(source) + "[" + target + "]", e);
        }
    }

    /** Overrides {@link ASSHProvider#get(String, String)} */
    @Override
    public void get(String source, String target) throws SOSProviderException {
        try (SFTPClient sftp = sshClient.newSFTPClient()) {
            sftp.get(sftp.canonicalize(source), new FileSystemFile(target));
        } catch (Throwable e) {
            throw new SOSProviderException(getPathOperationPrefix(source) + "[" + target + "]", e);
        }
    }

    public SSHClient getSSHClient() {
        return sshClient;
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
                } catch (Throwable e) {
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
                if (getServerInfo().hasWindowsShell()) {
                    envs.append(String.format("set %s=%s&", entry.getKey(), entry.getValue()));
                } else {
                    envs.append(String.format("export \"%s=%s\";", entry.getKey(), entry.getValue()));
                }
            }
            command = envs.toString() + command;
        }
        return command;
    }

    private void checkBeforeOperation(String method, SSHClient ssh) throws SOSProviderException {
        if (ssh == null) {
            throw new SOSProviderClientNotInitializedException(getLogPrefix() + method + "SSHClient");
        }
    }

    private void checkBeforeOperation(String method, SSHClient ssh, String paramValue, String msg) throws SOSProviderException {
        checkBeforeOperation(method, ssh);
        checkParam(method, paramValue, msg);
    }

    protected ProviderFile createProviderFile(String path, FileAttributes attr) {
        if (!isValidFileType(attr)) {
            return null;
        }
        return createProviderFile(path, attr.getSize(), SSHJProviderUtil.getFileLastModifiedMillis(attr));
    }

}
