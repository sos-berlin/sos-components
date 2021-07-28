
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
    "san"
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
    @JsonProperty("san")
    private String san;

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
    @JsonProperty("san")
    public String getSan() {
        return san;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("san")
    public void setSan(String san) {
        this.san = san;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("commonName", commonName).append("organizationUnit", organizationUnit).append("organization", organization).append("countryCode", countryCode).append("location", location).append("state", state).append("san", san).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(commonName).append(san).append(countryCode).append(organization).append(organizationUnit).append(location).append(state).toHashCode();
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
        return new EqualsBuilder().append(commonName, rhs.commonName).append(san, rhs.san).append(countryCode, rhs.countryCode).append(organization, rhs.organization).append(organizationUnit, rhs.organizationUnit).append(location, rhs.location).append(state, rhs.state).isEquals();
    }

}
