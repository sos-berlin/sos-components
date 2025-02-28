
package com.sos.joc.model.cluster;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.cluster.common.ClusterServices;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Immediately run JOC services such as dailyplan,cleanup
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "type",
    "auditLog"
})
public class ClusterServiceRun {

    /**
     * JOC cluster services
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    private ClusterServices type;
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    /**
     * JOC cluster services
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    public ClusterServices getType() {
        return type;
    }

    /**
     * JOC cluster services
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    public void setType(ClusterServices type) {
        this.type = type;
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
        return new ToStringBuilder(this).append("type", type).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(type).append(auditLog).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ClusterServiceRun) == false) {
            return false;
        }
        ClusterServiceRun rhs = ((ClusterServiceRun) other);
        return new EqualsBuilder().append(type, rhs.type).append(auditLog, rhs.auditLog).isEquals();
    }

}
