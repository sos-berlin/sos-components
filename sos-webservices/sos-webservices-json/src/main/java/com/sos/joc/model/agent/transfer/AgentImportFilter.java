
package com.sos.joc.model.agent.transfer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.publish.ArchiveFormat;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Agent Import Filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "format",
    "overwrite",
    "controllerId",
    "auditLog"
})
public class AgentImportFilter {

    /**
     * Archive Format of the archive file
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("format")
    private ArchiveFormat format = ArchiveFormat.fromValue("ZIP");
    @JsonProperty("overwrite")
    private Boolean overwrite;
    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    /**
     * Archive Format of the archive file
     * <p>
     * 
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("format")
    public void setFormat(ArchiveFormat format) {
        this.format = format;
    }

    @JsonProperty("overwrite")
    public Boolean getOverwrite() {
        return overwrite;
    }

    @JsonProperty("overwrite")
    public void setOverwrite(Boolean overwrite) {
        this.overwrite = overwrite;
    }

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
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
        return new ToStringBuilder(this).append("format", format).append("overwrite", overwrite).append("controllerId", controllerId).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(format).append(controllerId).append(auditLog).append(overwrite).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AgentImportFilter) == false) {
            return false;
        }
        AgentImportFilter rhs = ((AgentImportFilter) other);
        return new EqualsBuilder().append(format, rhs.format).append(controllerId, rhs.controllerId).append(auditLog, rhs.auditLog).append(overwrite, rhs.overwrite).isEquals();
    }

}
