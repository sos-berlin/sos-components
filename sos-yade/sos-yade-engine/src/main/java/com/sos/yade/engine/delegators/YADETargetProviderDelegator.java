package com.sos.yade.engine.delegators;

import com.sos.commons.vfs.common.AProviderContext;
import com.sos.commons.vfs.common.IProvider;
import com.sos.commons.vfs.common.file.ProviderDirectoryPath;
import com.sos.yade.engine.arguments.YADETargetArguments;

public class YADETargetProviderDelegator extends AYADEProviderDelegator {

    private final static String LOG_PREFIX = "[Target]";
    private final ProviderDirectoryPath directory;

    public YADETargetProviderDelegator(IProvider provider, YADETargetArguments args) {
        super(provider, args);
        directory = provider.getDirectoryPath(args.getDirectory().getValue());

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
    public ProviderDirectoryPath getDirectory() {
        return directory;
    }

    @Override
    public String getLogPrefix() {
        return LOG_PREFIX;
    }

}
