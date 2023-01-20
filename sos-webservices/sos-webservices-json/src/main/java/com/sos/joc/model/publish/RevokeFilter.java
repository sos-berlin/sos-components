
package com.sos.joc.model.publish;

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
 * revoke
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerIds",
    "deployConfigurations",
    "cancelOrdersDateFrom",
    "auditLog"
})
public class RevokeFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerIds")
    private List<String> controllerIds = new ArrayList<String>();
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("deployConfigurations")
    private List<Config> deployConfigurations = new ArrayList<Config>();
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("cancelOrdersDateFrom")
    private String cancelOrdersDateFrom;
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
    @JsonProperty("controllerIds")
    public List<String> getControllerIds() {
        return controllerIds;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerIds")
    public void setControllerIds(List<String> controllerIds) {
        this.controllerIds = controllerIds;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("deployConfigurations")
    public List<Config> getDeployConfigurations() {
        return deployConfigurations;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("deployConfigurations")
    public void setDeployConfigurations(List<Config> deployConfigurations) {
        this.deployConfigurations = deployConfigurations;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("cancelOrdersDateFrom")
    public String getCancelOrdersDateFrom() {
        return cancelOrdersDateFrom;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("cancelOrdersDateFrom")
    public void setCancelOrdersDateFrom(String cancelOrdersDateFrom) {
        this.cancelOrdersDateFrom = cancelOrdersDateFrom;
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
        return new ToStringBuilder(this).append("controllerIds", controllerIds).append("deployConfigurations", deployConfigurations).append("cancelOrdersDateFrom", cancelOrdersDateFrom).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(cancelOrdersDateFrom).append(auditLog).append(controllerIds).append(deployConfigurations).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RevokeFilter) == false) {
            return false;
        }
        RevokeFilter rhs = ((RevokeFilter) other);
        return new EqualsBuilder().append(cancelOrdersDateFrom, rhs.cancelOrdersDateFrom).append(auditLog, rhs.auditLog).append(controllerIds, rhs.controllerIds).append(deployConfigurations, rhs.deployConfigurations).isEquals();
    }

}
