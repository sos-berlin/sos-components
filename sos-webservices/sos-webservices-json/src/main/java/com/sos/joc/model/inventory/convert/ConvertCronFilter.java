
package com.sos.joc.model.inventory.convert;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Convert Cron Filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "folder",
    "calendarName",
    "suffix",
    "prefix",
    "auditLog"
})
public class ConvertCronFilter {

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("folder")
    @JsonPropertyDescription("absolute path of an object.")
    private String folder;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("calendarName")
    private String calendarName;
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
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("folder")
    public String getFolder() {
        return folder;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("folder")
    public void setFolder(String folder) {
        this.folder = folder;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("calendarName")
    public String getCalendarName() {
        return calendarName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("calendarName")
    public void setCalendarName(String calendarName) {
        this.calendarName = calendarName;
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
        return new ToStringBuilder(this).append("folder", folder).append("calendarName", calendarName).append("suffix", suffix).append("prefix", prefix).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(calendarName).append(folder).append(suffix).append(auditLog).append(prefix).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ConvertCronFilter) == false) {
            return false;
        }
        ConvertCronFilter rhs = ((ConvertCronFilter) other);
        return new EqualsBuilder().append(calendarName, rhs.calendarName).append(folder, rhs.folder).append(suffix, rhs.suffix).append(auditLog, rhs.auditLog).append(prefix, rhs.prefix).isEquals();
    }

}
