package com.sos.commons.vfs.ssh;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.exception.SOSNoSuchFileException;
import com.sos.commons.exception.SOSRequiredArgumentMissingException;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.SOSCommandResult;
import com.sos.commons.util.common.SOSEnv;
import com.sos.commons.util.common.SOSTimeout;
import com.sos.commons.vfs.common.AProvider;
import com.sos.commons.vfs.common.AProviderArguments.Protocol;
import com.sos.commons.vfs.common.CredentialStoreResolver;
import com.sos.commons.vfs.common.proxy.Proxy;
import com.sos.commons.vfs.common.proxy.ProxySocketFactory;
import com.sos.commons.vfs.exception.SOSAuthenticationFailedException;
import com.sos.commons.vfs.ssh.common.SSHProviderArguments;
import com.sos.commons.vfs.ssh.common.SSHProviderArguments.AuthMethod;
import com.sos.commons.vfs.ssh.common.SSHProviderUtil;
import com.sos.commons.vfs.ssh.common.SSHServerInfo;
import com.sos.commons.vfs.ssh.exception.SOSSFTPClientNotInitializedException;
import com.sos.commons.vfs.ssh.exception.SOSSSHCommandExitViolentlyException;

import net.schmizz.keepalive.KeepAliveProvider;
import net.schmizz.sshj.Config;
import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.Service;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.FileMode;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.Response.StatusCode;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.sftp.SFTPException;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.userauth.method.AuthKeyboardInteractive;
import net.schmizz.sshj.userauth.method.AuthPassword;
import net.schmizz.sshj.userauth.method.AuthPublickey;
import net.schmizz.sshj.userauth.method.PasswordResponseProvider;
import net.schmizz.sshj.xfer.FileSystemFile;

public class SSHProvider extends AProvider<SSHProviderArguments> {

    private Config config;
    private SSHClient sshClient;
    private SFTPClient sftpClient;

    private SSHServerInfo serverInfo;
    /** e.g. "OpenSSH_$version" -> OpenSSH_for_Windows_8.1. Can be null. */
    private String serverVersion;

    public SSHProvider(SSHProviderArguments args) {
        super(args);
    }

    @Override
    public void connect() throws Exception {
        if (CredentialStoreResolver.resolve(getArguments(), getArguments().getPassphrase())) {
            CredentialStoreResolver.resolveAttachment(getArguments(), getArguments().getAuthFile());
        }

        createSSHClient();
        sshClient.connect(getArguments().getHost().getValue(), getArguments().getPort().getValue());
        authenticate();
        setKeepAlive();
        serverVersion = sshClient.getTransport().getServerVersion();
        createSFTPClient();
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
        if (sftpClient != null) {
            try {
                sftpClient.close();
            } catch (IOException e) {
            }
        }
        if (sshClient != null) {
            try {
                sshClient.close();
            } catch (IOException e) {
            }
        }
    }

    @Override
    public void createDirectory(String path) throws Exception {
        if (sftpClient == null) {
            throw new SOSSFTPClientNotInitializedException();
        }
        if (SOSString.isEmpty(path)) {
            throw new SOSMissingDataException("path");
        }
        sftpClient.mkdir(path);
    }

    @Override
    public void createDirectories(String path) throws Exception {
        if (sftpClient == null) {
            throw new SOSSFTPClientNotInitializedException();
        }
        if (SOSString.isEmpty(path)) {
            throw new SOSMissingDataException("path");
        }
        sftpClient.mkdirs(path);
    }

    @Override
    public void delete(String path) throws Exception {
        if (sftpClient == null) {
            throw new SOSSFTPClientNotInitializedException();
        }
        if (SOSString.isEmpty(path)) {
            throw new SOSMissingDataException("path");
        }
        try {
            path = sftpClient.canonicalize(path);
        } catch (SFTPException e) {
            throwException(e, path);
        }
        FileAttributes attr = getFileAttributes(path);
        switch (attr.getType()) {
        case DIRECTORY:
            deleteDirectories(path);
            break;
        case REGULAR:
            // case SYMLINK:
            sftpClient.rm(path);
            break;
        default:
            break;
        }
    }

