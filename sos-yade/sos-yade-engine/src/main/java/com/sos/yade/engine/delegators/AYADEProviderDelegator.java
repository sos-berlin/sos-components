package com.sos.yade.engine.delegators;

import com.sos.commons.vfs.common.IProvider;
import com.sos.yade.engine.arguments.YADESourceTargetArguments;

public abstract class AYADEProviderDelegator implements IYADEProviderDelegator {

    private final IProvider provider;
    private final YADESourceTargetArguments args;

    public AYADEProviderDelegator(IProvider provider, YADESourceTargetArguments args) {
        this.provider = provider;
        this.args = args;
    }

    @Override
    public IProvider getProvider() {
        return provider;
    }

    @Override
    public YADESourceTargetArguments getArgs() {
        return args;
    }

}
