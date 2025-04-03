package com.sos.yade.engine.commons.delegators;

import com.sos.commons.vfs.commons.AProviderContext;
import com.sos.commons.vfs.commons.IProvider;
import com.sos.yade.engine.commons.YADEDirectoryMapper;
import com.sos.yade.engine.commons.YADEProviderFile;
import com.sos.yade.engine.commons.arguments.YADESourceArguments;

/** @apiNote all operations */
public class YADESourceProviderDelegator extends AYADEProviderDelegator {

    private final YADEDirectoryMapper directoryMapper;

    public YADESourceProviderDelegator(IProvider provider, YADESourceArguments args) {
        super(provider, args);

        // set YADE specific ProviderContext
        final String providerLogPrefix = getLogPrefix();
        provider.setContext(new AProviderContext() {

            @Override
            public String getLogPrefix() {
                return providerLogPrefix;
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

}
