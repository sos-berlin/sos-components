package com.sos.yade.engine.delegators;

import com.sos.commons.vfs.common.AProviderContext;
import com.sos.commons.vfs.common.IProvider;
import com.sos.commons.vfs.common.file.ProviderDirectoryPath;
import com.sos.yade.engine.arguments.YADESourceArguments;

public class YADESourceProviderDelegator extends AYADEProviderDelegator {

    private final static String LOG_PREFIX = "[Source]";
    private final ProviderDirectoryPath directory;

    public YADESourceProviderDelegator(IProvider provider, YADESourceArguments args) {
        super(provider, args);
        directory = provider.getDirectoryPath(args.getDirectory().getValue());
        provider.setContext(new AProviderContext() {

            @Override
            public String getLogPrefix() {
                return LOG_PREFIX;
            }
        });
    }

    @Override
    public YADESourceArguments getArgs() {
        return (YADESourceArguments) super.getArgs();
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
