package com.sos.commons.vfs.common.file.selection;

import java.util.regex.Pattern;

public class ProviderFileSelectionConfig {

    private final String directory;
    private final boolean recursive;

    private final Pattern fileNamePattern;
    private final Pattern excludedDirectoriesPattern;
    private final String excludedFileNameEnd;// TODO Pattern extensions

    private final int maxFiles;
    private final long maxFileSize;
    private final long minFileSize;

    private ProviderFileSelectionConfig(Builder builder) {
        this.directory = builder.directory;
        this.recursive = builder.recursive;
        this.fileNamePattern = builder.fileNamePattern;
        this.excludedDirectoriesPattern = builder.excludedDirectoriesPattern;
        this.excludedFileNameEnd = builder.excludedFileNameEnd;
        this.maxFiles = builder.maxFiles;
        this.maxFileSize = builder.maxFileSize;
        this.minFileSize = builder.minFileSize;
    }

    public static class Builder {

        private String directory;
        private boolean recursive;

        private Pattern fileNamePattern;
        private Pattern excludedDirectoriesPattern;
        private String excludedFileNameEnd;

        private int maxFiles = -1;
        private long maxFileSize = -1L;
        private long minFileSize = -1L;

        public Builder directory(String val) {
            this.directory = val;
            return this;
        }

        public Builder recursive(boolean val) {
            this.recursive = val;
            return this;
        }

        public Builder fileNamePattern(Pattern val) {
            this.fileNamePattern = val;
            return this;
        }

        public Builder excludedDirectoriesPattern(Pattern val) {
            this.excludedDirectoriesPattern = val;
            return this;
        }

        public Builder excludedFileNameEnd(String val) {
            this.excludedFileNameEnd = val;
            return this;
        }

        public Builder maxFiles(int val) {
            this.maxFiles = val;
            return this;
        }

        public Builder maxFileSize(long val) {
            this.maxFileSize = val;
            return this;
        }

        public Builder minFileSize(long val) {
            this.minFileSize = val;
            return this;
        }

        public ProviderFileSelectionConfig build() {
            return new ProviderFileSelectionConfig(this);
        }
    }

    public String getDirectory() {
        return directory;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public Pattern getFileNamePattern() {
        return fileNamePattern;
    }

    public Pattern getExcludedDirectoriesPattern() {
        return excludedDirectoriesPattern;
    }

    public String getExcludedFileNameEnd() {
        return excludedFileNameEnd;
    }

    public int getMaxFiles() {
        return maxFiles;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public long getMinFileSize() {
        return minFileSize;
    }
}
