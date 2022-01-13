
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
    "envIndependent",
    "envRelated",
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
     * Filter for environment independent Objects
     * <p>
     * 
     * 
     */
    @JsonProperty("envIndependent")
    private EnvIndependentConfigurations envIndependent;
    /**
     * Filter for environment related Objects
     * <p>
     * 
     * 
     */
    @JsonProperty("envRelated")
    private EnvRelatedConfigurations envRelated;
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
     * Filter for environment independent Objects
     * <p>
     * 
     * 
     */
    @JsonProperty("envIndependent")
    public EnvIndependentConfigurations getEnvIndependent() {
        return envIndependent;
    }

    /**
     * Filter for environment independent Objects
     * <p>
     * 
     * 
     */
    @JsonProperty("envIndependent")
    public void setEnvIndependent(EnvIndependentConfigurations envIndependent) {
        this.envIndependent = envIndependent;
    }

    /**
     * Filter for environment related Objects
     * <p>
     * 
     * 
     */
    @JsonProperty("envRelated")
    public EnvRelatedConfigurations getEnvRelated() {
        return envRelated;
    }

    /**
     * Filter for environment related Objects
     * <p>
     * 
     * 
     */
    @JsonProperty("envRelated")
    public void setEnvRelated(EnvRelatedConfigurations envRelated) {
        this.envRelated = envRelated;
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
        return new ToStringBuilder(this).append("controllerId", controllerId).append("envIndependent", envIndependent).append("envRelated", envRelated).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(envIndependent).append(envRelated).append(controllerId).append(auditLog).toHashCode();
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
        return new EqualsBuilder().append(envIndependent, rhs.envIndependent).append(envRelated, rhs.envRelated).append(controllerId, rhs.controllerId).append(auditLog, rhs.auditLog).isEquals();
    }

}
