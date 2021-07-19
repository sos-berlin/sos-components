
package com.sos.joc.model.publish;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * sets the properties to create a (C)ertificate (S)igning (R)equest filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "commonName",
    "organizationUnit",
    "organization",
    "countryCode",
    "location",
    "state",
    "alias",
    "subjectAltName",
    "auditLog"
})
public class CreateCSRFilter {

    @JsonProperty("commonName")
    private String commonName;
    @JsonProperty("organizationUnit")
    private String organizationUnit;
    @JsonProperty("organization")
    private String organization;
    @JsonProperty("countryCode")
    private String countryCode;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("location")
    private String location;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("state")
    private String state;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("alias")
    private String alias;
    @JsonProperty("subjectAltName")
    private String subjectAltName;
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    @JsonProperty("commonName")
    public String getCommonName() {
        return commonName;
    }

    @JsonProperty("commonName")
    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    @JsonProperty("organizationUnit")
    public String getOrganizationUnit() {
        return organizationUnit;
    }

    @JsonProperty("organizationUnit")
    public void setOrganizationUnit(String organizationUnit) {
        this.organizationUnit = organizationUnit;
    }

    @JsonProperty("organization")
    public String getOrganization() {
        return organization;
    }

    @JsonProperty("organization")
    public void setOrganization(String organization) {
        this.organization = organization;
    }

    @JsonProperty("countryCode")
    public String getCountryCode() {
        return countryCode;
    }

    @JsonProperty("countryCode")
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("location")
    public String getLocation() {
        return location;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("location")
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("state")
    public String getState() {
        return state;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("state")
    public void setState(String state) {
        this.state = state;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("alias")
    public String getAlias() {
        return alias;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("alias")
    public void setAlias(String alias) {
        this.alias = alias;
    }

    @JsonProperty("subjectAltName")
    public String getSubjectAltName() {
        return subjectAltName;
    }

    @JsonProperty("subjectAltName")
    public void setSubjectAltName(String subjectAltName) {
        this.subjectAltName = subjectAltName;
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
        return new ToStringBuilder(this).append("commonName", commonName).append("organizationUnit", organizationUnit).append("organization", organization).append("countryCode", countryCode).append("location", location).append("state", state).append("alias", alias).append("subjectAltName", subjectAltName).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(commonName).append(auditLog).append(countryCode).append(organization).append(alias).append(organizationUnit).append(location).append(state).append(subjectAltName).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CreateCSRFilter) == false) {
            return false;
        }
        CreateCSRFilter rhs = ((CreateCSRFilter) other);
        return new EqualsBuilder().append(commonName, rhs.commonName).append(auditLog, rhs.auditLog).append(countryCode, rhs.countryCode).append(organization, rhs.organization).append(alias, rhs.alias).append(organizationUnit, rhs.organizationUnit).append(location, rhs.location).append(state, rhs.state).append(subjectAltName, rhs.subjectAltName).isEquals();
    }

}
