package com.sos.commons.vfs.common.file;

public class ProviderDirectoryPath {

    private final String path;
    private final String pathWithTrailingSeparator;

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
