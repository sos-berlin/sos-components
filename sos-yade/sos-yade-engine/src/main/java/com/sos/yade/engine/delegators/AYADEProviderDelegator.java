package com.sos.yade.engine.delegators;

import com.sos.commons.util.SOSPathUtil;
import com.sos.commons.vfs.common.IProvider;
import com.sos.commons.vfs.common.file.ProviderDirectoryPath;
import com.sos.yade.engine.arguments.YADESourceTargetArguments;

public abstract class AYADEProviderDelegator implements IYADEProviderDelegator {

    private final IProvider provider;
    private final YADESourceTargetArguments args;
    private final ProviderDirectoryPath directory;

    private final String pathSeparator;

    public AYADEProviderDelegator(IProvider provider, YADESourceTargetArguments args) {
        this.provider = provider;
        this.args = args;
        this.directory = provider.getDirectoryPath(args.getDirectory().getValue());
        this.pathSeparator = getProviderPathSeparator(this.directory);
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
    public ProviderDirectoryPath getDirectory() {
        return directory;
    }

    public String getPathSeparator() {
        return pathSeparator;
    }

    public String normalizePath(String path) {
        return pathSeparator.equals("/") ? SOSPathUtil.toUnixPath(path) : SOSPathUtil.toWindowsPath(path);
    }

    private String getProviderPathSeparator(ProviderDirectoryPath directory) {
        if (directory == null) {
            return provider.getDirectoryPath("/tmp/tmp").getPathSeparator();
        }
        return directory.getPathSeparator();
    }

}
