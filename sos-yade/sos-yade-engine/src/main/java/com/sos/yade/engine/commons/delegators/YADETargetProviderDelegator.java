package com.sos.yade.engine.commons.delegators;

import com.sos.commons.vfs.commons.AProvider;
import com.sos.commons.vfs.commons.AProviderContext;
import com.sos.yade.engine.commons.arguments.YADETargetArguments;
import com.sos.yade.engine.exceptions.YADEEngineInitializationException;

/** @apiNote COPY/MOVE operations */
public class YADETargetProviderDelegator extends AYADEProviderDelegator {

    public YADETargetProviderDelegator(AProvider<?, ?> provider, YADETargetArguments args) throws YADEEngineInitializationException {
        super(provider, args, false);

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
