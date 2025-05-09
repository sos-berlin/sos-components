package com.sos.yade.engine.commons.arguments;

import com.sos.commons.util.arguments.base.ASOSArguments;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.arguments.base.SOSArgument.DisplayMode;

public class YADENotificationMailServerArguments extends ASOSArguments {

    private SOSArgument<String> hostname = new SOSArgument<>("Hostname", false);
    private SOSArgument<Integer> port = new SOSArgument<>("Port", false, Integer.valueOf(25));
    private SOSArgument<String> account = new SOSArgument<>("Account", false);
    private SOSArgument<String> password = new SOSArgument<>("Password", false, DisplayMode.MASKED);

    private SOSArgument<String> queueDirectory = new SOSArgument<>("QueueDirectory", false);
    private SOSArgument<String> connectTimeout = new SOSArgument<>("ConnectTimeout", false, "30s");

    public boolean isEnabled() {
        return !hostname.isEmpty();
    }

    public SOSArgument<String> getHostname() {
        return hostname;
    }

    public SOSArgument<Integer> getPort() {
        return port;
    }

    public SOSArgument<String> getAccount() {
        return account;
    }

    public SOSArgument<String> getPassword() {
        return password;
    }

    public SOSArgument<String> getQueueDirectory() {
        return queueDirectory;
    }

    public SOSArgument<String> getConnectTimeout() {
        return connectTimeout;
    }
}
