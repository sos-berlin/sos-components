package com.sos.yade.engine.common.delegators;

import com.sos.commons.vfs.common.AProviderContext;
import com.sos.commons.vfs.common.IProvider;
import com.sos.yade.engine.common.YADEDirectoryMapper;
import com.sos.yade.engine.common.YADEProviderFile;
import com.sos.yade.engine.common.arguments.YADESourceArguments;

/** @apiNote all operations */
public class YADESourceProviderDelegator extends AYADEProviderDelegator {

    private final static String IDENTIFIER = "Source";
    public final static String LOG_PREFIX = "[" + IDENTIFIER + "]";

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
        provider.setProviderFileCreator(builder -> new YADEProviderFile(this, builder.getFullPath(), builder.getSize(), builder
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
