
package com.sos.inventory.model.descriptor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "licenseKeyFile",
    "licenseBinFile"
})
public class License {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("licenseKeyFile")
    private String licenseKeyFile;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("licenseBinFile")
    private String licenseBinFile;

    /**
     * No args constructor for use in serialization
     * 
     */
    public License() {
    }

    /**
     * 
     * @param licenseBinFile
     * @param licenseKeyFile
     */
    public License(String licenseKeyFile, String licenseBinFile) {
        super();
        this.licenseKeyFile = licenseKeyFile;
        this.licenseBinFile = licenseBinFile;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("licenseKeyFile")
    public String getLicenseKeyFile() {
        return licenseKeyFile;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("licenseKeyFile")
    public void setLicenseKeyFile(String licenseKeyFile) {
        this.licenseKeyFile = licenseKeyFile;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("licenseBinFile")
    public String getLicenseBinFile() {
        return licenseBinFile;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("licenseBinFile")
    public void setLicenseBinFile(String licenseBinFile) {
        this.licenseBinFile = licenseBinFile;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("licenseKeyFile", licenseKeyFile).append("licenseBinFile", licenseBinFile).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(licenseKeyFile).append(licenseBinFile).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof License) == false) {
            return false;
        }
        License rhs = ((License) other);
        return new EqualsBuilder().append(licenseKeyFile, rhs.licenseKeyFile).append(licenseBinFile, rhs.licenseBinFile).isEquals();
    }

}