    private void throwException(SFTPException e, String msg) throws Exception {
        StatusCode sc = e.getStatusCode();
        if (sc != null) {
            if (sc.equals(StatusCode.NO_SUCH_FILE) || sc.equals(StatusCode.NO_SUCH_PATH)) {
                throw new SOSNoSuchFileException(msg, e);
            }
        }
        throw e;
    }

    @Override
    public boolean deleteIfExists(String path) throws Exception {
        try {
            delete(path);
            return true;
        } catch (SOSNoSuchFileException e) {
            return false;
        }
    }

    /** Deletes all files and sub folders. */
    private void deleteDirectories(String path) throws Exception {
        if (SOSString.isEmpty(path)) {
            throw new SOSMissingDataException("path");
        }
        final Deque<RemoteResourceInfo> toRemove = new LinkedList<RemoteResourceInfo>();
        dirInfo(sftpClient, path, toRemove, true);
        while (!toRemove.isEmpty()) {
            RemoteResourceInfo resource = toRemove.pop();
            if (resource.isDirectory()) {
                sftpClient.rmdir(resource.getPath());
            } else if (resource.isRegularFile()) {
                sftpClient.rm(resource.getPath());
            }
        }
        sftpClient.rmdir(path);
    }

    @Override
    public void rename(String oldpath, String newpath) throws Exception {
        if (sftpClient == null) {
            throw new SOSSFTPClientNotInitializedException();
        }
        if (SOSString.isEmpty(oldpath) || SOSString.isEmpty(newpath)) {
            throw new SOSMissingDataException("oldpath or newpath");
        }
        try {
            oldpath = sftpClient.canonicalize(oldpath);
        } catch (SFTPException e) {
            throwException(e, oldpath);
        }

        sftpClient.rename(oldpath, newpath);
    }

    @Override
    public boolean exists(String path) {
        try {
            if (!SOSString.isEmpty(path)) {
                FileAttributes attr = getFileAttributes(path);
                if (attr != null) {
                    return true;
                }
            }
        } catch (Throwable e) {
        }
        return false;
    }

    @Override
    public boolean isFile(String path) {
        return is(path, FileMode.Type.REGULAR);
    }

    @Override
    public boolean isDirectory(String path) {
        return is(path, FileMode.Type.DIRECTORY);
    }

    @Override
    public long getSize(String path) throws Exception {
        try {
            FileAttributes attr = getFileAttributes(path);
            if (attr != null) {
                return attr.getSize();
            }
        } catch (SFTPException e) {
            throwException(e, path);
        }
        return -1;
    }

    @Override
    public long getModificationTime(String path) throws Exception {
        if (SOSString.isEmpty(path)) {
            throw new SOSMissingDataException("path");
        }
        try {
            FileAttributes attr = getFileAttributes(path);
            if (attr != null) {
                return attr.getMtime();
            }
        } catch (SFTPException e) {
            throwException(e, path);
        }
        return -1;
    }

    public void setProxy() {
        Proxy proxy = getArguments().getProxy();
        if (proxy != null) {
            sshClient.setSocketFactory(new ProxySocketFactory(proxy));
        }
    }

    public void put(String source, String target, int perm) throws Exception {
        put(source, target);
        sftpClient.chmod(target, perm);
    }

    public void put(String source, String target) throws Exception {
        if (sftpClient == null) {
            throw new SOSSFTPClientNotInitializedException();
        }
        sftpClient.put(new FileSystemFile(source), target);
    }

    public void get(String source, String target) throws Exception {
        if (sftpClient == null) {
            throw new SOSSFTPClientNotInitializedException();
        }
        sftpClient.get(sftpClient.canonicalize(source), new FileSystemFile(target));
    }

    @Override
    public SOSCommandResult executeCommand(String command) {
        return executeCommand(command, null, null);
    }

