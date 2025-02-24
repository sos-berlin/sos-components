package com.sos.yade.engine.delegators;

import com.sos.commons.vfs.common.AProviderContext;
import com.sos.commons.vfs.common.IProvider;
import com.sos.yade.engine.arguments.YADESourceArguments;

public class YADESourceProviderDelegator extends AYADEProviderDelegator {

    private final static String IDENTIFIER = "Source";
    private final static String LOG_PREFIX = "[" + IDENTIFIER + "]";

    private final YADEDirectoryMapper directoryMapper;

    public YADESourceProviderDelegator(IProvider provider, YADESourceArguments args) {
        super(provider, args);

        // set YADE specific ProviderContext
        provider.setContext(new AProviderContext() {

            @Override
            public String getLogPrefix() {
                return LOG_PREFIX;
            }
        });
        directoryMapper = new YADEDirectoryMapper();
        // if (getDirectory() != null) {
        // directories.addSourceDirectory(getDirectory().getPath());
        // }
        // set YADE specific ProviderFile
        // Not sets YADEProviderFile.index because it can be changes (e.g. because of zeroBytes relaxed handling)
        provider.setProviderFileCreator(builder -> new YADEProviderFile(provider, builder.getFullPath(), builder.getSize(), builder
                .getLastModifiedMillis(), directoryMapper, args.isCheckSteadyStateEnabled()));

    }

    public YADEDirectoryMapper getDirectoryMapper() {
        return directoryMapper;
    }

    @Override
    public YADESourceArguments getArgs() {
        return (YADESourceArguments) super.getArgs();
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
