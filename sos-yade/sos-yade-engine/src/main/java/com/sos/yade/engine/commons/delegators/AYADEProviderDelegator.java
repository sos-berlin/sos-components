package com.sos.yade.engine.commons.delegators;

import com.sos.commons.util.SOSPathUtils;
import com.sos.commons.util.SOSShell;
import com.sos.commons.util.SOSString;
import com.sos.commons.vfs.commons.AProvider;
import com.sos.commons.vfs.commons.AProviderArguments.Protocol;
import com.sos.yade.engine.commons.arguments.YADEJumpHostArguments;
import com.sos.yade.engine.commons.arguments.YADESourceTargetArguments;

public abstract class AYADEProviderDelegator implements IYADEProviderDelegator {

    private final AProvider<?> provider;
    private final YADESourceTargetArguments args;

    private final String label;

    private final String directory;
    private final String directoryWithTrailingPathSeparator;

    private final boolean isHTTP;
    private final boolean isAzure;
    private final boolean isWindows;

    public AYADEProviderDelegator(AProvider<?> provider, YADESourceTargetArguments args) {
        this.provider = provider;
        this.args = args;
        this.label = args.getLabel().getValue();
        this.isHTTP = isHTTPProvider();
        this.isAzure = isAzureProvider();
        this.isWindows = isWindowsProvider();
        this.directory = getDirectoryPath(args.getDirectory().getValue());
        this.directoryWithTrailingPathSeparator = getDirectoryPathWithTrailingPathSeparator(directory);
    }

    /** Overrides {@link IYADEProviderDelegator#getProvider()} */
    @Override
    public AProvider<?> getProvider() {
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

    public boolean isAzure() {
        return isAzure;
    }

    public boolean isWindows() {
        return isWindows;
    }

    public boolean isJumpHost() {
        return YADEJumpHostArguments.LABEL.equals(label);
    }

    private boolean isHTTPProvider() {
        switch (getArgs().getProvider().getProtocol().getValue()) {
        case AZURE_BLOB_STORAGE:
        case HTTP:
        case HTTPS:
        case WEBDAV:
        case WEBDAVS:
            return true;
        default:
            return false;
        }
    }

    private boolean isAzureProvider() {
        return Protocol.AZURE_BLOB_STORAGE.equals(getArgs().getProvider().getProtocol().getValue());
    }

    // TODO optimize for SFTP + isHTTPProvider/isWindowsProvider
    private boolean isWindowsProvider() {
        switch (getArgs().getProvider().getProtocol().getValue()) {
        case LOCAL:
            return SOSShell.IS_WINDOWS;
        default:
            return false;
        }
    }

    private String getDirectoryPath(String path) {
        if (SOSString.isEmpty(path)) {
            return null;
        }
        String dir = path;

        // JumpHost Note: the java nio methods such as 'normalize' or 'absolutePath' cannot be used,
        // because the paths are created based on the current system and not on the JumpHost system on which the JumpHost client is installed */
        // TODO - normalizePath were updated - re-check if if (!isJumpHost()) { is needed
        if (!isJumpHost() && !isHTTP) {
            // HTTP/WebDAV returns an absolutely encoded path with the base URI, e.g.: http://<server>:<port>/<dir>
            dir = provider.normalizePath(dir);
        }

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
