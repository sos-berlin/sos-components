package com.sos.commons.vfs.ssh.common;

import java.nio.file.Path;
import java.util.List;

import com.sos.commons.util.common.SOSArgument;
import com.sos.commons.util.common.SOSArgumentHelper.DisplayMode;
import com.sos.commons.vfs.common.AProviderArguments;

public class SSHProviderArguments extends AProviderArguments {

    public enum AuthMethod {
        PASSWORD, PUBLICKEY, KEYBOARD_INTERACTIVE
    }

    private SOSArgument<Integer> port = new SOSArgument<Integer>("port", true, 22);

    // Authentication
    private SOSArgument<String> passphrase = new SOSArgument<String>("passphrase", false, DisplayMode.MASKED);
    private SOSArgument<Path> authFile = new SOSArgument<Path>("auth_file", false);
    private SOSArgument<AuthMethod> authMethod = new SOSArgument<AuthMethod>("auth_method", false, AuthMethod.PASSWORD);
    private SOSArgument<List<AuthMethod>> preferredAuthentications = new SOSArgument<List<AuthMethod>>("preferred_authentications", false);
    private SOSArgument<List<AuthMethod>> requiredAuthentications = new SOSArgument<List<AuthMethod>>("required_authentications", false);

    // Socket connect timeout in seconds based on socket.connect
    private SOSArgument<Integer> connectTimeout = new SOSArgument<Integer>("connect_timeout", false, 0);
    // Socket timeout SO_TIMEOUT in seconds based on socket.setSoTimeout
    private SOSArgument<Integer> socketTimeout = new SOSArgument<Integer>("socket_timeout", false, 0);

    // KeepAlive - timeout interval in seconds
    // ?TODO server_alive_count_max
    private SOSArgument<Integer> serverAliveInterval = new SOSArgument<Integer>("server_alive_interval", false);

    private SOSArgument<Boolean> useKeyAgent = new SOSArgument<Boolean>("use_keyagent", false, false);
    private SOSArgument<Boolean> strictHostkeyChecking = new SOSArgument<Boolean>("strict_hostkey_checking", false, false);
    private SOSArgument<Boolean> useZlibCompression = new SOSArgument<Boolean>("use_zlib_compression", false, false);
    private SOSArgument<Boolean> simulateShell = new SOSArgument<Boolean>("simulate_shell", false, false);

    public SSHProviderArguments() {
        getProtocol().setDefaultValue(Protocol.SFTP);
        getUser().setRequired(true);
    }

    public SOSArgument<Integer> getPort() {
        return port;
    }

    public SOSArgument<AuthMethod> getAuthMethod() {
        return authMethod;
    }

    public SOSArgument<String> getPassphrase() {
        return passphrase;
    }

    public SOSArgument<Path> getAuthFile() {
        return authFile;
    }

    public SOSArgument<List<AuthMethod>> getPreferredAuthentications() {
        return preferredAuthentications;
    }

    public SOSArgument<List<AuthMethod>> getRequiredAuthentications() {
        return requiredAuthentications;
    }

    public SOSArgument<Integer> getConnectTimeout() {
        return connectTimeout;
    }

    public Integer getConnectTimeoutAsMs() {
        return connectTimeout.getValue() == null ? 0 : connectTimeout.getValue() * 1_000;
    }

    public SOSArgument<Integer> getSocketTimeout() {
        return socketTimeout;
    }

    public Integer getSocketTimeoutAsMs() {
        return socketTimeout.getValue() == null ? 0 : socketTimeout.getValue() * 1_000;
    }

    public SOSArgument<Integer> getServerAliveInterval() {
        return serverAliveInterval;
    }

    public SOSArgument<Boolean> getUseKeyAgent() {
        return useKeyAgent;
    }

    public SOSArgument<Boolean> getStrictHostkeyChecking() {
        return strictHostkeyChecking;
    }

    public SOSArgument<Boolean> getUseZlibCompression() {
        return useZlibCompression;
    }

    public SOSArgument<Boolean> getSimulateShell() {
        return simulateShell;
    }
}
