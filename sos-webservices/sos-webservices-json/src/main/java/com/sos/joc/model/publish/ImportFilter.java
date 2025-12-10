
package com.sos.joc.model.publish;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Import Filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "overwrite",
    "targetFolder",
    "format",
    "filename",
    "suffix",
    "prefix",
    "overwriteTags",
    "auditLog"
})
public class ImportFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("overwrite")
    private Boolean overwrite;
    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("targetFolder")
    @JsonPropertyDescription("absolute path of an object.")
    private String targetFolder;
    /**
     * Archive Format of the archive file
     * <p>
     * 
     * 
     */
    @JsonProperty("format")
    private ArchiveFormat format = ArchiveFormat.fromValue("ZIP");
    @JsonProperty("filename")
    private String filename;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("suffix")
    private String suffix;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("prefix")
    private String prefix;
    @JsonProperty("overwriteTags")
    private Boolean overwriteTags = false;
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("overwrite")
    public Boolean getOverwrite() {
        return overwrite;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("overwrite")
    public void setOverwrite(Boolean overwrite) {
        this.overwrite = overwrite;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("targetFolder")
    public String getTargetFolder() {
        return targetFolder;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("targetFolder")
    public void setTargetFolder(String targetFolder) {
        this.targetFolder = targetFolder;
    }

    /**
     * Archive Format of the archive file
     * <p>
     * 
     * 
     */
    @JsonProperty("format")
    public ArchiveFormat getFormat() {
        return format;
    }

    /**
     * Archive Format of the archive file
     * <p>
     * 
     * 
     */
    @JsonProperty("format")
    public void setFormat(ArchiveFormat format) {
        this.format = format;
    }

    @JsonProperty("filename")
    public String getFilename() {
        return filename;
    }

    @JsonProperty("filename")
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("suffix")
    public String getSuffix() {
        return suffix;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("suffix")
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("prefix")
    public String getPrefix() {
        return prefix;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("prefix")
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @JsonProperty("overwriteTags")
    public Boolean getOverwriteTags() {
        return overwriteTags;
    }

    @JsonProperty("overwriteTags")
    public void setOverwriteTags(Boolean overwriteTags) {
        this.overwriteTags = overwriteTags;
    }

    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    public AuditParams getAuditLog() {
        return auditLog;
    }

    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    public void setAuditLog(AuditParams auditLog) {
        this.auditLog = auditLog;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("overwrite", overwrite).append("targetFolder", targetFolder).append("format", format).append("filename", filename).append("suffix", suffix).append("prefix", prefix).append("overwriteTags", overwriteTags).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(targetFolder).append(filename).append(auditLog).append(prefix).append(format).append(overwriteTags).append(suffix).append(overwrite).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ImportFilter) == false) {
            return false;
        }
        ImportFilter rhs = ((ImportFilter) other);
        return new EqualsBuilder().append(targetFolder, rhs.targetFolder).append(filename, rhs.filename).append(auditLog, rhs.auditLog).append(prefix, rhs.prefix).append(format, rhs.format).append(overwriteTags, rhs.overwriteTags).append(suffix, rhs.suffix).append(overwrite, rhs.overwrite).isEquals();
    }

}
