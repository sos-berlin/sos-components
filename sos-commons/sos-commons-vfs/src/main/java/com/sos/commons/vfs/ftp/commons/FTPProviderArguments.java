package com.sos.commons.vfs.ftp.commons;

import com.sos.commons.util.SOSString;
import com.sos.commons.util.arguments.base.ASOSArguments;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.arguments.base.SOSArgumentHelper;
import com.sos.commons.vfs.commons.AProviderArguments;
import com.sos.commons.vfs.exceptions.ProviderInitializationException;

public class FTPProviderArguments extends AProviderArguments {

    public enum TransferMode {

        ASCII, BINARY;

        public static TransferMode fromString(String type) {
            if (SOSString.isEmpty(type)) {
                return null;
            }
            return TransferMode.valueOf(type.trim().toUpperCase());
        }
    }

    protected static final int DEFAULT_PORT = 21;

    // seconds
    /** see {@link ASOSArguments#asSeconds(SOSArgument, long) */
    private SOSArgument<String> connectTimeout = new SOSArgument<>("ConnectTimeout", false, "0s");
    // KeepAlive - timeout interval in seconds
    // ?TODO server_alive_count_max
    private SOSArgument<Long> serverAliveInterval = new SOSArgument<>("ServerAliveInterval", false, Long.valueOf(180));// YADE1 default

    private SOSArgument<Boolean> protocolCommandListener = new SOSArgument<>("ProtocolCommandListener", false, Boolean.valueOf(false));

    /** Passive mode for higher compatibility (client.enterLocalPassiveMode()). Prevents problems with Firewalls/NAT routers. */
    private SOSArgument<Boolean> passiveMode = new SOSArgument<>("PassiveMode", false, Boolean.valueOf(false));

    private SOSArgument<TransferMode> transferMode = new SOSArgument<>("TransferMode", false, TransferMode.BINARY);

    public FTPProviderArguments() {
        getProtocol().setValue(Protocol.FTP);
        getPort().setDefaultValue(DEFAULT_PORT);
        getUser().setRequired(true);
    }

    // internal use to avoid calling the default empty FTPProviderArguments constructor when initializing FTPSProviderArguments
    protected FTPProviderArguments(Object dummy) {
        getUser().setRequired(true);
    }

    /** Overrides {@link AProviderArguments#getAccessInfo() */
    @Override
    public String getAccessInfo() throws ProviderInitializationException {
        return String.format("%s@%s:%s", getUser().getDisplayValue(), getHost().getDisplayValue(), getPort().getDisplayValue());
    }

    /** Overrides {@link AProviderArguments#getAdvancedAccessInfo() */
    @Override
    public String getAdvancedAccessInfo() {
        return null;
    }

    public boolean isBinaryTransferMode() {
        return TransferMode.BINARY.equals(transferMode.getValue());
    }

    public boolean isPassiveMode() {
        return passiveMode.isTrue();
    }

    public SOSArgument<Long> getServerAliveInterval() {
        return serverAliveInterval;
    }

    public SOSArgument<String> getConnectTimeout() {
        return connectTimeout;
    }

    public int getConnectTimeoutAsMillis() {
        return (int) SOSArgumentHelper.asMillis(connectTimeout);
    }

    public SOSArgument<Boolean> getProtocolCommandListener() {
        return protocolCommandListener;
    }

    public SOSArgument<Boolean> getPassiveMode() {
        return passiveMode;
    }

    public SOSArgument<TransferMode> getTransferMode() {
        return transferMode;
    }

    public String getTransferModeValue() {
        return transferMode.getValue() == null ? null : transferMode.getValue().name().toLowerCase();
    }
}
