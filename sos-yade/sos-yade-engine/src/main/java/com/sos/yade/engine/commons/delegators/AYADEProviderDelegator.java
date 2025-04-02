package com.sos.yade.engine.commons.delegators;

import com.sos.commons.util.SOSPathUtils;
import com.sos.commons.util.SOSString;
import com.sos.commons.vfs.commons.IProvider;
import com.sos.yade.engine.commons.arguments.YADESourceTargetArguments;

public abstract class AYADEProviderDelegator implements IYADEProviderDelegator {

    private final IProvider provider;
    private final YADESourceTargetArguments args;

    private final String label;
    private final String logPrefix;

    private final String directory;
    private final String directoryWithTrailingPathSeparator;

    private final boolean isHTTP;

    public AYADEProviderDelegator(IProvider provider, YADESourceTargetArguments args, String label) {
        this.provider = provider;
        this.args = args;
        this.label = label;
        this.logPrefix = "[" + label + "]";
        this.isHTTP = isHTTPProvider();
        this.directory = getDirectoryPath(args.getDirectory().getValue());
        this.directoryWithTrailingPathSeparator = getDirectoryPathWithTrailingPathSeparator(directory);
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

    /** Overrides {@link IYADEProviderDelegator#getLabel()} */
    @Override
    public String getLabel() {
        return label;
    }

    /** Overrides {@link IYADEProviderDelegator#getLogPrefix()} */
    @Override
    public String getLogPrefix() {
        return logPrefix;
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
        return SOSPathUtils.appendPath(parent, file, provider.getPathSeparator());
    }

    public String getParentPath(String path) {
        return SOSPathUtils.getParentPath(path, provider.getPathSeparator());
    }

    public boolean containsParentPath(String path) {
        return path.contains(provider.getPathSeparator());
    }

    public boolean isHTTP() {
        return isHTTP;
    }

    private boolean isHTTPProvider() {
        switch (getArgs().getProvider().getProtocol().getValue()) {
        case HTTP:
        case HTTPS:
        case WEBDAV:
        case WEBDAVS:
            return true;
        default:
            return false;
        }
    }

    private String getDirectoryPath(String path) {
        if (SOSString.isEmpty(path)) {
            return null;
        }
        String dir = path;
        // TODO always normalize?
        // if (isHTTP) {
        /** resolved and normalized based on baseURL */
        dir = provider.normalizePath(path);
        // }
        return SOSPathUtils.isUnixStylePathSeparator(getProvider().getPathSeparator()) ? SOSPathUtils.getUnixStyleDirectoryWithoutTrailingSeparator(
                dir) : SOSPathUtils.getWindowsStyleDirectoryWithoutTrailingSeparator(dir);
    }

    private String getDirectoryPathWithTrailingPathSeparator(String path) {
        if (SOSString.isEmpty(path)) {
            return null;
        }
        return SOSPathUtils.isUnixStylePathSeparator(getProvider().getPathSeparator()) ? SOSPathUtils.getUnixStyleDirectoryWithTrailingSeparator(path)
                : SOSPathUtils.getWindowsStyleDirectoryWithTrailingSeparator(path);
    }

}
