
package com.sos.joc.model.processClass;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * ProcessClassDocuFilter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "processClass",
    "documentation",
    "auditLog"
})
public class ProcessClassDocuFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("processClass")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String processClass;
    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("documentation")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String documentation;
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
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("processClass")
    public String getProcessClass() {
        return processClass;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("processClass")
    public void setProcessClass(String processClass) {
        this.processClass = processClass;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("documentation")
    public String getDocumentation() {
        return documentation;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("documentation")
    public void setDocumentation(String documentation) {
        this.documentation = documentation;
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
        return new ToStringBuilder(this).append("controllerId", controllerId).append("processClass", processClass).append("documentation", documentation).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(processClass).append(controllerId).append(auditLog).append(documentation).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ProcessClassDocuFilter) == false) {
            return false;
        }
        ProcessClassDocuFilter rhs = ((ProcessClassDocuFilter) other);
        return new EqualsBuilder().append(processClass, rhs.processClass).append(controllerId, rhs.controllerId).append(auditLog, rhs.auditLog).append(documentation, rhs.documentation).isEquals();
    }

}
