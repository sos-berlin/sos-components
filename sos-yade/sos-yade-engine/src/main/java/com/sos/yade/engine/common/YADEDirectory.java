package com.sos.yade.engine.common;

import com.sos.commons.vfs.common.IProvider;

public class YADEDirectory {

    private final String path;
    private final String pathWithTrailingSeparator;

    // TODO check if starts like WindowsOpenSSH /C:/....
    public YADEDirectory(IProvider provider, String path) {
        this.path = provider.getDirectoryPathWithoutTrailingSeparator(path);
        this.pathWithTrailingSeparator = provider.getDirectoryPathWithTrailingSeparator(path);
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
    public boolean equalsIgnoreCase(YADEDirectory other) {
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
        YADEDirectory other = (YADEDirectory) obj;
        return this.path.equals(other.path);
    }

    @Override
    public String toString() {
        return path;
    }

}
