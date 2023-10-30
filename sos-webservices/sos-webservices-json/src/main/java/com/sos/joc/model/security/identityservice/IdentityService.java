
package com.sos.joc.model.security.identityservice;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * IdentityService
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "identityServiceType",
    "identityServiceName",
    "serviceAuthenticationScheme",
    "ordering",
    "disabled",
    "secondFactor",
    "secondFactorIdentityServiceName",
    "required",
    "auditLog"
})
public class IdentityService {

    /**
     * Identity Service Types
     * <p>
     * 
     * 
     */
    @JsonProperty("identityServiceType")
    private IdentityServiceTypes identityServiceType;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("identityServiceName")
    private String identityServiceName;
    /**
     * Identity Service Authentication Scheme
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("serviceAuthenticationScheme")
    private IdentityServiceAuthenticationScheme serviceAuthenticationScheme;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("ordering")
    private Integer ordering;
    /**
     * disabled parameter
     * <p>
     * controls if the object is disabled
     * 
     */
    @JsonProperty("disabled")
    @JsonPropertyDescription("controls if the object is disabled")
    private Boolean disabled = false;
    /**
     * Second Factor
     * <p>
     * Identity Service is used as a second factor
     * 
     */
    @JsonProperty("secondFactor")
    @JsonPropertyDescription("Identity Service is used as a second factor")
    private Boolean secondFactor = false;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("secondFactorIdentityServiceName")
    private String secondFactorIdentityServiceName;
    /**
     * required parameter
     * <p>
     * controls if the identity service is required
     * 
     */
    @JsonProperty("required")
    @JsonPropertyDescription("controls if the identity service is required")
    private Boolean required = false;
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
    public IdentityService() {
    }

    /**
     * 
     * @param identityServiceType
     * @param identityServiceName
     * @param secondFactorIdentityServiceName
     * @param auditLog
     * @param serviceAuthenticationScheme
     * @param ordering
     * @param secondFactor
     * @param disabled
     * @param required
     */
    public IdentityService(IdentityServiceTypes identityServiceType, String identityServiceName, IdentityServiceAuthenticationScheme serviceAuthenticationScheme, Integer ordering, Boolean disabled, Boolean secondFactor, String secondFactorIdentityServiceName, Boolean required, AuditParams auditLog) {
        super();
        this.identityServiceType = identityServiceType;
        this.identityServiceName = identityServiceName;
        this.serviceAuthenticationScheme = serviceAuthenticationScheme;
        this.ordering = ordering;
        this.disabled = disabled;
        this.secondFactor = secondFactor;
        this.secondFactorIdentityServiceName = secondFactorIdentityServiceName;
        this.required = required;
        this.auditLog = auditLog;
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
     * string without < and >
     * <p>
     * 
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("identityServiceName")
    public void setIdentityServiceName(String identityServiceName) {
        this.identityServiceName = identityServiceName;
    }

    /**
     * Identity Service Authentication Scheme
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("serviceAuthenticationScheme")
    public IdentityServiceAuthenticationScheme getServiceAuthenticationScheme() {
        return serviceAuthenticationScheme;
    }

    /**
     * Identity Service Authentication Scheme
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("serviceAuthenticationScheme")
    public void setServiceAuthenticationScheme(IdentityServiceAuthenticationScheme serviceAuthenticationScheme) {
        this.serviceAuthenticationScheme = serviceAuthenticationScheme;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("ordering")
    public Integer getOrdering() {
        return ordering;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("ordering")
    public void setOrdering(Integer ordering) {
        this.ordering = ordering;
    }

    /**
     * disabled parameter
     * <p>
     * controls if the object is disabled
     * 
     */
    @JsonProperty("disabled")
    public Boolean getDisabled() {
        return disabled;
    }

    /**
     * disabled parameter
     * <p>
     * controls if the object is disabled
     * 
     */
    @JsonProperty("disabled")
    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    /**
     * Second Factor
     * <p>
     * Identity Service is used as a second factor
     * 
     */
    @JsonProperty("secondFactor")
    public Boolean getSecondFactor() {
        return secondFactor;
    }

    /**
     * Second Factor
     * <p>
     * Identity Service is used as a second factor
     * 
     */
    @JsonProperty("secondFactor")
    public void setSecondFactor(Boolean secondFactor) {
        this.secondFactor = secondFactor;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("secondFactorIdentityServiceName")
    public String getSecondFactorIdentityServiceName() {
        return secondFactorIdentityServiceName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("secondFactorIdentityServiceName")
    public void setSecondFactorIdentityServiceName(String secondFactorIdentityServiceName) {
        this.secondFactorIdentityServiceName = secondFactorIdentityServiceName;
    }

    /**
     * required parameter
     * <p>
     * controls if the identity service is required
     * 
     */
    @JsonProperty("required")
    public Boolean getRequired() {
        return required;
    }

    /**
     * required parameter
     * <p>
     * controls if the identity service is required
     * 
     */
    @JsonProperty("required")
    public void setRequired(Boolean required) {
        this.required = required;
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
        return new ToStringBuilder(this).append("identityServiceType", identityServiceType).append("identityServiceName", identityServiceName).append("serviceAuthenticationScheme", serviceAuthenticationScheme).append("ordering", ordering).append("disabled", disabled).append("secondFactor", secondFactor).append("secondFactorIdentityServiceName", secondFactorIdentityServiceName).append("required", required).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(identityServiceType).append(identityServiceName).append(secondFactorIdentityServiceName).append(auditLog).append(serviceAuthenticationScheme).append(ordering).append(secondFactor).append(disabled).append(required).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof IdentityService) == false) {
            return false;
        }
        IdentityService rhs = ((IdentityService) other);
        return new EqualsBuilder().append(identityServiceType, rhs.identityServiceType).append(identityServiceName, rhs.identityServiceName).append(secondFactorIdentityServiceName, rhs.secondFactorIdentityServiceName).append(auditLog, rhs.auditLog).append(serviceAuthenticationScheme, rhs.serviceAuthenticationScheme).append(ordering, rhs.ordering).append(secondFactor, rhs.secondFactor).append(disabled, rhs.disabled).append(required, rhs.required).isEquals();
    }

}
