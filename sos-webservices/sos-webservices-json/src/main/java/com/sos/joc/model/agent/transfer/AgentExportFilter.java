
package com.sos.joc.model.agent.transfer;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.publish.ExportFile;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * AgentExportFilter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "exportFile",
    "agentIds",
    "auditLog"
})
public class AgentExportFilter {

    /**
     * ExportFile
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("exportFile")
    private ExportFile exportFile;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentIds")
    private List<String> agentIds = new ArrayList<String>();
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    /**
     * ExportFile
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("exportFile")
    public ExportFile getExportFile() {
        return exportFile;
    }

    /**
     * ExportFile
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("exportFile")
    public void setExportFile(ExportFile exportFile) {
        this.exportFile = exportFile;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentIds")
    public List<String> getAgentIds() {
        return agentIds;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentIds")
    public void setAgentIds(List<String> agentIds) {
        this.agentIds = agentIds;
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
        return new ToStringBuilder(this).append("exportFile", exportFile).append("agentIds", agentIds).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(exportFile).append(agentIds).append(auditLog).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AgentExportFilter) == false) {
            return false;
        }
        AgentExportFilter rhs = ((AgentExportFilter) other);
        return new EqualsBuilder().append(exportFile, rhs.exportFile).append(agentIds, rhs.agentIds).append(auditLog, rhs.auditLog).isEquals();
    }

}
