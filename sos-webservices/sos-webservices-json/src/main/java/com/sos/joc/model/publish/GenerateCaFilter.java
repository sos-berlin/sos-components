
package com.sos.joc.model.publish;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * set generate CA filter
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
    "auditLog"
})
public class GenerateCaFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("commonName")
    private String commonName;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("organizationUnit")
    private String organizationUnit;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("organization")
    private String organization;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("countryCode")
    private String countryCode;
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
    @JsonProperty("commonName")
    public String getCommonName() {
        return commonName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("commonName")
    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("organizationUnit")
    public String getOrganizationUnit() {
        return organizationUnit;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("organizationUnit")
    public void setOrganizationUnit(String organizationUnit) {
        this.organizationUnit = organizationUnit;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("organization")
    public String getOrganization() {
        return organization;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("organization")
    public void setOrganization(String organization) {
        this.organization = organization;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("countryCode")
    public String getCountryCode() {
        return countryCode;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("countryCode")
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
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
        return new ToStringBuilder(this).append("commonName", commonName).append("organizationUnit", organizationUnit).append("organization", organization).append("countryCode", countryCode).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(commonName).append(organizationUnit).append(auditLog).append(countryCode).append(organization).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof GenerateCaFilter) == false) {
            return false;
        }
        GenerateCaFilter rhs = ((GenerateCaFilter) other);
        return new EqualsBuilder().append(commonName, rhs.commonName).append(organizationUnit, rhs.organizationUnit).append(auditLog, rhs.auditLog).append(countryCode, rhs.countryCode).append(organization, rhs.organization).isEquals();
    }

}
