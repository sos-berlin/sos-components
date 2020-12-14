
package com.sos.joc.model.job;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * JobDocuFilter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "job",
    "documentation",
    "auditLog"
})
public class JobDocuFilter {

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
     * absolute path of an object.
     * (Required)
     * 
     */
    @JsonProperty("job")
    @JsonPropertyDescription("absolute path of an object.")
    private String job;
    /**
     * path
     * <p>
     * absolute path of an object.
     * (Required)
     * 
     */
    @JsonProperty("documentation")
    @JsonPropertyDescription("absolute path of an object.")
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
     * absolute path of an object.
     * (Required)
     * 
     */
    @JsonProperty("job")
    public String getJob() {
        return job;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * (Required)
     * 
     */
    @JsonProperty("job")
    public void setJob(String job) {
        this.job = job;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
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
     * absolute path of an object.
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
        return new ToStringBuilder(this).append("controllerId", controllerId).append("job", job).append("documentation", documentation).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllerId).append(job).append(auditLog).append(documentation).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobDocuFilter) == false) {
            return false;
        }
        JobDocuFilter rhs = ((JobDocuFilter) other);
        return new EqualsBuilder().append(controllerId, rhs.controllerId).append(job, rhs.job).append(auditLog, rhs.auditLog).append(documentation, rhs.documentation).isEquals();
    }

}
