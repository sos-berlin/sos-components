package com.sos.yade.engine.delegators;

import com.sos.commons.vfs.common.AProviderContext;
import com.sos.commons.vfs.common.IProvider;
import com.sos.yade.engine.arguments.YADESourceArguments;

public class YADESourceProviderDelegator extends AYADEProviderDelegator {

    private final static String LOG_PREFIX = "[Source]";

    public YADESourceProviderDelegator(IProvider provider, YADESourceArguments args) {
        super(provider, args);

        // set YADE specific ProviderContext
        provider.setContext(new AProviderContext() {

            @Override
            public String getLogPrefix() {
                return LOG_PREFIX;
            }
        });
        // set YADE specific ProviderFile
        // Not sets YADEProviderFile.index because it can be changes (e.g. because of zeroBytes relaxed handling)
        provider.setProviderFileCreator(builder -> new YADEProviderFile(builder.getFullPath(), builder.getSize(), builder.getLastModifiedMillis(),
                args.isCheckSteadyStateEnabled()));

    }

    @Override
    public YADESourceArguments getArgs() {
        return (YADESourceArguments) super.getArgs();
    }

    @Override
    public String getLogPrefix() {
        return LOG_PREFIX;
    }

}