    @Override
    public SOSCommandResult executeCommand(String command, SOSTimeout timeout) {
        return executeCommand(command, timeout, null);
    }

    @Override
    public SOSCommandResult executeCommand(String command, SOSEnv env) {
        return executeCommand(command, null, env);
    }

    @Override
    public SOSCommandResult executeCommand(String command, SOSTimeout timeout, SOSEnv env) {
        SOSCommandResult result = new SOSCommandResult(command);
        if (sshClient == null) {
            return result;
        }

        try (Session session = sshClient.startSession()) {
            if (getArguments().getSimulateShell().getValue()) {
                session.allocateDefaultPTY();
            }
            command = handleEnvs(command, session, env);
            Command cmd = session.exec(command);

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
        } catch (Throwable e) {
            result.setException(e);
        }
        return result;
    }

    public SSHServerInfo getServerInfo() {
        if (serverInfo == null) {
            serverInfo = new SSHServerInfo(serverVersion, executeCommand("uname"));
        }
        return serverInfo;
    }

    private FileAttributes getFileAttributes(String path) throws Exception {
        if (sftpClient == null) {
            throw new SOSSFTPClientNotInitializedException();
        }
        if (SOSString.isEmpty(path)) {
            throw new SOSMissingDataException("path");
        }
        return sftpClient.stat(path);
    }

    public SSHClient getSSHClient() {
        return sshClient;
    }

    private void createSSHClient() throws Exception {
        setConfig();
        setKeepAliveProvider();
        sshClient = new SSHClient(config);
        setHostKeyVerifier();
        setCompression();
        setRemoteCharset();
        setTimeout();
        setProxy();
    }

    private void createSFTPClient() throws Exception {
        if (sshClient == null || !getArguments().getProtocol().getValue().equals(Protocol.SFTP)) {
            return;
        }
        sftpClient = sshClient.newSFTPClient();
    }

    private void setConfig() {
        config = new DefaultConfig();
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
        return new AuthPassword(SSHProviderUtil.getPasswordFinder(getArguments().getPassword().getValue()));
    }

    private AuthKeyboardInteractive getAuthKeyboardInteractive() throws SOSRequiredArgumentMissingException, UserAuthException, TransportException {
        if (getArguments().getPassword().isEmpty()) {
            throw new SOSRequiredArgumentMissingException(getArguments().getPassword().getName());
        }
        return new AuthKeyboardInteractive(new PasswordResponseProvider(SSHProviderUtil.getPasswordFinder(getArguments().getPassword().getValue())));
    }

    private AuthPublickey getAuthPublickey() throws Exception {
        // TODO Agent support getArguments().getUseKeyAgent().getValue()
        KeyProvider keyProvider = null;
        if (getArguments().getKeepassDatabase() != null) {   // from Keepass attachment
            keyProvider = SSHProviderUtil.getKeyProviderFromKeepass(config, getArguments());
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
                if (serverInfo.hasWindowsShell()) {
                    envs.append(String.format("set %s=%s&", entry.getKey(), entry.getValue()));
                } else {
                    envs.append(String.format("export \"%s=%s\";", entry.getKey(), entry.getValue()));
                }
            }
            command = envs.toString() + command;
        }
        return command;
    }

    private void dirInfo(SFTPClient client, String path, Deque<RemoteResourceInfo> result, boolean recursive) throws Exception {
        List<RemoteResourceInfo> infos = client.ls(path);// SFTPException: No such file
        for (RemoteResourceInfo resource : infos) {
            result.push(resource);
            if (recursive && resource.isDirectory()) {
                dirInfo(client, resource.getPath(), result, recursive);
            }
        }
    }

    private boolean is(String path, FileMode.Type type) {
        try {
            FileAttributes attr = getFileAttributes(path);
            if (attr != null) {
                return type.equals(attr.getType());
            }
        } catch (Throwable e) {
        }
        return false;
    }

}
