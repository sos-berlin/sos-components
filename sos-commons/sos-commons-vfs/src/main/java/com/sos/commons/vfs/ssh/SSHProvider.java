package com.sos.commons.vfs.ssh;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.credentialstore.keepass.SOSKeePassPath;
import com.sos.commons.exception.SOSRequiredArgumentMissingException;
import com.sos.commons.util.common.SOSCommandResult;
import com.sos.commons.util.common.SOSEnv;
import com.sos.commons.util.common.SOSTimeout;
import com.sos.commons.vfs.common.AProvider;
import com.sos.commons.vfs.common.AProviderArguments.Protocol;
import com.sos.commons.vfs.common.proxy.Proxy;
import com.sos.commons.vfs.ssh.common.SSHProviderArguments;
import com.sos.commons.vfs.ssh.common.SSHProviderArguments.AuthMethod;
import com.sos.commons.vfs.ssh.common.SSHShellInfo;
import com.sos.commons.vfs.ssh.exception.SOSAuthenticationFailedException;
import com.sos.commons.vfs.ssh.exception.SOSSFTPClientNotInitializedException;
import com.sos.commons.vfs.ssh.exception.SOSSSHCommandExitViolentlyException;

import net.schmizz.keepalive.KeepAliveProvider;
import net.schmizz.sshj.Config;
import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.Service;
import net.schmizz.sshj.common.Factory;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.common.SSHException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.FileMode;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.userauth.keyprovider.FileKeyProvider;
import net.schmizz.sshj.userauth.keyprovider.KeyFormat;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.userauth.keyprovider.KeyProviderUtil;
import net.schmizz.sshj.userauth.method.AuthKeyboardInteractive;
import net.schmizz.sshj.userauth.method.AuthPassword;
import net.schmizz.sshj.userauth.method.AuthPublickey;
import net.schmizz.sshj.userauth.method.PasswordResponseProvider;
import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.Resource;

