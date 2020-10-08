
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
 * Import Deploy Filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllers",
    "keyAlgorithm",
    "auditLog"
})
public class ImportDeployFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllers")
    private List<Controller> controllers = new ArrayList<Controller>();
    @JsonProperty("keyAlgorithm")
    private String keyAlgorithm;
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
    @JsonProperty("controllers")
    public List<Controller> getControllers() {
        return controllers;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllers")
    public void setControllers(List<Controller> controllers) {
        this.controllers = controllers;
    }

    @JsonProperty("keyAlgorithm")
    public String getKeyAlgorithm() {
        return keyAlgorithm;
    }

    @JsonProperty("keyAlgorithm")
    public void setKeyAlgorithm(String keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
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
        return new ToStringBuilder(this).append("controllers", controllers).append("keyAlgorithm", keyAlgorithm).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllers).append(keyAlgorithm).append(auditLog).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ImportDeployFilter) == false) {
            return false;
        }
        ImportDeployFilter rhs = ((ImportDeployFilter) other);
        return new EqualsBuilder().append(controllers, rhs.controllers).append(keyAlgorithm, rhs.keyAlgorithm).append(auditLog, rhs.auditLog).isEquals();
    }

}
