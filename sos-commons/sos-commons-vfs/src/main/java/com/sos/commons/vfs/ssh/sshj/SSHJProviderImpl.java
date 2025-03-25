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
import com.sos.commons.vfs.commons.AProvider;
import com.sos.commons.vfs.commons.AProviderArguments.FileType;
import com.sos.commons.vfs.commons.IProvider;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.commons.vfs.commons.file.files.DeleteFilesResult;
import com.sos.commons.vfs.commons.file.files.RenameFilesResult;
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
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.connection.channel.direct.Signal;
import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.FileMode;
import net.schmizz.sshj.sftp.OpenMode;
import net.schmizz.sshj.sftp.RemoteFile;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.xfer.FileSystemFile;

public class SSHJProviderImpl extends SSHProvider {

    private SSHClient sshClient;
    private Map<String, Command> commands = new ConcurrentHashMap<>();

    public SSHJProviderImpl(ISOSLogger logger, SSHProviderArguments args) throws ProviderInitializationException {
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

            sshClient = SSHJClientFactory.createAuthenticatedClient(getArguments(), getProxyProvider());
            setServerVersion(sshClient.getTransport().getServerVersion());
            getServerInfo();

            getLogger().info(getConnectedMsg(SSHJProviderUtils.getConnectedInfos(sshClient)));
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
        return sshClient == null ? false : sshClient.isConnected();
    }

