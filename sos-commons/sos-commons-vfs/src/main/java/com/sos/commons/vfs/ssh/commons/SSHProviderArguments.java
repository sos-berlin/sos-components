package com.sos.commons.vfs.ssh.commons;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;

import com.sos.commons.util.arguments.base.ASOSArguments;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.arguments.base.SOSArgument.DisplayMode;
import com.sos.commons.util.arguments.base.SOSArgumentHelper;
import com.sos.commons.vfs.commons.AProviderArguments;
import com.sos.commons.vfs.exceptions.ProviderInitializationException;

public class SSHProviderArguments extends AProviderArguments {

    public static final String CLASS_KEY = "SSH_PROVIDER";

    // TODO add JSCH or ...
    public enum SSHProviderType {
        SSHJ
    }

    private static final int DEFAULT_PORT = 22;
    // TODO - dynamically
    private static String ARG_NAME_PREFERRED_AUTHENTICATIONS_DISPLAY_NAME = "PreferredAuthentications";
    private static String ARG_NAME_REQUIRED_AUTHENTICATIONS_DISPLAY_NAME = "RequiredAuthentications";
    private static String ARG_NAME_AUTH_METHOD_PASSWORD_DISPLAY_NAME = "AuthenticationMethodPassword";
    private static String ARG_NAME_AUTH_METHOD_PUBLICKEY_DISPLAY_NAME = "AuthenticationMethodPublickey";
    private static String ARG_NAME_AUTH_METHOD_KEYBOARD_INTERACTIVE_DISPLAY_NAME = "AuthenticationMethodKeyboardInteractive";

    // Authentication
    private SOSArgument<String> passphrase = new SOSArgument<>("passphrase", false, DisplayMode.MASKED);
    /** String because can contains cs:// syntax */
    private SOSArgument<String> authFile = new SOSArgument<>("auth_file", false);
    private SOSArgument<SSHAuthMethod> authMethod = new SOSArgument<>("auth_method", false);
    private SOSArgument<List<SSHAuthMethod>> preferredAuthentications = new SOSArgument<>("preferred_authentications", false);
    private SOSArgument<List<SSHAuthMethod>> requiredAuthentications = new SOSArgument<>("required_authentications", false);

    // Socket timeout SO_TIMEOUT in seconds based on socket.setSoTimeout
    // - Read Timeout Maximum time to wait for a response after the connection is established.
    /** see {@link ASOSArguments#asSeconds(SOSArgument, long) */
    private SOSArgument<String> socketTimeout = new SOSArgument<>("socket_timeout", false, "0");

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

    /** Overrides {@link AProviderArguments#getAccessInfo() */
    @Override
    public String getAccessInfo() throws ProviderInitializationException {
        return String.format("%s@%s:%s", getUser().getDisplayValue(), getHost().getDisplayValue(), getPort().getDisplayValue());
    }

    /** Overrides {@link AProviderArguments#getAdvancedAccessInfo() */
    @Override
    public String getAdvancedAccessInfo() {
        if (!getRequiredAuthentications().isEmpty()) {
            return ARG_NAME_REQUIRED_AUTHENTICATIONS_DISPLAY_NAME + "=" + getRequiredAuthenticationsAsString();
        }
        if (!getPreferredAuthentications().isEmpty()) {
            return ARG_NAME_PREFERRED_AUTHENTICATIONS_DISPLAY_NAME + "=" + getPreferredAuthenticationsAsString();
        }
        if (!getAuthMethod().isEmpty()) {
            switch (getAuthMethod().getValue()) {
            case PASSWORD:
                return ARG_NAME_AUTH_METHOD_PASSWORD_DISPLAY_NAME;
            case PUBLICKEY:
                return ARG_NAME_AUTH_METHOD_PUBLICKEY_DISPLAY_NAME;
            case KEYBOARD_INTERACTIVE:
                return ARG_NAME_AUTH_METHOD_KEYBOARD_INTERACTIVE_DISPLAY_NAME;
            default:
                break;
            }
        }
        return null;
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

    public String getPreferredAuthenticationsAsString() {
        return SSHAuthMethod.toString(preferredAuthentications.getValue());
    }

    public SOSArgument<List<SSHAuthMethod>> getRequiredAuthentications() {
        return requiredAuthentications;
    }

    public String getRequiredAuthenticationsAsString() {
        return SSHAuthMethod.toString(requiredAuthentications.getValue());
    }

    public SOSArgument<String> getSocketTimeout() {
        return socketTimeout;
    }

    public int getSocketTimeoutAsMillis() {
        return (int) SOSArgumentHelper.asMillis(socketTimeout);
    }

    public SOSArgument<String> getServerAliveInterval() {
        return serverAliveInterval;
    }

    public int getServerAliveIntervalAsSeconds() {
        return (int) SOSArgumentHelper.asSeconds(serverAliveInterval, 0L);
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
