
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
    "singleFactorCert",
    "singleFactorPwd",
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
     * Single Factory Cert
     * <p>
     * Identity Service allows logon via certificate
     * 
     */
    @JsonProperty("singleFactorCert")
    @JsonPropertyDescription("Identity Service allows logon via certificate")
    private Boolean singleFactorCert = false;
    /**
     * Single Factory Pwd
     * <p>
     * Identity Service allows login via user account/password
     * 
     */
    @JsonProperty("singleFactorPwd")
    @JsonPropertyDescription("Identity Service allows login via user account/password")
    private Boolean singleFactorPwd = false;
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
     * @param auditLog
     * @param serviceAuthenticationScheme
     * @param ordering
     * @param disabled
     * @param singleFactorCert
     * @param singleFactorPwd
     * @param required
     */
    public IdentityService(IdentityServiceTypes identityServiceType, String identityServiceName, IdentityServiceAuthenticationScheme serviceAuthenticationScheme, Integer ordering, Boolean disabled, Boolean singleFactorCert, Boolean singleFactorPwd, Boolean required, AuditParams auditLog) {
        super();
        this.identityServiceType = identityServiceType;
        this.identityServiceName = identityServiceName;
        this.serviceAuthenticationScheme = serviceAuthenticationScheme;
        this.ordering = ordering;
        this.disabled = disabled;
        this.singleFactorCert = singleFactorCert;
        this.singleFactorPwd = singleFactorPwd;
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
     * Single Factory Cert
     * <p>
     * Identity Service allows logon via certificate
     * 
     */
    @JsonProperty("singleFactorCert")
    public Boolean getSingleFactorCert() {
        return singleFactorCert;
    }

    /**
     * Single Factory Cert
     * <p>
     * Identity Service allows logon via certificate
     * 
     */
    @JsonProperty("singleFactorCert")
    public void setSingleFactorCert(Boolean singleFactorCert) {
        this.singleFactorCert = singleFactorCert;
    }

    /**
     * Single Factory Pwd
     * <p>
     * Identity Service allows login via user account/password
     * 
     */
    @JsonProperty("singleFactorPwd")
    public Boolean getSingleFactorPwd() {
        return singleFactorPwd;
    }

    /**
     * Single Factory Pwd
     * <p>
     * Identity Service allows login via user account/password
     * 
     */
    @JsonProperty("singleFactorPwd")
    public void setSingleFactorPwd(Boolean singleFactorPwd) {
        this.singleFactorPwd = singleFactorPwd;
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
        return new ToStringBuilder(this).append("identityServiceType", identityServiceType).append("identityServiceName", identityServiceName).append("serviceAuthenticationScheme", serviceAuthenticationScheme).append("ordering", ordering).append("disabled", disabled).append("singleFactorCert", singleFactorCert).append("singleFactorPwd", singleFactorPwd).append("required", required).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(identityServiceType).append(identityServiceName).append(auditLog).append(serviceAuthenticationScheme).append(ordering).append(disabled).append(singleFactorCert).append(singleFactorPwd).append(required).toHashCode();
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
        return new EqualsBuilder().append(identityServiceType, rhs.identityServiceType).append(identityServiceName, rhs.identityServiceName).append(auditLog, rhs.auditLog).append(serviceAuthenticationScheme, rhs.serviceAuthenticationScheme).append(ordering, rhs.ordering).append(disabled, rhs.disabled).append(singleFactorCert, rhs.singleFactorCert).append(singleFactorPwd, rhs.singleFactorPwd).append(required, rhs.required).isEquals();
    }

}
