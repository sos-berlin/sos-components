
package com.sos.joc.model.jobtemplate.propagate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * JobTemplates propagate filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "overwriteNotification",
    "overwriteAdmissionTime",
    "auditLog"
})
public class JobTemplatesPropagateBaseFilter {

    @JsonProperty("overwriteNotification")
    private Boolean overwriteNotification = false;
    @JsonProperty("overwriteAdmissionTime")
    private Boolean overwriteAdmissionTime = false;
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    @JsonProperty("overwriteNotification")
    public Boolean getOverwriteNotification() {
        return overwriteNotification;
    }

    @JsonProperty("overwriteNotification")
    public void setOverwriteNotification(Boolean overwriteNotification) {
        this.overwriteNotification = overwriteNotification;
    }

    @JsonProperty("overwriteAdmissionTime")
    public Boolean getOverwriteAdmissionTime() {
        return overwriteAdmissionTime;
    }

    @JsonProperty("overwriteAdmissionTime")
    public void setOverwriteAdmissionTime(Boolean overwriteAdmissionTime) {
        this.overwriteAdmissionTime = overwriteAdmissionTime;
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
        return new ToStringBuilder(this).append("overwriteNotification", overwriteNotification).append("overwriteAdmissionTime", overwriteAdmissionTime).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(overwriteNotification).append(overwriteAdmissionTime).append(auditLog).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobTemplatesPropagateBaseFilter) == false) {
            return false;
        }
        JobTemplatesPropagateBaseFilter rhs = ((JobTemplatesPropagateBaseFilter) other);
        return new EqualsBuilder().append(overwriteNotification, rhs.overwriteNotification).append(overwriteAdmissionTime, rhs.overwriteAdmissionTime).append(auditLog, rhs.auditLog).isEquals();
    }

}
