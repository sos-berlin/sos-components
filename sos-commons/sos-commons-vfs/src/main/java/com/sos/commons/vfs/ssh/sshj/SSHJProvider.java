package com.sos.commons.vfs.ssh.sshj;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.base.Joiner;
import com.sos.commons.credentialstore.CredentialStoreArguments;
import com.sos.commons.exception.SOSNoSuchFileException;
import com.sos.commons.exception.SOSRequiredArgumentMissingException;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.SOSCommandResult;
import com.sos.commons.util.common.SOSEnv;
import com.sos.commons.util.common.SOSTimeout;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.AProviderArguments.Protocol;
import com.sos.commons.vfs.common.proxy.Proxy;
import com.sos.commons.vfs.common.proxy.ProxySocketFactory;
import com.sos.commons.vfs.exception.SOSAuthenticationFailedException;
import com.sos.commons.vfs.exception.SOSProviderConnectException;
import com.sos.commons.vfs.exception.SOSProviderException;
import com.sos.commons.vfs.ssh.common.ASSHProvider;
import com.sos.commons.vfs.ssh.common.SSHProviderArguments;
import com.sos.commons.vfs.ssh.common.SSHProviderArguments.AuthMethod;
import com.sos.commons.vfs.ssh.exception.SOSSFTPClientNotInitializedException;
import com.sos.commons.vfs.ssh.exception.SOSSSHCommandExitViolentlyException;

import net.schmizz.keepalive.KeepAlive;
import net.schmizz.keepalive.KeepAliveProvider;
import net.schmizz.keepalive.KeepAliveRunner;
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

public class SSHJProvider extends ASSHProvider {

    private Config config;
    private SSHClient sshClient;
    private SFTPClient sftpClient;

    private Map<String, Command> commands = new ConcurrentHashMap<>();

