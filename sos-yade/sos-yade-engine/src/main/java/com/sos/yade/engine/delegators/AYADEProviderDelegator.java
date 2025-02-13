package com.sos.yade.engine.delegators;

import com.sos.commons.util.SOSPathUtil;
import com.sos.commons.vfs.common.IProvider;
import com.sos.commons.vfs.common.file.ProviderDirectoryPath;
import com.sos.yade.engine.arguments.YADESourceTargetArguments;

public abstract class AYADEProviderDelegator implements IYADEProviderDelegator {

    private final IProvider provider;
    private final YADESourceTargetArguments args;
    private final ProviderDirectoryPath directory;

    private final char pathSeparator;

    public AYADEProviderDelegator(IProvider provider, YADESourceTargetArguments args) {
        this.provider = provider;
        this.args = args;
        this.directory = getDirectory(args.getDirectory().getValue());
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

    public char getPathSeparator() {
        return pathSeparator;
    }

    public String normalizePath(String path) {
        return SOSPathUtil.isUnixStylePathSeparator(pathSeparator) ? SOSPathUtil.toUnixPath(path) : SOSPathUtil.toWindowsPath(path);
    }

    private ProviderDirectoryPath getDirectory(String path) {
        if (path == null) {
            return null;
        }
        return provider.getDirectoryPath(path);
    }

    private char getProviderPathSeparator(ProviderDirectoryPath directory) {
        if (directory == null) {
            return provider.getDirectoryPath("/tmp/tmp").getPathSeparator();
        }
        return directory.getPathSeparator();
    }

}
