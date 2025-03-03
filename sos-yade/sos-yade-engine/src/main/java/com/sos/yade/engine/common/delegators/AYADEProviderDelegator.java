package com.sos.yade.engine.common.delegators;

import com.sos.commons.util.SOSPathUtil;
import com.sos.commons.vfs.common.IProvider;
import com.sos.yade.engine.common.arguments.YADESourceTargetArguments;

public abstract class AYADEProviderDelegator implements IYADEProviderDelegator {

    private final IProvider provider;
    private final YADESourceTargetArguments args;

    private final String directory;
    private final String directoryWithTrailingPathSeparator;

    public AYADEProviderDelegator(IProvider provider, YADESourceTargetArguments args) {
        this.provider = provider;
        this.args = args;
        this.directory = provider.getDirectoryPath(args.getDirectory().getValue());
        this.directoryWithTrailingPathSeparator = provider.getDirectoryPathWithTrailingPathSeparator(args.getDirectory().getValue());
    }

    @Override
    public IProvider getProvider() {
        return provider;
    }

    @Override
    public YADESourceTargetArguments getArgs() {
        return args;
    }

    @Override
    public String getDirectory() {
        return directory;
    }

    @Override
    public String getDirectoryWithTrailingPathSeparator() {
        return directoryWithTrailingPathSeparator;
    }

    public String appendPath(String parent, String file) {
        return SOSPathUtil.appendPath(parent, file, provider.getPathSeparator());
    }

    public String getParentPath(String path) {
        return SOSPathUtil.getParentPath(path, provider.getPathSeparator());
    }

    public boolean containsParentPath(String path) {
        return path.contains(provider.getPathSeparator());
    }

}
