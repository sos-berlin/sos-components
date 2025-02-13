package com.sos.yade.engine.delegators;

import com.sos.commons.vfs.common.AProviderContext;
import com.sos.commons.vfs.common.IProvider;
import com.sos.yade.engine.arguments.YADETargetArguments;

public class YADETargetProviderDelegator extends AYADEProviderDelegator {

    private final static String IDENTIFIER = "Target";
    private final static String LOG_PREFIX = "[" + IDENTIFIER + "]";

    public YADETargetProviderDelegator(IProvider provider, YADETargetArguments args) {
        super(provider, args);

        // set YADE specific ProviderContext
        provider.setContext(new AProviderContext() {

            @Override
            public String getLogPrefix() {
                return LOG_PREFIX;
            }
        });
    }

    @Override
    public YADETargetArguments getArgs() {
        return (YADETargetArguments) super.getArgs();
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public String getLogPrefix() {
        return LOG_PREFIX;
    }
}
