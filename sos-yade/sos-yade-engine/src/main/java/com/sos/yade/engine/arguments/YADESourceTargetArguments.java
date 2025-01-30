package com.sos.yade.engine.arguments;

import com.sos.commons.util.common.SOSArgument;
import com.sos.commons.vfs.common.AProviderArguments;

public class YADESourceTargetArguments {

    private AProviderArguments provider;
    private YADEProviderCommandArguments commands;

    private SOSArgument<Integer> connectionErrorRetryCountMax = new SOSArgument<>("connection_error_retry_count_max", false);
    private SOSArgument<String> connectionErrorRetryInterval = new SOSArgument<>("connection_error_retry_interval", false, "0s");

    public boolean isRetryOnConnectionErrorEnabled() {
        return connectionErrorRetryCountMax.getValue() != null && connectionErrorRetryCountMax.getValue().intValue() > 0;
    }

    public AProviderArguments getProvider() {
        return provider;
    }

    public void setProvider(AProviderArguments val) {
        provider = val;
    }

    public YADEProviderCommandArguments getCommands() {
        return commands;
    }

    public void setCommands(YADEProviderCommandArguments val) {
        commands = val;
    }

    public SOSArgument<Integer> getConnectionErrorRetryCountMax() {
        return connectionErrorRetryCountMax;
    }

    public SOSArgument<String> getConnectionErrorRetryInterval() {
        return connectionErrorRetryInterval;
    }
}