    public SSHJProvider(ISOSLogger logger, SSHProviderArguments args, CredentialStoreArguments csArgs) throws Exception {
        super(logger, args, csArgs);
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
            createSFTPClient();
            getServerInfo();

            printConnectedInfos();
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
        List<String> msg = new ArrayList<>();
        if (sftpClient != null) {
            try {
                sftpClient.close();
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("[sftpClient]closed");
                }
            } catch (IOException e) {
                msg.add("[sftpClient]" + e.toString());
            } finally {
                sftpClient = null;
            }
        }
        if (sshClient != null) {
            try {
                sshClient.close();
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("[sshClient]closed");
                }
            } catch (IOException e) {
                msg.add("[sftpClient]" + e.toString());
            } finally {
                sshClient = null;
            }
        }
        if (msg.size() > 0) {
            getLogger().info("[disconnect][onClose]" + Joiner.on(", ").join(msg));
        }
        getLogger().info(getDisconnectedMsg());
    }

    @Override
    public void createDirectory(String path) throws SOSProviderException {
        checkBeforeOperation(path, "path");

        try {
            if (getLogger().isTraceEnabled()) {
                getLogger().trace("[createDirectory][%s]try to create...", path);
            }
            sftpClient.mkdir(path);
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[createDirectory][%s]created", path);
            }
        } catch (Throwable e) {
            throw new SOSProviderException("[createDirectory][" + path + "]", e);
        }
    }

    @Override
    public void createDirectories(String path) throws SOSProviderException {
        checkBeforeOperation(path, "path");

        try {
            if (getLogger().isTraceEnabled()) {
                getLogger().trace("[createDirectories][%s]try to create...", path);
            }
            sftpClient.mkdirs(path);
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[createDirectories][%s]created", path);
            }
        } catch (Throwable e) {
            throw new SOSProviderException("[createDirectories][" + path + "]", e);
        }
    }

    @Override
    public void delete(String path) throws SOSProviderException {
        checkBeforeOperation(path, "path");

        try {
            try {
                path = sftpClient.canonicalize(path);
            } catch (SFTPException e) {
                throwException(e, path);
            }
            FileAttributes attr = sftpClient.stat(path);
            switch (attr.getType()) {
            case DIRECTORY:
                deleteDirectories(path);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("[delete][directory][%s]deleted", path);
                }
                break;
            case REGULAR:
                sftpClient.rm(path);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("[delete][regular][%s]deleted", path);
                }
                break;
            // case SYMLINK:
            default:
                break;
            }
        } catch (Throwable e) {
            throw new SOSProviderException("[delete][" + path + "]", e);
        }
    }

    // TODO test if not exists ....
    @Override
    public boolean deleteIfExists(String path) throws SOSProviderException {
        try {
            try {
                delete(path);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("[deleteIfExists][%s]deleted", path);
                }
                return true;
            } catch (SOSProviderException e) {
                if (e.getCause() instanceof SOSNoSuchFileException) {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("[deleteIfExists][%s]not exists", path);
                    }
                    return false;
                }
                throw e;
            }
        } catch (Throwable e) {
            throw new SOSProviderException("[deleteIfExists][" + path + "]", e.getCause());
        }
    }

    /** Deletes all files and sub folders. */
    private void deleteDirectories(String path) throws Exception {
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
    public void rename(String source, String target) throws SOSProviderException {
        checkBeforeOperation(source, "source");
        checkParam(target, "target");

        try {
            try {
                source = sftpClient.canonicalize(source);
            } catch (SFTPException e) {
                throwException(e, source);
            }
            sftpClient.rename(source, target);
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[rename][source=%s][target=%s]renamed", source, target);
            }
        } catch (Throwable e) {
            throw new SOSProviderException("[rename][source=" + source + "][target=" + target + "]", e);
        }
    }

    @Override
    public boolean exists(String path) {
        try {
            checkBeforeOperation(path, "path"); // here because should not throw any errors

            FileAttributes attr = sftpClient.stat(path);
            if (attr != null) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("[exists][%s]true", path);
                }
                return true;
            }
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[exists][%s][false]attr=null", path);
            }
        } catch (Throwable e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[exists][%s][false]%s", path, e.toString());
            }
        }
        return false;
    }

    @Override
    public boolean isRegularFile(String path) {
        return is(path, FileMode.Type.REGULAR);
    }

    @Override
    public boolean isDirectory(String path) {
        return is(path, FileMode.Type.DIRECTORY);
    }

    @Override
    public Long getSize(String path) throws SOSProviderException {
        checkBeforeOperation(path, "path");

        try {
            FileAttributes attr = sftpClient.stat(path);
            if (attr != null) {
                Long result = Long.valueOf(attr.getSize());
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("[getSize][%s]%s", path, result);
                }
                return result;
            }
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[getSize][%s][null]attr=null", path);
            }
            return null;
        } catch (Throwable e) {
            throw new SOSProviderException("[getSize][" + path + "]", e);
        }
    }

    @Override
    public Long getLastModifiedMillis(String path) {
        try {
            checkBeforeOperation(path, "path");

            FileAttributes attr = sftpClient.stat(path);
            if (attr != null) {
                // getMtime is in seconds
                Long result = Long.valueOf(attr.getMtime() * 1_000L);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("[getLastModifiedMillis][%s]%s", path, result);
                }
                return result;
            }
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[getLastModifiedMillis][%s][null]attr=null", path);
            }
            return null;
        } catch (Throwable e) {
            getLogger().warn("[getLastModifiedMillis][%s]%s", path, e);
            return null;
        }
    }

    @Override
    public boolean setLastModifiedFromMillis(String path, Long milliseconds) {
        if (!isValidModificationTime(milliseconds)) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[setLastModifiedFromMillis][%s][%s][false]not valid modification time", path, milliseconds);
            }
            return false;
        }

        try {
            checkBeforeOperation(path, "path");

            FileAttributes attr = sftpClient.stat(path);
            if (attr != null) {
                long seconds = milliseconds / 1_000L;
                FileAttributes newAttr = new FileAttributes.Builder().withAtimeMtime(attr.getAtime(), seconds).build();
                sftpClient.setattr(path, newAttr);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("[setLastModifiedFromMillis][%s][seconds=%s]true", path, seconds);
                }
                return true;
            }
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[setLastModifiedFromMillis][%s][%s][false]attr=null", path, milliseconds);
            }
            return false;
        } catch (Throwable e) {
            getLogger().warn("[setLastModifiedFromMillis][%s][%s]%s", path, milliseconds, e);
            return false;
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
        put(source, target);
        try {
            sftpClient.chmod(target, perm);
        } catch (Throwable e) {
            throw new SOSProviderException("[put][source=" + source + "][target=" + target + "][perm=" + perm + "]", e);
        }
    }

    @Override
    public void put(String source, String target) throws SOSProviderException {
        if (sftpClient == null) {
            throw new SOSSFTPClientNotInitializedException();
        }
        try {
            sftpClient.put(new FileSystemFile(source), target);
        } catch (Throwable e) {
            throw new SOSProviderException("[put][source=" + source + "][target=" + target + "]", e);
        }
    }

    @Override
    public void get(String source, String target) throws SOSProviderException {
        if (sftpClient == null) {
            throw new SOSSFTPClientNotInitializedException();
        }
        try {
            sftpClient.get(sftpClient.canonicalize(source), new FileSystemFile(target));
        } catch (Throwable e) {
            throw new SOSProviderException("[get][source=" + source + "][target=" + target + "]", e);
        }
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
            checkBeforeOperation(path, "path");
            FileAttributes attr = sftpClient.stat(path);
            if (attr != null) {
                return type.equals(attr.getType());
            }
        } catch (Throwable e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[is][%s][type=%s]%s", path, type, e.toString());
            }
        }
        return false;
    }

    private void printConnectedInfos() {
        List<String> msg = new ArrayList<String>();
        if (sshClient.getTimeout() > 0 || sshClient.getConnectTimeout() > 0) {
            msg.add("ConnectTimeout=" + millis2string(sshClient.getConnectTimeout()) + ", SocketTimeout=" + millis2string(sshClient.getTimeout()));
        }
        if (sshClient.getConnection() != null) {
            KeepAlive r = sshClient.getConnection().getKeepAlive();
            if (r.getKeepAliveInterval() > 0) {
                if (r instanceof KeepAliveRunner) {
                    msg.add("KeepAliveInterval=" + r.getKeepAliveInterval() + "s, MaxAliveCount=" + ((KeepAliveRunner) r).getMaxAliveCount());
                } else {
                    msg.add("KeepAliveInterval=" + r.getKeepAliveInterval() + "s");
                }
            }
        }
        getLogger().info(getConnectedMsg(msg));
    }

    private void checkBeforeOperation(String paramValue, String msg) throws SOSProviderException {
        if (sftpClient == null) {
            throw new SOSSFTPClientNotInitializedException();
        }
        checkParam(paramValue, msg);
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

}
