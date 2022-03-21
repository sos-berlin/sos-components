
package com.sos.joc.model.agent;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * store subagent clusters
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "subagentClusters",
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
        return new ToStringBuilder(this).append("subagentClusters", subagentClusters).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(auditLog).append(subagentClusters).toHashCode();
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
        return new EqualsBuilder().append(auditLog, rhs.auditLog).append(subagentClusters, rhs.subagentClusters).isEquals();
    }

}
