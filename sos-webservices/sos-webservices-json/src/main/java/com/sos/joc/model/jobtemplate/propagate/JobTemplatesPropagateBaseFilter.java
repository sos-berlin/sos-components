
package com.sos.joc.model.jobtemplate.propagate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


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
    "overwriteValues",
    "propagateOptionalArguments",
    "deleteUnknownNodeProperties",
    "auditLog"
})
public class JobTemplatesPropagateBaseFilter {

    @JsonProperty("overwriteNotification")
    private Boolean overwriteNotification = false;
    @JsonProperty("overwriteAdmissionTime")
    private Boolean overwriteAdmissionTime = false;
    @JsonProperty("overwriteValues")
    private Boolean overwriteValues = false;
    @JsonProperty("propagateOptionalArguments")
    private Boolean propagateOptionalArguments = false;
    @JsonProperty("deleteUnknownNodeProperties")
    private Boolean deleteUnknownNodeProperties = false;
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

    @JsonProperty("overwriteValues")
    public Boolean getOverwriteValues() {
        return overwriteValues;
    }

    @JsonProperty("overwriteValues")
    public void setOverwriteValues(Boolean overwriteValues) {
        this.overwriteValues = overwriteValues;
    }

    @JsonProperty("propagateOptionalArguments")
    public Boolean getPropagateOptionalArguments() {
        return propagateOptionalArguments;
    }

    @JsonProperty("propagateOptionalArguments")
    public void setPropagateOptionalArguments(Boolean propagateOptionalArguments) {
        this.propagateOptionalArguments = propagateOptionalArguments;
    }

    @JsonProperty("deleteUnknownNodeProperties")
    public Boolean getDeleteUnknownNodeProperties() {
        return deleteUnknownNodeProperties;
    }

    @JsonProperty("deleteUnknownNodeProperties")
    public void setDeleteUnknownNodeProperties(Boolean deleteUnknownNodeProperties) {
        this.deleteUnknownNodeProperties = deleteUnknownNodeProperties;
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
        return new ToStringBuilder(this).append("overwriteNotification", overwriteNotification).append("overwriteAdmissionTime", overwriteAdmissionTime).append("overwriteValues", overwriteValues).append("propagateOptionalArguments", propagateOptionalArguments).append("deleteUnknownNodeProperties", deleteUnknownNodeProperties).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(propagateOptionalArguments).append(auditLog).append(overwriteValues).append(overwriteNotification).append(overwriteAdmissionTime).append(deleteUnknownNodeProperties).toHashCode();
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
        return new EqualsBuilder().append(propagateOptionalArguments, rhs.propagateOptionalArguments).append(auditLog, rhs.auditLog).append(overwriteValues, rhs.overwriteValues).append(overwriteNotification, rhs.overwriteNotification).append(overwriteAdmissionTime, rhs.overwriteAdmissionTime).append(deleteUnknownNodeProperties, rhs.deleteUnknownNodeProperties).isEquals();
    }

}
