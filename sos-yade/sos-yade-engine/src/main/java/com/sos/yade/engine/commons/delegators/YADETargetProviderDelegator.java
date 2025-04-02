package com.sos.yade.engine.commons.delegators;

import com.sos.commons.vfs.commons.AProviderContext;
import com.sos.commons.vfs.commons.IProvider;
import com.sos.yade.engine.commons.arguments.YADETargetArguments;

/** @apiNote COPY/MOVE operations */
public class YADETargetProviderDelegator extends AYADEProviderDelegator {

    public final static String IDENTIFIER = "Target";

    public YADETargetProviderDelegator(IProvider provider, YADETargetArguments args, String label) {
        super(provider, args, label == null ? IDENTIFIER : label);

        // set YADE specific ProviderContext
        final String providerLogPrefix = getLogPrefix();
        provider.setContext(new AProviderContext() {

            @Override
            public String getLogPrefix() {
                return providerLogPrefix;
            }
        });
    }

    @Override
    public YADETargetArguments getArgs() {
        return (YADETargetArguments) super.getArgs();
    }
}