public class SSHProvider extends AProvider<SSHProviderArguments> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SSHProvider.class);

    private Config config;
    private SSHClient sshClient;
    private SFTPClient sftpClient;

    private SSHShellInfo shellInfo;
    private String serverVersion;

    public SSHProvider(SSHProviderArguments args) {
        super(args);
    }

    @Override
    public void connect() throws Exception {
        createSSHClient();
        sshClient.connect(getArguments().getHost().getValue(), getArguments().getPort().getValue());
        authenticate();
        setKeepAlive();
        serverVersion = sshClient.getTransport().getServerVersion();// "OpenSSH_$version" OpenSSH_for_Windows_8.1. can be null
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
    public void mkdir(String path) throws Exception {
        if (sftpClient == null) {
            throw new SOSSFTPClientNotInitializedException();
        }
        sftpClient.mkdirs(path);
    }

    @Override
    public void rmdir(String path) throws Exception {// remove directory - all files and sub folders
        if (sftpClient == null) {
            throw new SOSSFTPClientNotInitializedException();
        }
        path = sftpClient.canonicalize(path);

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
    public void rm(String path) throws Exception {// remove file
        if (sftpClient == null) {
            throw new SOSSFTPClientNotInitializedException();
        }
        sftpClient.rm(sftpClient.canonicalize(path));
    }

    @Override
    public void rename(String oldpath, String newpath) throws Exception {
        if (sftpClient == null) {
            throw new SOSSFTPClientNotInitializedException();
        }
        sftpClient.rename(sftpClient.canonicalize(oldpath), sftpClient.canonicalize(newpath));
    }

    @Override
    public boolean fileExists(String path) {
        return is(path, FileMode.Type.REGULAR);
    }

    @Override
    public boolean directoryExists(String path) {
        return is(path, FileMode.Type.DIRECTORY);
    }

    @Override
    public long getSize(String path) throws Exception {
        FileAttributes attr = getFileAttributes(path);
        if (attr != null) {
            return attr.getSize();
        }
        return -1;
    }

    @Override
    public long getModificationTime(String path) throws Exception {
        FileAttributes attr = getFileAttributes(path);
        if (attr != null) {
            return attr.getMtime();
        }
        return -1;
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
            handleEnvs(session, env);
            Command cmd = session.exec(command);

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

    public SSHShellInfo getShellInfo() {
        if (shellInfo == null) {
            shellInfo = new SSHShellInfo(serverVersion, executeCommand("uname"));
        }
        return shellInfo;
    }

    private void handleEnvs(Session session, SOSEnv env) {
        if (env == null) {
            return;
        }
        // only global env vars
        if (env.getEnvVars() != null && env.getEnvVars().size() > 0) {
            env.getEnvVars().forEach((k, v) -> {
                try {
                    session.setEnvVar(k, v);
                } catch (Throwable e) {
                    LOGGER.error(String.format("[%s=%s]%s", k, v, e.toString()), e);
                }
            });
        }
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

    private FileAttributes getFileAttributes(String path) throws Exception {
        if (sftpClient == null) {
            throw new SOSSFTPClientNotInitializedException();
        }
        return sftpClient.stat(sftpClient.canonicalize(path));
    }

    private void createSSHClient() throws Exception {
        setConfig();
        setKeepAliveProvider();
        sshClient = new SSHClient(config);
        setHostKeyVerifier();
        setCompression();
        setTimeout();
        setProxy();
    }

    private void setConfig() {
        config = new DefaultConfig();
    }

    private void setKeepAliveProvider() {
        if (getArguments().getServerAliveInterval().getValue() != null) {
            config.setKeepAliveProvider(KeepAliveProvider.KEEP_ALIVE);
        }
    }

    private void setKeepAlive() {
        if (getArguments().getServerAliveInterval().getValue() != null) {
            sshClient.getConnection().getKeepAlive().setKeepAliveInterval(getArguments().getServerAliveInterval().getValue());
        }
    }

    private void setHostKeyVerifier() throws IOException {
        if (getArguments().getStrictHostkeyChecking().getValue()) {
            sshClient.loadKnownHosts();
        } else {
            // default OpenSSHKnownHosts
            sshClient.addHostKeyVerifier(new PromiscuousVerifier());
        }
    }

    private void setCompression() throws TransportException {
        if (getArguments().getUseZlibCompression().getValue()) {
            // TODO Factory
            // (needs JZlib in classpath)
            sshClient.useCompression();
        }
    }

    private void setTimeout() {
        sshClient.setTimeout(getArguments().getSocketTimeoutAsMs());
        sshClient.setConnectTimeout(getArguments().getConnectTimeoutAsMs());
    }

    public void setProxy() {
        Proxy proxy = getArguments().getProxy();
        if (proxy != null) {
            // client.setSocketFactory(new ProxySocketFactory(proxy));
        }
    }

    private void createSFTPClient() throws Exception {
        if (sshClient == null || !getArguments().getProtocol().getValue().equals(Protocol.SFTP)) {
            return;
        }
        sftpClient = sshClient.newSFTPClient();
    }

    private void authenticate() throws Exception {
        if (getArguments().getPreferredAuthentications().getValue() != null && getArguments().getPreferredAuthentications().getValue().size() > 0) {
            usePreferredAuthentications();
        } else if (getArguments().getRequiredAuthentications().getValue() != null && getArguments().getRequiredAuthentications().getValue()
                .size() > 0) {
            useRequiredAuthentications();
        } else {
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

    private AuthPassword getAuthPassword() throws SOSRequiredArgumentMissingException, UserAuthException, TransportException {
        if (getArguments().getPassword().getValue() == null) {
            throw new SOSRequiredArgumentMissingException(getArguments().getPassword().getName());
        }
        return new AuthPassword(getPasswordFinder(getArguments().getPassword().getValue()));
    }

    private AuthKeyboardInteractive getAuthKeyboardInteractive() throws SOSRequiredArgumentMissingException, UserAuthException, TransportException {
        if (getArguments().getPassword().getValue() == null) {
            throw new SOSRequiredArgumentMissingException(getArguments().getPassword().getName());
        }
        return new AuthKeyboardInteractive(new PasswordResponseProvider(getPasswordFinder(getArguments().getPassword().getValue())));
    }

    private AuthPublickey getAuthPublickey() throws Exception {

        if (getArguments().getUseKeyAgent().getValue()) {
            // TODO
        } else {
            KeyProvider keyProvider = null;
            // from Keepass attachment
            if (getArguments().getKeepassDatabase() != null) {
                org.linguafranca.pwdb.Entry<?, ?, ?, ?> ke = getArguments().getKeepassDatabaseEntry();
                try {
                    keyProvider = getKeyProvider(getArguments().getKeepassDatabase().getAttachment(ke, getArguments()
                            .getKeepassAttachmentPropertyName()));
                } catch (Exception e) {
                    String keePassPath = ke.getPath() + SOSKeePassPath.PROPERTY_PREFIX + getArguments().getKeepassAttachmentPropertyName();
                    throw new Exception(String.format("[keepass][%s]%s", keePassPath, e.toString()), e);
                }
            } else {// from File
                Path authFile = getArguments().getAuthFile().getValue();
                if (authFile == null) {
                    throw new SOSRequiredArgumentMissingException(getArguments().getAuthFile().getName());
                }
                if (getArguments().getPassphrase().getValue() != null) {
                    keyProvider = sshClient.loadKeys(authFile.toFile().getCanonicalPath(), getArguments().getPassphrase().getValue());
                } else {
                    keyProvider = sshClient.loadKeys(authFile.toFile().getCanonicalPath());
                }
            }
            return new AuthPublickey(keyProvider);
        }
        return null;
    }

    private KeyProvider getKeyProvider(byte[] privateKey) throws Exception {
        Reader r = null;
        try {
            KeyFormat kf = KeyProviderUtil.detectKeyFileFormat(new StringReader(new String(privateKey, "UTF-8")), false);
            r = new StringReader(new String(privateKey, "UTF-8"));
            FileKeyProvider kp = Factory.Named.Util.create(config.getFileKeyProviderFactories(), kf.toString());
            if (kp == null) {
                throw new SSHException("No provider available for " + kf + " key file");
            }

            PasswordFinder pf = getArguments().getPassphrase().getValue() == null ? null : getPasswordFinder(getArguments().getPassphrase()
                    .getValue());
            kp.init(r, pf);
            return kp;
        } catch (Throwable e) {
            throw e;
        } finally {
            if (r != null) {
                r.close();
            }
        }
    }

    private PasswordFinder getPasswordFinder(String password) {
        return new PasswordFinder() {

            @Override
            public char[] reqPassword(Resource<?> resource) {
                return password.toCharArray().clone();
            }

            @Override
            public boolean shouldRetry(Resource<?> resource) {
                return false;
            }

        };
    }

    public SSHClient getClient() {
        return sshClient;
    }
}
