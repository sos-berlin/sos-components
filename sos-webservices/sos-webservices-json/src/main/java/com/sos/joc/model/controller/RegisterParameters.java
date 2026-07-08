
package com.sos.joc.model.controller;

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
 * register params
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "controllers",
    "clusterWatcher",
    "auditLog",
    "forceFailoverConfirmation"
})
public class RegisterParameters {

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    @JsonProperty("forceFailoverConfirmation")
    private Boolean forceFailoverConfirmation = false;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllers")
    private List<RegisterParameter> controllers = new ArrayList<RegisterParameter>();
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
    
    @JsonProperty("forceFailoverConfirmation")
    public Boolean getForceFailoverConfirmation() {
        return forceFailoverConfirmation;
    }

    @JsonProperty("forceFailoverConfirmation")
    public void setForceFailoverConfirmation(Boolean forceFailoverConfirmation) {
        this.forceFailoverConfirmation = forceFailoverConfirmation;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllers")
    public List<RegisterParameter> getControllers() {
        return controllers;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllers")
    public void setControllers(List<RegisterParameter> controllers) {
        this.controllers = controllers;
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
        return new ToStringBuilder(this).append("controllerId", controllerId).append("controllers", controllers).append("auditLog", auditLog).append("forceFailoverConfirmation", forceFailoverConfirmation).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllers).append(controllerId).append(auditLog).append(forceFailoverConfirmation).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RegisterParameters) == false) {
            return false;
        }
        RegisterParameters rhs = ((RegisterParameters) other);
        return new EqualsBuilder().append(controllers, rhs.controllers).append(controllerId, rhs.controllerId).append(auditLog, rhs.auditLog).append(forceFailoverConfirmation, rhs.forceFailoverConfirmation).isEquals();
    }

}
