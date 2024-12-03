
package com.sos.joc.model.agent;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * store subagent clusters
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "subagentClusters",
    "update",
    "auditLog"
})
public class StoreSubagentClusters {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("subagentClusters")
    private List<SubagentCluster> subagentClusters = new ArrayList<SubagentCluster>();
    @JsonProperty("update")
    private Boolean update = false;
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
    @JsonProperty("subagentClusters")
    public List<SubagentCluster> getSubagentClusters() {
        return subagentClusters;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("subagentClusters")
    public void setSubagentClusters(List<SubagentCluster> subagentClusters) {
        this.subagentClusters = subagentClusters;
    }

    @JsonProperty("update")
    public Boolean getUpdate() {
        return update;
    }

    @JsonProperty("update")
    public void setUpdate(Boolean update) {
        this.update = update;
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
        return new ToStringBuilder(this).append("subagentClusters", subagentClusters).append("update", update).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(update).append(auditLog).append(subagentClusters).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof StoreSubagentClusters) == false) {
            return false;
        }
        StoreSubagentClusters rhs = ((StoreSubagentClusters) other);
        return new EqualsBuilder().append(update, rhs.update).append(auditLog, rhs.auditLog).append(subagentClusters, rhs.subagentClusters).isEquals();
    }

}
