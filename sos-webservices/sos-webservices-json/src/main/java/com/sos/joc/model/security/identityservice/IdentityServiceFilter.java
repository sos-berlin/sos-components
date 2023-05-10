
package com.sos.joc.model.security.identityservice;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * IdentityService Filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "identityServiceName",
    "identityServiceType",
    "disabled",
    "secondFactor",
    "auditLog"
})
public class IdentityServiceFilter {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("identityServiceName")
    private String identityServiceName;
    /**
     * Identity Service Types
     * <p>
     * 
     * 
     */
    @JsonProperty("identityServiceType")
    private IdentityServiceTypes identityServiceType;
    /**
     * disabled filter parameter
     * <p>
     * returns disabled objects
     * 
     */
    @JsonProperty("disabled")
    @JsonPropertyDescription("returns disabled objects")
    private Boolean disabled;
    /**
     * Second Factory
     * <p>
     * Identity Service is used as a second factor
     * 
     */
    @JsonProperty("secondFactor")
    @JsonPropertyDescription("Identity Service is used as a second factor")
    private Boolean secondFactor;
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    /**
     * No args constructor for use in serialization
     * 
     */
    public IdentityServiceFilter() {
    }

    /**
     * 
     * @param identityServiceName
     * @param identityServiceType
     * @param auditLog
     * @param secondFactor
     * @param disabled
     */
    public IdentityServiceFilter(String identityServiceName, IdentityServiceTypes identityServiceType, Boolean disabled, Boolean secondFactor, AuditParams auditLog) {
        super();
        this.identityServiceName = identityServiceName;
        this.identityServiceType = identityServiceType;
        this.disabled = disabled;
        this.secondFactor = secondFactor;
        this.auditLog = auditLog;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("identityServiceName")
    public String getIdentityServiceName() {
        return identityServiceName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("identityServiceName")
    public void setIdentityServiceName(String identityServiceName) {
        this.identityServiceName = identityServiceName;
    }

    /**
     * Identity Service Types
     * <p>
     * 
     * 
     */
    @JsonProperty("identityServiceType")
    public IdentityServiceTypes getIdentityServiceType() {
        return identityServiceType;
    }

    /**
     * Identity Service Types
     * <p>
     * 
     * 
     */
    @JsonProperty("identityServiceType")
    public void setIdentityServiceType(IdentityServiceTypes identityServiceType) {
        this.identityServiceType = identityServiceType;
    }

    /**
     * disabled filter parameter
     * <p>
     * returns disabled objects
     * 
     */
    @JsonProperty("disabled")
    public Boolean getDisabled() {
        return disabled;
    }

    /**
     * disabled filter parameter
     * <p>
     * returns disabled objects
     * 
     */
    @JsonProperty("disabled")
    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    /**
     * Second Factory
     * <p>
     * Identity Service is used as a second factor
     * 
     */
    @JsonProperty("secondFactor")
    public Boolean getSecondFactor() {
        return secondFactor;
    }

    /**
     * Second Factory
     * <p>
     * Identity Service is used as a second factor
     * 
     */
    @JsonProperty("secondFactor")
    public void setSecondFactor(Boolean secondFactor) {
        this.secondFactor = secondFactor;
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
        return new ToStringBuilder(this).append("identityServiceName", identityServiceName).append("identityServiceType", identityServiceType).append("disabled", disabled).append("secondFactor", secondFactor).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(identityServiceName).append(identityServiceType).append(disabled).append(auditLog).append(secondFactor).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof IdentityServiceFilter) == false) {
            return false;
        }
        IdentityServiceFilter rhs = ((IdentityServiceFilter) other);
        return new EqualsBuilder().append(identityServiceName, rhs.identityServiceName).append(identityServiceType, rhs.identityServiceType).append(disabled, rhs.disabled).append(auditLog, rhs.auditLog).append(secondFactor, rhs.secondFactor).isEquals();
    }

}
