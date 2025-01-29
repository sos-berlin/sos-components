package com.sos.commons.vfs.common.file;

import java.nio.file.Path;

import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSPathUtil;

public class ProviderDirectoryPath {

    private final String path;
    private final String pathWithTrailingSeparator;

    public ProviderDirectoryPath(Path path) {
        String p = SOSPath.toAbsoluteNormalizedPath(path).toString();
        this.path = SOSPathUtil.getUnixStyleDirectoryWithoutTrailingSeparator(p);
        this.pathWithTrailingSeparator = SOSPathUtil.getUnixStyleDirectoryWithTrailingSeparator(p);
    }

    public ProviderDirectoryPath(String pathWithoutTrailingSeparator, String pathWithTrailingSeparator) {
        this.path = pathWithoutTrailingSeparator;
        this.pathWithTrailingSeparator = pathWithTrailingSeparator;
    }

    /** @return path without trailing separator or null */
    public String getPath() {
        return path;
    }

    /** @return path with trailing separator or null */
    public String getPathWithTrailingSeparator() {
        return pathWithTrailingSeparator;
    }

    /** Case-insensitive
     * 
     * @param other
     * @return */
    public boolean equalsIgnoreCase(ProviderDirectoryPath other) {
        if (other == null) {
            return false;
        }
        if (this.path == null && other.path == null) {
            return true;
        }
        if (this.path == null || other.path == null) {
            return false;
        }
        return this.path.equalsIgnoreCase(other.path);
    }

    /** Case-sensitive */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ProviderDirectoryPath other = (ProviderDirectoryPath) obj;
        return this.path.equals(other.path);
    }

    @Override
    public String toString() {
        return path;
    }
}
