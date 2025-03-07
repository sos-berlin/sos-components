package com.sos.commons.vfs.ftp.commons;

import java.nio.file.Path;

import com.sos.commons.util.common.SOSArgument;
import com.sos.commons.util.common.SOSArgumentHelper.DisplayMode;
import com.sos.commons.vfs.commons.AProviderExtendedArguments;

public class FTPProviderArguments extends AProviderExtendedArguments {

    public enum TransferMode {
        ASCII, BINARY;
    }

    protected static final int DEFAULT_PORT = 21;

    private SOSArgument<Integer> port = new SOSArgument<Integer>("port", true);

    // Java Keystore/Truststore
    private SOSArgument<KeyStoreType> keystoreType = new SOSArgument<>("keystore_type", false, KeyStoreType.JKS);
    private SOSArgument<Path> keystoreFile = new SOSArgument<>("keystore_file", false);
    private SOSArgument<String> keystorePassword = new SOSArgument<>("keystore_password", false, DisplayMode.MASKED);

    // seconds
    private SOSArgument<Integer> connectTimeout = new SOSArgument<Integer>("connect_timeout", false, Integer.valueOf(0));
    // KeepAlive - timeout interval in seconds
    // ?TODO server_alive_count_max
    private SOSArgument<Long> serverAliveInterval = new SOSArgument<>("server_alive_interval", false, Long.valueOf(180));// YADE1 default

    private SOSArgument<Boolean> protocolCommandListener = new SOSArgument<>("protocol_command_listener", false, Boolean.valueOf(false));

    /** Passive mode for higher compatibility (client.enterLocalPassiveMode()). Prevents problems with Firewalls/NAT routers. */
    private SOSArgument<Boolean> passiveMode = new SOSArgument<>("passive_mode", false, Boolean.valueOf(false));

    private SOSArgument<TransferMode> transferMode = new SOSArgument<>("transfer_mode", false, TransferMode.BINARY);

    public FTPProviderArguments() {
        getProtocol().setValue(Protocol.FTP);
    }

    public SOSArgument<Integer> getPort() {
        if (port.isEmpty()) {
            port.setValue(Integer.valueOf(DEFAULT_PORT));
        }
        return port;
    }

    public boolean isBinaryTransferMode() {
        return TransferMode.BINARY.equals(transferMode.getValue());
    }

    public SOSArgument<KeyStoreType> getKeystoreType() {
        return keystoreType;
    }

    public SOSArgument<Path> getKeystoreFile() {
        return keystoreFile;
    }

    public SOSArgument<String> getKeystorePassword() {
        return keystorePassword;
    }

    public SOSArgument<Long> getServerAliveInterval() {
        return serverAliveInterval;
    }

    public SOSArgument<Integer> getConnectTimeout() {
        return connectTimeout;
    }

    public int getConnectTimeoutAsMs() {
        return asMs(connectTimeout);
    }

    public SOSArgument<Boolean> getProtocolCommandListener() {
        return protocolCommandListener;
    }

    public SOSArgument<Boolean> getPassiveMode() {
        return passiveMode;
    }

}
