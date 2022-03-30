
package com.sos.joc.model.inventory.convert;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    "agentName",
    "subagentClusterId",
    "systemCrontab",
    "auditLog"
})
public class ConvertCronFilter {

    @JsonProperty("folder")
    private String folder = "/";
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("calendarName")
    private String calendarName;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentName")
    private String agentName;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("subagentClusterId")
    private String subagentClusterId;
    @JsonProperty("systemCrontab")
    private Boolean systemCrontab = false;
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    @JsonProperty("folder")
    public String getFolder() {
        return folder;
    }

    @JsonProperty("folder")
    public void setFolder(String folder) {
        this.folder = folder;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
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
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("agentName")
    public String getAgentName() {
        return agentName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentName")
    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("subagentClusterId")
    public String getSubagentClusterId() {
        return subagentClusterId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("subagentClusterId")
    public void setSubagentClusterId(String subagentClusterId) {
        this.subagentClusterId = subagentClusterId;
    }

    @JsonProperty("systemCrontab")
    public Boolean getSystemCrontab() {
        return systemCrontab;
    }

    @JsonProperty("systemCrontab")
    public void setSystemCrontab(Boolean systemCrontab) {
        this.systemCrontab = systemCrontab;
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
        return new ToStringBuilder(this).append("folder", folder).append("calendarName", calendarName).append("agentName", agentName).append("subagentClusterId", subagentClusterId).append("systemCrontab", systemCrontab).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(calendarName).append(folder).append(auditLog).append(systemCrontab).append(agentName).append(subagentClusterId).toHashCode();
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
        return new EqualsBuilder().append(calendarName, rhs.calendarName).append(folder, rhs.folder).append(auditLog, rhs.auditLog).append(systemCrontab, rhs.systemCrontab).append(agentName, rhs.agentName).append(subagentClusterId, rhs.subagentClusterId).isEquals();
    }

}
