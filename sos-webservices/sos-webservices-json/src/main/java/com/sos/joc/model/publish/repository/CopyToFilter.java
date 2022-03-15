
package com.sos.joc.model.publish.repository;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Filter To Copy To Repository
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "rollout",
    "local",
    "auditLog"
})
public class CopyToFilter {

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * Filter for configurations
     * <p>
     * 
     * 
     */
    @JsonProperty("rollout")
    private Configurations rollout;
    /**
     * Filter for configurations
     * <p>
     * 
     * 
     */
    @JsonProperty("local")
    private Configurations local;
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

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
     * Filter for configurations
     * <p>
     * 
     * 
     */
    @JsonProperty("rollout")
    public Configurations getRollout() {
        return rollout;
    }

    /**
     * Filter for configurations
     * <p>
     * 
     * 
     */
    @JsonProperty("rollout")
    public void setRollout(Configurations rollout) {
        this.rollout = rollout;
    }

    /**
     * Filter for configurations
     * <p>
     * 
     * 
     */
    @JsonProperty("local")
    public Configurations getLocal() {
        return local;
    }

    /**
     * Filter for configurations
     * <p>
     * 
     * 
     */
    @JsonProperty("local")
    public void setLocal(Configurations local) {
        this.local = local;
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
        return new ToStringBuilder(this).append("controllerId", controllerId).append("rollout", rollout).append("local", local).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllerId).append(rollout).append(auditLog).append(local).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CopyToFilter) == false) {
            return false;
        }
        CopyToFilter rhs = ((CopyToFilter) other);
        return new EqualsBuilder().append(controllerId, rhs.controllerId).append(rollout, rhs.rollout).append(auditLog, rhs.auditLog).append(local, rhs.local).isEquals();
    }

}
