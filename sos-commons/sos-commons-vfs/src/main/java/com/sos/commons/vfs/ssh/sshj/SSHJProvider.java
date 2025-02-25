package com.sos.commons.vfs.ssh.sshj;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.sos.commons.exception.SOSNoSuchFileException;
import com.sos.commons.exception.SOSRequiredArgumentMissingException;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.SOSCommandResult;
import com.sos.commons.util.common.SOSEnv;
import com.sos.commons.util.common.SOSTimeout;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.commons.vfs.common.file.files.DeleteFilesResult;
import com.sos.commons.vfs.common.file.files.RenameFilesResult;
import com.sos.commons.vfs.common.file.selection.ProviderFileSelection;
import com.sos.commons.vfs.common.proxy.Proxy;
import com.sos.commons.vfs.common.proxy.ProxySocketFactory;
import com.sos.commons.vfs.exception.SOSAuthenticationFailedException;
import com.sos.commons.vfs.exception.SOSProviderConnectException;
import com.sos.commons.vfs.exception.SOSProviderException;
import com.sos.commons.vfs.exception.SOSProviderInitializationException;
import com.sos.commons.vfs.ssh.common.ASSHProvider;
import com.sos.commons.vfs.ssh.common.SSHProviderArguments;
import com.sos.commons.vfs.ssh.common.SSHProviderArguments.AuthMethod;
import com.sos.commons.vfs.ssh.exceptions.SOSSSHClientNotInitializedException;
import com.sos.commons.vfs.ssh.exceptions.SOSSSHCommandExitViolentlyException;

import net.schmizz.keepalive.KeepAliveProvider;
import net.schmizz.sshj.Config;
import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.Service;
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
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.userauth.method.AuthKeyboardInteractive;
import net.schmizz.sshj.userauth.method.AuthPassword;
import net.schmizz.sshj.userauth.method.AuthPublickey;
import net.schmizz.sshj.userauth.method.PasswordResponseProvider;
import net.schmizz.sshj.xfer.FileSystemFile;

public class SSHJProvider extends ASSHProvider {

    private Config config;
    private SSHClient sshClient;

    private Map<String, Command> commands = new ConcurrentHashMap<>();

    public SSHJProvider(ISOSLogger logger, SSHProviderArguments args) throws SOSProviderInitializationException {
        super(logger, args);
    }

    @Override
    public void connect() throws SOSProviderConnectException {
        if (SOSString.isEmpty(getArguments().getHost().getValue())) {
            throw new SOSProviderConnectException(new SOSRequiredArgumentMissingException("host"));
        }
        try {
            getLogger().info(getConnectMsg());

            createSSHClient();
            sshClient.connect(getArguments().getHost().getValue(), getArguments().getPort().getValue());
            authenticate();
            setKeepAlive();
            setServerVersion(sshClient.getTransport().getServerVersion());
            getServerInfo();

            getLogger().info(getConnectedMsg(SSHJProviderUtil.getConnectedInfos(sshClient)));
        } catch (Throwable e) {
            throw new SOSProviderConnectException(String.format("[%s]", getMainInfo()), e);
        }
    }

    @Override
    public boolean isConnected() {
        if (sshClient == null) {
            return false;
        }
        return sshClient.isConnected();
    }

    @Override
    public void disconnect() {
        commands = new ConcurrentHashMap<>();
        closeQuietly(sshClient);
        sshClient = null;
        getLogger().info(getDisconnectedMsg());
    }

