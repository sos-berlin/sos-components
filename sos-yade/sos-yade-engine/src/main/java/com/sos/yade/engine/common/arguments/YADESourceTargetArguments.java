package com.sos.yade.engine.common.arguments;

import com.sos.commons.vfs.common.AProviderArguments;

public class YADESourceTargetArguments {

    private AProviderArguments provider;
    private YADEProviderCommandArguments commands;

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
}
