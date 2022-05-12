
package com.sos.joc.model.joc;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * js7-license information
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "valid",
    "validFrom",
    "validUntil",
    "type"
})
public class Js7LicenseInfo {

    @JsonProperty("valid")
    private Boolean valid = false;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("validFrom")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date validFrom;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("validUntil")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date validUntil;
    /**
     * sos js7 license types
     * <p>
     * 
     * 
     */
    @JsonProperty("type")
    private LicenseType type;

    @JsonProperty("valid")
    public Boolean getValid() {
        return valid;
    }

    @JsonProperty("valid")
    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("validFrom")
    public Date getValidFrom() {
        return validFrom;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("validFrom")
    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("validUntil")
    public Date getValidUntil() {
        return validUntil;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("validUntil")
    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }

    /**
     * sos js7 license types
     * <p>
     * 
     * 
     */
    @JsonProperty("type")
    public LicenseType getType() {
        return type;
    }

    /**
     * sos js7 license types
     * <p>
     * 
     * 
     */
    @JsonProperty("type")
    public void setType(LicenseType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("valid", valid).append("validFrom", validFrom).append("validUntil", validUntil).append("type", type).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(valid).append(validUntil).append(validFrom).append(type).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Js7LicenseInfo) == false) {
            return false;
        }
        Js7LicenseInfo rhs = ((Js7LicenseInfo) other);
        return new EqualsBuilder().append(valid, rhs.valid).append(validUntil, rhs.validUntil).append(validFrom, rhs.validFrom).append(type, rhs.type).isEquals();
    }

}