    @Override
    public boolean createDirectoriesIfNotExist(String path) throws SOSProviderException {
        checkBeforeOperation("createDirectoriesIfNotExist", sshClient, path, "path");
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

    @Override
    public DeleteFilesResult deleteFilesIfExist(Collection<String> files, boolean stopOnSingleFileError) throws SOSProviderException {
        if (files == null) {
            return null;
        }
        checkBeforeOperation("deleteFilesIfExist", sshClient);

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

    @Override
    public RenameFilesResult renameFilesIfExist(Map<String, String> files, boolean stopOnSingleFileError) throws SOSProviderException {
        if (files == null) {
            return null;
        }
        checkBeforeOperation("renameFilesIfExist", sshClient);

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

    @Override
    public List<ProviderFile> selectFiles(ProviderFileSelection selection) throws SOSProviderException {
        checkBeforeOperation("selectFiles", sshClient);

        selection = ProviderFileSelection.createIfNull(selection);
        String directory = selection.getConfig().getDirectory() == null ? "." : selection.getConfig().getDirectory();
        List<ProviderFile> result = new ArrayList<>();
        try {
            int counterAdded = 0;
            result.addAll(SSHJProviderUtil.selectFiles(getLogger(), getLogger().isDebugEnabled(), getLogPrefix(), sshClient, getProviderFileCreator(),
                    selection, directory, counterAdded));
        } catch (SOSProviderException e) {
            throw e;
        }
        return result;
    }

    @Override
    public ProviderFile getFileIfExists(String path) throws SOSProviderException {
        checkBeforeOperation("getFileIfExists", sshClient, path, "path");

        ProviderFile f = null;
        try (SFTPClient sftp = sshClient.newSFTPClient()) {
            FileAttributes attr = sftp.stat(path);
            if (attr != null && SSHJProviderUtil.isFileType(attr.getType())) {
                f = createProviderFile(path, attr.getSize(), SSHJProviderUtil.getFileLastModifiedMillis(attr));
            }
        } catch (NoSuchFileException e) {
        } catch (IOException e) {
            throw new SOSProviderException(getPathOperationPrefix(path), e);
        }
        return f;
    }

    @Override
    public ProviderFile rereadFileIfExists(ProviderFile file) throws SOSProviderException {
        checkBeforeOperation("rereadFileIfExists", sshClient);

        try (SFTPClient sftp = sshClient.newSFTPClient()) {
            FileAttributes attr = sftp.stat(file.getFullPath());
            if (attr != null && SSHJProviderUtil.isFileType(attr.getType())) {
                file.setSize(attr.getSize());
                file.setLastModifiedMillis(SSHJProviderUtil.getFileLastModifiedMillis(attr));
            } else {
                // file = null; ???
            }
        } catch (NoSuchFileException e) {
            file = null;
        } catch (IOException e) {
            throw new SOSProviderException(getPathOperationPrefix(file.getFullPath()), e);
        }
        return file;
    }

    @Override
    public boolean isDirectory(String path) {
        if (sshClient == null) {
            return false;
        }

        try (SFTPClient sftp = sshClient.newSFTPClient()) {
            checkParam("isDirectory", path, "path");
            return SSHJProviderUtil.is(getLogger(), getLogPrefix(), sftp, path, FileMode.Type.DIRECTORY);
        } catch (Throwable e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("%s[isDirectory=false]%s", getPathOperationPrefix(path), e.toString());
            }
            return false;
        }
    }

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

    private void setProxy() {
        Proxy proxy = getArguments().getProxy();
        if (proxy != null) {
            sshClient.setSocketFactory(new ProxySocketFactory(proxy));
        }
    }

    @Override
    public void put(String source, String target, int perm) throws SOSProviderException {
        try (SFTPClient sftp = sshClient.newSFTPClient()) {
            SSHJProviderUtil.put(sftp, source, target);
            sftp.chmod(target, perm);
        } catch (Throwable e) {
            throw new SOSProviderException(getPathOperationPrefix(source) + "[" + target + "][perm=" + perm + "]", e);
        }
    }

    @Override
    public void put(String source, String target) throws SOSProviderException {
        try (SFTPClient sftp = sshClient.newSFTPClient()) {
            SSHJProviderUtil.put(sftp, source, target);
        } catch (Throwable e) {
            throw new SOSProviderException(getPathOperationPrefix(source) + "[" + target + "]", e);
        }
    }

    @Override
    public void get(String source, String target) throws SOSProviderException {
        try (SFTPClient sftp = sshClient.newSFTPClient()) {
            sftp.get(sftp.canonicalize(source), new FileSystemFile(target));
        } catch (Throwable e) {
            throw new SOSProviderException(getPathOperationPrefix(source) + "[" + target + "]", e);
        }
    }

    @Override
    public SOSCommandResult executeCommand(String command, SOSTimeout timeout, SOSEnv env) {
        SOSCommandResult result = new SOSCommandResult(command);
        if (sshClient == null) {
            return result;
        }

        String uuid = getUUID();
        try (Session session = sshClient.startSession()) {
            if (getArguments().getSimulateShell().getValue()) {
                session.allocateDefaultPTY();
            }
            result.setCommand(handleEnvs(command, session, env));
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

    private synchronized String getUUID() {
        return UUID.randomUUID().toString();
    }

    // other thread
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

    private void resetCommand(String uuid) {
        commands.remove(uuid);
    }

    public SSHClient getSSHClient() {
        return sshClient;
    }

    private void createSSHClient() throws Exception {
        config = new DefaultConfig();
        setKeepAliveProvider();
        sshClient = new SSHClient(config);
        setHostKeyVerifier();
        setCompression();
        setRemoteCharset();
        setTimeout();
        setProxy();
    }

    private void setKeepAliveProvider() {
        if (!getArguments().getServerAliveInterval().isEmpty()) {
            config.setKeepAliveProvider(KeepAliveProvider.KEEP_ALIVE);
        }
    }

    private void setKeepAlive() {
        if (!getArguments().getServerAliveInterval().isEmpty()) {
            sshClient.getConnection().getKeepAlive().setKeepAliveInterval(getArguments().getServerAliveInterval().getValue());
        }
    }

    private void setHostKeyVerifier() throws IOException {
        // default HostKeyVerifier -> OpenSSHKnownHosts
        if (getArguments().getStrictHostkeyChecking().getValue()) {
            if (getArguments().getHostkeyLocation().isEmpty()) {
                sshClient.loadKnownHosts();// default search in <user.home>/.ssh/known_hosts|known_hosts2
            } else {
                sshClient.loadKnownHosts(getArguments().getHostkeyLocation().getValue().toFile());
            }
        } else {
            sshClient.addHostKeyVerifier(new PromiscuousVerifier());
        }
    }

    private void setCompression() throws TransportException {
        if (getArguments().getUseZlibCompression().getValue()) {
            // JCRAFT compression com.jcraft.jzlib-1.1.3.jar
            // uses default compression_level=6 (1-best speed, 9-best compression)
            // see JZlib.Z_DEFAULT_COMPRESSION(-1) and com.jcraft.jzlib.Deplate.deflateInit(with Z_DEFAULT_COMPRESSION))
            sshClient.useCompression();
        }
    }

    private void setRemoteCharset() throws TransportException {
        sshClient.setRemoteCharset(getArguments().getRemoteCharset().getValue());
    }

    private void setTimeout() {
        sshClient.setTimeout(getArguments().getSocketTimeoutAsMs());
        sshClient.setConnectTimeout(getArguments().getConnectTimeoutAsMs());
    }

    private void authenticate() throws Exception {
        if (!getArguments().getPreferredAuthentications().isEmpty()) {
            usePreferredAuthentications();
        } else if (!getArguments().getRequiredAuthentications().isEmpty()) {
            useRequiredAuthentications();
        } else {
            useAuthMethodAuthentication();
        }
    }

    private void usePreferredAuthentications() throws Exception {
        List<net.schmizz.sshj.userauth.method.AuthMethod> methods = new LinkedList<>();
        for (AuthMethod am : getArguments().getPreferredAuthentications().getValue()) {
            switch (am) {
            case PUBLICKEY:
                methods.add(getAuthPublickey());
                break;
            case PASSWORD:
                methods.add(getAuthPassword());
                break;
            case KEYBOARD_INTERACTIVE:
                methods.add(getAuthKeyboardInteractive());
                break;
            }
        }
        sshClient.auth(getArguments().getUser().getValue(), methods);
    }

    /** ssh(d)_config AuthenticationMethods */
    private void useRequiredAuthentications() throws Exception {
        for (AuthMethod am : getArguments().getRequiredAuthentications().getValue()) {
            switch (am) {
            case PUBLICKEY:
                partialAuthentication(getAuthPublickey());
                break;
            case PASSWORD:
                partialAuthentication(getAuthPassword());
                break;
            case KEYBOARD_INTERACTIVE:
                partialAuthentication(getAuthKeyboardInteractive());
                break;
            }
        }
    }

    private void partialAuthentication(net.schmizz.sshj.userauth.method.AuthMethod method) throws SOSAuthenticationFailedException, UserAuthException,
            TransportException {
        if (!sshClient.getUserAuth().authenticate(getArguments().getUser().getValue(), (Service) sshClient.getConnection(), method, sshClient
                .getTransport().getTimeoutMs())) {
            if (!sshClient.getUserAuth().hadPartialSuccess()) {
                throw new SOSAuthenticationFailedException();
            }
        }
    }

    private void useAuthMethodAuthentication() throws Exception {
        if (getArguments().getAuthMethod().getValue() == null) {
            throw new SOSRequiredArgumentMissingException(getArguments().getAuthMethod().getName());
        }
        net.schmizz.sshj.userauth.method.AuthMethod method = null;
        switch (getArguments().getAuthMethod().getValue()) {
        case PUBLICKEY:
            method = getAuthPublickey();
            break;
        case PASSWORD:
            method = getAuthPassword();
            break;
        case KEYBOARD_INTERACTIVE:
            method = getAuthKeyboardInteractive();
            break;
        }
        sshClient.auth(getArguments().getUser().getValue(), method);
    }

    private AuthPassword getAuthPassword() throws SOSRequiredArgumentMissingException, UserAuthException, TransportException {
        if (getArguments().getPassword().isEmpty()) {
            throw new SOSRequiredArgumentMissingException(getArguments().getPassword().getName());
        }
        return new AuthPassword(SSHJProviderUtil.getPasswordFinder(getArguments().getPassword().getValue()));
    }

    private AuthKeyboardInteractive getAuthKeyboardInteractive() throws SOSRequiredArgumentMissingException, UserAuthException, TransportException {
        if (getArguments().getPassword().isEmpty()) {
            throw new SOSRequiredArgumentMissingException(getArguments().getPassword().getName());
        }
        return new AuthKeyboardInteractive(new PasswordResponseProvider(SSHJProviderUtil.getPasswordFinder(getArguments().getPassword().getValue())));
    }

    private AuthPublickey getAuthPublickey() throws Exception {
        // TODO Agent support getArguments().getUseKeyAgent().getValue()
        KeyProvider keyProvider = null;
        // from KeePass attachment
        if (getArguments().getKeepassDatabase() != null && getArguments().getKeepassDatabaseEntry() != null && !SOSString.isEmpty(getArguments()
                .getKeepassAttachmentPropertyName())) {
            keyProvider = SSHJProviderUtil.getKeyProviderFromKeepass(sshClient, getArguments());
        } else {// from File
            if (SOSString.isEmpty(getArguments().getAuthFile().getValue())) {
                throw new SOSRequiredArgumentMissingException(getArguments().getAuthFile().getName());
            }
            Path authFile = Paths.get(getArguments().getAuthFile().getValue());
            if (getArguments().getPassphrase().isEmpty()) {
                keyProvider = sshClient.loadKeys(authFile.toFile().getCanonicalPath());
            } else {
                keyProvider = sshClient.loadKeys(authFile.toFile().getCanonicalPath(), getArguments().getPassphrase().getValue());
            }
        }
        return new AuthPublickey(keyProvider);
    }

    private String handleEnvs(String command, Session session, SOSEnv env) throws Exception {
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
                        closeQuietly(remoteFile);
                        closeQuietly(sftpRef.get());
                        close.set(true);
                    }
                }
            };
        } catch (Throwable e) {
            closeQuietly(sftpRef.get());
            throw new SOSProviderException(getPathOperationPrefix(path), e);
        }
    }

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
                        closeQuietly(remoteFile);
                        closeQuietly(sftpRef.get());
                        close.set(true);
                    }
                }
            };
        } catch (Throwable e) {
            closeQuietly(sftpRef.get());
            throw new SOSProviderException(getPathOperationPrefix(path), e);
        }
    }

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

    private void checkBeforeOperation(String method, SSHClient ssh) throws SOSProviderException {
        if (ssh == null) {
            throw new SOSSSHClientNotInitializedException(getLogPrefix() + method);
        }
    }

    private void checkBeforeOperation(String method, SSHClient ssh, String paramValue, String msg) throws SOSProviderException {
        checkBeforeOperation(method, ssh);
        checkParam(method, paramValue, msg);
    }

}
