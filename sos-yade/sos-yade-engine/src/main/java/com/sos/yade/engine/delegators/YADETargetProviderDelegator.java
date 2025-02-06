package com.sos.yade.engine.delegators;

import com.sos.commons.vfs.common.AProviderContext;
import com.sos.commons.vfs.common.IProvider;
import com.sos.yade.engine.arguments.YADETargetArguments;
import com.sos.yade.engine.handlers.operations.YADECopyOrMoveOperationTargetFilesConfig;

public class YADETargetProviderDelegator extends AYADEProviderDelegator {

    private final static String LOG_PREFIX = "[Target]";

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
    
    public YADETargetProviderFile newYADETargetProviderFile(YADESourceProviderDelegator sourceDelegator, YADEProviderFile sourceFile, YADECopyOrMoveOperationTargetFilesConfig config) {
        if (config.cumulate()) {
            return null;// new YADETargetProviderFile(cumulativeFileFullPath, 0, 0, false);
        }

        String targetFullPath = null;
        // fullPath - relative from source dir to target dir ...

        // sourceDelegator.getDirectory();
        // targetDelegator.getDirectory();
        if (config.compress()) {

        }
        return new YADETargetProviderFile(targetFullPath, 0, 0, false);
    }


    @Override
    public YADETargetArguments getArgs() {
        return (YADETargetArguments) super.getArgs();
    }


    @Override
    public String getLogPrefix() {
        return LOG_PREFIX;
    }

}
