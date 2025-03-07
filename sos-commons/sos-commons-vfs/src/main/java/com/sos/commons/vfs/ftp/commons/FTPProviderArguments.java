package com.sos.commons.vfs.ftp.commons;

import com.sos.commons.util.common.SOSArgument;
import com.sos.commons.vfs.commons.AProviderExtendedArguments;

public class FTPProviderArguments extends AProviderExtendedArguments {

    public enum TransferMode {
        ASCII, BINARY;
    }

    protected static final int DEFAULT_PORT = 21;

    // do not set the default value because SOSArgument.getValue() returns the default value
    // - so FTPSProviderArguments cannot check whether the port has been set or not
    private SOSArgument<Integer> port = new SOSArgument<>("port", false);

    // seconds
    private SOSArgument<Integer> connectTimeout = new SOSArgument<>("connect_timeout", false, Integer.valueOf(0));
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
