package com.sos.yade.engine.commons.delegators;

import com.sos.commons.util.SOSPathUtil;
import com.sos.commons.util.SOSString;
import com.sos.commons.vfs.commons.IProvider;
import com.sos.yade.engine.commons.arguments.YADESourceTargetArguments;

public abstract class AYADEProviderDelegator implements IYADEProviderDelegator {

    private final IProvider provider;
    private final YADESourceTargetArguments args;

    private final String directory;
    private final String directoryWithTrailingPathSeparator;

    public AYADEProviderDelegator(IProvider provider, YADESourceTargetArguments args) {
        this.provider = provider;
        this.args = args;
        this.directory = getDirectoryPath(args.getDirectory().getValue());
        this.directoryWithTrailingPathSeparator = getDirectoryPathWithTrailingPathSeparator(args.getDirectory().getValue());
    }

    /** Overrides {@link IYADEProviderDelegator#getProvider()} */
    @Override
    public IProvider getProvider() {
        return provider;
    }

    /** Overrides {@link IYADEProviderDelegator#getArgs()} */
    @Override
    public YADESourceTargetArguments getArgs() {
        return args;
    }

    /** Overrides {@link IYADEProviderDelegator#getDirectory()} */
    @Override
    public String getDirectory() {
        return directory;
    }

    /** Overrides {@link IYADEProviderDelegator#getDirectoryWithTrailingPathSeparator()} */
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

    private String getDirectoryPath(String path) {
        if (SOSString.isEmpty(path)) {
            return null;
        }
        return SOSPathUtil.isUnixStylePathSeparator(getProvider().getPathSeparator()) ? SOSPathUtil.getUnixStyleDirectoryWithoutTrailingSeparator(
                path) : SOSPathUtil.getWindowsStyleDirectoryWithoutTrailingSeparator(path);
    }

    private String getDirectoryPathWithTrailingPathSeparator(String path) {
        if (SOSString.isEmpty(path)) {
            return null;
        }
        return SOSPathUtil.isUnixStylePathSeparator(getProvider().getPathSeparator()) ? SOSPathUtil.getUnixStyleDirectoryWithTrailingSeparator(path)
                : SOSPathUtil.getWindowsStyleDirectoryWithTrailingSeparator(path);
    }

}