    /** Overrides {@link IProvider#disconnect()} */
    @Override
    public void disconnect() {
        if (sshClient == null) {
            commands.clear();
            return;
        }

        commands.clear();
        SOSClassUtil.closeQuietly(sshClient);

        sshClient = null;
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
            FileAttributes r = (FileAttributes) fileRepresentator;
            return (FileMode.Type.REGULAR.equals(r.getType()) && getArguments().getValidFileTypes().getValue().contains(FileType.REGULAR))
                    || (FileMode.Type.SYMLINK.equals(r.getType()) && getArguments().getValidFileTypes().getValue().contains(FileType.SYMLINK));
        });

        String directory = selection.getConfig().getDirectory() == null ? "." : selection.getConfig().getDirectory();
        List<ProviderFile> result = new ArrayList<>();
        try {
            SSHJProviderUtils.selectFiles(this, selection, directory, result);
        } catch (ProviderException e) {
            throw e;
        }
        return result;
    }

    /** Overrides {@link IProvider#exists(String)} */
    @Override
    public boolean exists(String path) throws ProviderException {
        validatePrerequisites("exists", path, "path");

        try (SFTPClient sftp = sshClient.newSFTPClient()) {
            return SSHJProviderUtils.exists(sftp, path);
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#createDirectoriesIfNotExists(String)} */
    @Override
    public boolean createDirectoriesIfNotExists(String path) throws ProviderException {
        validatePrerequisites("createDirectoriesIfNotExists", path, "path");

        try (SFTPClient sftp = sshClient.newSFTPClient()) {
            if (SSHJProviderUtils.exists(sftp, path)) {
                return false;
            }
            sftp.mkdirs(path);
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("%s[createDirectoriesIfNotExists][%s]created", getLogPrefix(), path);
            }
            return true;
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    // TODO test if not exists ....
    /** Overrides {@link IProvider#deleteIfExists(String)} */
    @Override
    public boolean deleteIfExists(String path) throws ProviderException {
        validatePrerequisites("deleteIfExists", path, "path");

        try {
            try (SFTPClient sftp = sshClient.newSFTPClient()) {
                SSHJProviderUtils.delete(sftp, path);
                return true;
            } catch (SOSNoSuchFileException e) {
                return false;
            }
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e.getCause());
        }
    }

    /** Overrides {@link IProvider#deleteFilesIfExists(Collection, boolean)}<br/>
     * Note - deleteIfExists(String) is not used because an SFTPClient client is used to delete all files<br/>
     */
    @Override
    public DeleteFilesResult deleteFilesIfExists(Collection<String> files, boolean stopOnSingleFileError) throws ProviderException {
        if (files == null) {
            return null;
        }
        validatePrerequisites("deleteFilesIfExists");

        DeleteFilesResult r = new DeleteFilesResult(files.size());
        try (SFTPClient sftp = sshClient.newSFTPClient()) {
            l: for (String file : files) {
                try {
                    if (SSHJProviderUtils.exists(sftp, file)) {
                        SSHJProviderUtils.delete(sftp, file);
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

        try (SFTPClient sftp = sshClient.newSFTPClient()) {
            if (SSHJProviderUtils.exists(sftp, source)) {
                SSHJProviderUtils.rename(sftp, source, target);
                return true;
            }
            return false;
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(source + "->" + target), e);
        }
    }

    /** Overrides {@link IProvider#renameFilesIfSourceExists(Map, boolean)}<br/>
     * Note - renameFileIfSourceExists(String,String) is not used because an SFTPClient client is used to rename all files<br/>
     */
    @Override
    public RenameFilesResult renameFilesIfSourceExists(Map<String, String> files, boolean stopOnSingleFileError) throws ProviderException {
        if (files == null) {
            return null;
        }
        validatePrerequisites("renameFilesIfSourceExists");

        RenameFilesResult r = new RenameFilesResult(files.size());
        try (SFTPClient sftp = sshClient.newSFTPClient()) {
            l: for (Map.Entry<String, String> entry : files.entrySet()) {
                String source = entry.getKey();
                String target = entry.getValue();
                try {
                    if (SSHJProviderUtils.exists(sftp, source)) {
                        SSHJProviderUtils.rename(sftp, source, target);
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
            new ProviderException(e);
        }
        return r;
    }

    /** Overrides {@link IProvider#getFileIfExists(String)} */
    @Override
    public ProviderFile getFileIfExists(String path) throws ProviderException {
        validatePrerequisites("getFileIfExists", path, "path");

        try (SFTPClient sftp = sshClient.newSFTPClient()) {
            return createProviderFile(path, sftp.stat(path));
        } catch (NoSuchFileException e) {
            return null;
        } catch (IOException e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#getFileContentIfExists(String)} */
    @Override
    public String getFileContentIfExists(String path) throws ProviderException {
        validatePrerequisites("getFileContentIfExists", path, "path");

        try (SFTPClient sftp = sshClient.newSFTPClient()) {
            if (!SSHJProviderUtils.exists(sftp, path)) {
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
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#writeFile(String,String)} */
    @Override
    public void writeFile(String path, String content) throws ProviderException {
        validatePrerequisites("writeFile", path, "path");

        EnumSet<OpenMode> mode = EnumSet.of(OpenMode.WRITE, OpenMode.CREAT, OpenMode.TRUNC);
        try (SFTPClient sftp = sshClient.newSFTPClient()) {
            try (RemoteFile remoteFile = sftp.open(path, mode)) {
                remoteFile.write(0, content.getBytes(StandardCharsets.UTF_8), 0, content.length());
            }
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#setFileLastModifiedFromMillis(String,long)} */
    @Override
    public void setFileLastModifiedFromMillis(String path, long milliseconds) throws ProviderException {
        validatePrerequisites("setFileLastModifiedFromMillis", path, path);
        validateModificationTime(path, milliseconds);

        try (SFTPClient sftp = sshClient.newSFTPClient()) {
            FileAttributes attr = sftp.stat(path);
            long seconds = milliseconds / 1_000L;
            FileAttributes newAttr = new FileAttributes.Builder().withAtimeMtime(attr.getAtime(), seconds).build();
            sftp.setattr(path, newAttr);
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#getInputStream(String)} */
    @Override
    public InputStream getInputStream(String path) throws ProviderException {
        validatePrerequisites("getInputStream", path, "path");

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
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#getOutputStream(String,boolean)} */
    @Override
    public OutputStream getOutputStream(String path, boolean append) throws ProviderException {
        validatePrerequisites("getOutputStream", path, "path");

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
            throw new ProviderException(getPathOperationPrefix(path), e);
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

    /** Overrides {@link AProvider#validatePrerequisites(String)} */
    @Override
    public void validatePrerequisites(String method) throws ProviderException {
        if (sshClient == null) {
            throw new ProviderClientNotInitializedException(getLogPrefix() + method + "SSHClient");
        }
    }

    /** SSH Provider specific methods */

    /** Overrides {@link ASSHProvider#put(String, String, int)} */
    @Override
    public void put(String source, String target, int perm) throws ProviderException {
        try (SFTPClient sftp = sshClient.newSFTPClient()) {
            SSHJProviderUtils.put(sftp, source, target);
            sftp.chmod(target, perm);
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(source) + "[" + target + "][perm=" + perm + "]", e);
        }
    }

    /** Overrides {@link ASSHProvider#put(String, String)} */
    @Override
    public void put(String source, String target) throws ProviderException {
        try (SFTPClient sftp = sshClient.newSFTPClient()) {
            SSHJProviderUtils.put(sftp, source, target);
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(source) + "[" + target + "]", e);
        }
    }

    /** Overrides {@link ASSHProvider#get(String, String)} */
    @Override
    public void get(String source, String target) throws ProviderException {
        try (SFTPClient sftp = sshClient.newSFTPClient()) {
            sftp.get(sftp.canonicalize(source), new FileSystemFile(target));
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(source) + "[" + target + "]", e);
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

    protected ProviderFile createProviderFile(String path, FileAttributes attr) {
        return createProviderFile(path, attr.getSize(), SSHJProviderUtils.getFileLastModifiedMillis(attr));
    }

    private void validatePrerequisites(String method, String argValue, String msg) throws ProviderException {
        validatePrerequisites(method);
        validateArgument(method, argValue, msg);
    }

}
