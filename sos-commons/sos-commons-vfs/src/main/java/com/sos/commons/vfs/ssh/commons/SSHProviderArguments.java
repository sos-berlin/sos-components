package com.sos.commons.vfs.ssh.commons;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;

import com.sos.commons.util.arguments.base.ASOSArguments;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.arguments.base.SOSArgument.DisplayMode;
import com.sos.commons.vfs.commons.AProviderArguments;

public class SSHProviderArguments extends AProviderArguments {

    public static final String CLASS_KEY = "SSH_PROVIDER";

    // TODO add JSCH or ...
    public enum SSHProviderType {
        SSHJ
    }

    private static final int DEFAULT_PORT = 22;

    // Authentication
    private SOSArgument<String> passphrase = new SOSArgument<>("passphrase", false, DisplayMode.MASKED);
    /** String because can contains cs:// syntax */
    private SOSArgument<String> authFile = new SOSArgument<>("auth_file", false);
    private SOSArgument<SSHAuthMethod> authMethod = new SOSArgument<>("auth_method", false);
    private SOSArgument<List<SSHAuthMethod>> preferredAuthentications = new SOSArgument<>("preferred_authentications", false);
    private SOSArgument<List<SSHAuthMethod>> requiredAuthentications = new SOSArgument<>("required_authentications", false);

    // Socket connect timeout in seconds based on socket.connect
    /** see {@link ASOSArguments#asSeconds(SOSArgument, long) */
    private SOSArgument<String> connectTimeout = new SOSArgument<>("connect_timeout", false, "0s");
    // Socket timeout SO_TIMEOUT in seconds based on socket.setSoTimeout
    /** see {@link ASOSArguments#asSeconds(SOSArgument, long) */
    private SOSArgument<String> socketTimeout = new SOSArgument<>("socket_timeout", false, "0s");

    // KeepAlive - timeout interval in seconds
    /** see {@link ASOSArguments#asSeconds(SOSArgument, long) */
    private SOSArgument<String> serverAliveInterval = new SOSArgument<>("server_alive_interval", false);
    // SSHJ default - 5 (if KeepAlive is set)
    private SOSArgument<Integer> serverAliveCountMax = new SOSArgument<>("server_alive_count_max", false);

    private SOSArgument<Boolean> strictHostkeyChecking = new SOSArgument<>("strict_hostkey_checking", false, false);
    private SOSArgument<Path> hostkeyLocation = new SOSArgument<>("hostkey_location", false);
    private SOSArgument<Boolean> useZlibCompression = new SOSArgument<>("use_zlib_compression", false, false);
    private SOSArgument<Boolean> simulateShell = new SOSArgument<>("simulate_shell", false, false);

    private SOSArgument<Charset> remoteCharset = new SOSArgument<>("remote_charset", false, Charset.forName("UTF-8"));

    // "ssh_provider" - YADE 1 compatibility
    private SOSArgument<SSHProviderType> sshProviderType = new SOSArgument<>("ssh_provider", false, SSHProviderType.SSHJ);

    public SSHProviderArguments() {
        getProtocol().setDefaultValue(Protocol.SFTP);
        getPort().setDefaultValue(DEFAULT_PORT);
        getUser().setRequired(true);
    }

    public SOSArgument<SSHAuthMethod> getAuthMethod() {
        return authMethod;
    }

    public SOSArgument<String> getPassphrase() {
        return passphrase;
    }

    public SOSArgument<String> getAuthFile() {
        return authFile;
    }

    public SOSArgument<List<SSHAuthMethod>> getPreferredAuthentications() {
        return preferredAuthentications;
    }

    public SOSArgument<List<SSHAuthMethod>> getRequiredAuthentications() {
        return requiredAuthentications;
    }

    public SOSArgument<String> getConnectTimeout() {
        return connectTimeout;
    }

    public int getConnectTimeoutAsMillis() {
        return (int) asMillis(connectTimeout);
    }

    public SOSArgument<String> getSocketTimeout() {
        return socketTimeout;
    }

    public int getSocketTimeoutAsMillis() {
        return (int) asMillis(socketTimeout);
    }

    public SOSArgument<String> getServerAliveInterval() {
        return serverAliveInterval;
    }

    public int getServerAliveIntervalAsSeconds() {
        return (int) asSeconds(authFile, 0L);
    }

    public SOSArgument<Integer> getServerAliveCountMax() {
        return serverAliveCountMax;
    }

    public SOSArgument<Boolean> getStrictHostkeyChecking() {
        return strictHostkeyChecking;
    }

    public SOSArgument<Path> getHostkeyLocation() {
        return hostkeyLocation;
    }

    public SOSArgument<Boolean> getUseZlibCompression() {
        return useZlibCompression;
    }

    public SOSArgument<Boolean> getSimulateShell() {
        return simulateShell;
    }

    public SOSArgument<Charset> getRemoteCharset() {
        return remoteCharset;
    }

    public SOSArgument<SSHProviderType> getSSHProviderType() {
        return sshProviderType;
    }
}
