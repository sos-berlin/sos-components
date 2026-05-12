package com.sos.commons.vfs.commons.file.selection;

import java.time.Instant;
import java.util.TimeZone;
import java.util.regex.Pattern;

import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSShell;
import com.sos.commons.util.loggers.base.ISOSLogger;

public class ProviderFileSelectionConfig {

    private final ISOSLogger logger;

    private final String directory;
    private final boolean polling;
    private final boolean recursive;

    private final Pattern fileNamePattern;
    private final Pattern excludedDirectoriesPattern;
    private final String excludedFileExtension;// TODO Pattern extensions

    private final int maxFiles;

    private final Long minFileAge;
    private final Long maxFileAge;
    private final Long minFileSize;
    private final Long maxFileSize;

    private final String minFileSizeConfigured;
    private final String maxFileSizeConfigured;

    private final boolean filterByMaxFiles;

    private ProviderFileSelectionConfig(ISOSLogger logger, Builder builder) {
        this.logger = logger;

        this.directory = builder.directory;
        this.polling = builder.polling;
        this.recursive = builder.recursive;
        this.fileNamePattern = builder.fileNamePattern;
        this.excludedDirectoriesPattern = builder.excludedDirectoriesPattern;
        this.excludedFileExtension = builder.excludedFileExtension;
        this.maxFiles = builder.maxFiles;

        this.minFileAge = builder.minFileAge;
        this.maxFileAge = builder.maxFileAge;
        this.minFileSize = builder.minFileSize;
        this.maxFileSize = builder.maxFileSize;

        this.minFileSizeConfigured = builder.minFileSizeConfigured;
        this.maxFileSizeConfigured = builder.maxFileSizeConfigured;

        this.filterByMaxFiles = this.maxFiles > -1; // YADE 1: > 0
    }

    public static class Builder {

        private final Instant now;

        private boolean polling;
        private String directory;
        private boolean recursive;

        private Pattern fileNamePattern;
        private Pattern excludedDirectoriesPattern;
        private String excludedFileExtension;

        private int maxFiles = -1;

        private Long minFileAge;
        private Long maxFileAge;
        private Long minFileSize;
        private Long maxFileSize;

        private String minFileSizeConfigured;
        private String maxFileSizeConfigured;

        public Builder() {
            this.now = Instant.now();
        }

        public Builder polling(boolean val) {
            this.polling = val;
            return this;
        }

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

        public Builder excludedFileExtension(String val) {
            this.excludedFileExtension = val;
            return this;
        }

        public Builder maxFiles(Integer val) {
            if (val != null) {
                this.maxFiles = val.intValue();
            }
            return this;
        }

        public Builder minFileAge(String val) throws Exception {
            if (val != null) {
                this.minFileAge = toFileAge(this.now, val);
            }
            return this;
        }

        public Builder maxFileAge(String val) throws Exception {
            if (val != null) {
                this.maxFileAge = toFileAge(this.now, val);
            }
            return this;
        }

        public Builder minFileSize(String val) throws Exception {
            this.minFileSizeConfigured = val;
            if (this.minFileSizeConfigured != null) {
                this.minFileSize = toFileSize(val);
            }
            return this;
        }

        public Builder maxFileSize(String val) throws Exception {
            this.maxFileSizeConfigured = val;
            if (this.maxFileSizeConfigured != null) {
                this.maxFileSize = toFileSize(val);
            }
            return this;
        }

        public ProviderFileSelectionConfig build(ISOSLogger logger) {
            return new ProviderFileSelectionConfig(logger, this);
        }

    }

    public ISOSLogger getLogger() {
        return logger;
    }

    public String getDirectory() {
        return directory;
    }

    public boolean isPolling() {
        return polling;
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

    public String getExcludedFileExtension() {
        return excludedFileExtension;
    }

    public int getMaxFiles() {
        return maxFiles;
    }

    public boolean isFilterByMaxFilesEnabled() {
        return filterByMaxFiles;
    }

    public Long getMinFileAge() {
        return minFileAge;
    }

    public Long getMaxFileAge() {
        return maxFileAge;
    }

    public Long getMinFileSize() {
        return minFileSize;
    }

    public Long getMaxFileSize() {
        return maxFileSize;
    }

    public String getMinFileSizeConfigured() {
        return minFileSizeConfigured;
    }

    public String getMaxFileSizeConfigured() {
        return maxFileSizeConfigured;
    }

    public String getFileAgeAsUTCString(Long fileAge) {
        if (fileAge == null) {
            return "";
        }
        return SOSDate.tryGetDateTimeAsString(fileAge.longValue(), TimeZone.getTimeZone(SOSDate.TIMEZONE_UTC));
    }

    public static Long toFileSize(String val) throws Exception {
        return SOSShell.parseByteSize(val);
    }

    private static Long toFileAge(Instant now, String val) throws Exception {
        try {
            // e.g.: 2026-05-12T14:30:00Z
            // 2026-05-12T14:30:00+02:00
            return Instant.parse(val).toEpochMilli();
        } catch (Exception e) {
            return now.minusMillis(SOSDate.parseAge("s", val).longValue() * 1_000).toEpochMilli();
        }
    }

}
