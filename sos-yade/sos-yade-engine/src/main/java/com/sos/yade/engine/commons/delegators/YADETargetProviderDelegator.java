package com.sos.yade.engine.commons.delegators;

import com.sos.commons.vfs.commons.AProvider;
import com.sos.commons.vfs.commons.AProviderContext;
import com.sos.yade.engine.commons.arguments.YADETargetArguments;

/** @apiNote COPY/MOVE operations */
public class YADETargetProviderDelegator extends AYADEProviderDelegator {

    public YADETargetProviderDelegator(AProvider<?> provider, YADETargetArguments args) {
        super(provider, args);

        // set YADE specific ProviderContext
        final String label = getLabel();
        provider.setContext(new AProviderContext() {

            @Override
            public String getLabel() {
                return label;
            }
        });
    }

    @Override
    public YADETargetArguments getArgs() {
        return (YADETargetArguments) super.getArgs();
    }
}
