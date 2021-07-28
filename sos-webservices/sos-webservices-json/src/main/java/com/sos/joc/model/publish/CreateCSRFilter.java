
package com.sos.joc.model.publish;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
    "subjectAltName"
})
public class CreateCSRFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("commonName")
    private String commonName;
    @JsonProperty("organizationUnit")
    private String organizationUnit;
    @JsonProperty("organization")
    private String organization;
    @JsonProperty("countryCode")
    private String countryCode;
    @JsonProperty("location")
    private String location;
    @JsonProperty("state")
    private String state;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("subjectAltName")
    private String subjectAltName;

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

    @JsonProperty("location")
    public String getLocation() {
        return location;
    }

    @JsonProperty("location")
    public void setLocation(String location) {
        this.location = location;
    }

    @JsonProperty("state")
    public String getState() {
        return state;
    }

    @JsonProperty("state")
    public void setState(String state) {
        this.state = state;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("subjectAltName")
    public String getSubjectAltName() {
        return subjectAltName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("subjectAltName")
    public void setSubjectAltName(String subjectAltName) {
        this.subjectAltName = subjectAltName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("commonName", commonName).append("organizationUnit", organizationUnit).append("organization", organization).append("countryCode", countryCode).append("location", location).append("state", state).append("subjectAltName", subjectAltName).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(commonName).append(countryCode).append(organization).append(organizationUnit).append(location).append(state).append(subjectAltName).toHashCode();
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
        return new EqualsBuilder().append(commonName, rhs.commonName).append(countryCode, rhs.countryCode).append(organization, rhs.organization).append(organizationUnit, rhs.organizationUnit).append(location, rhs.location).append(state, rhs.state).append(subjectAltName, rhs.subjectAltName).isEquals();
    }

}
