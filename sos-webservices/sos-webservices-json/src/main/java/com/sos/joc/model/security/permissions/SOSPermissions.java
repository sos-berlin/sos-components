
package com.sos.joc.model.security.permissions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * SOSPermissions
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "SOSPermissions"
})
public class SOSPermissions {

    /**
     * SOSPermission
     * <p>
     * 
     * 
     */
    @JsonProperty("SOSPermissions")
    private SOSPermission sOSPermissions;

    /**
     * No args constructor for use in serialization
     * 
     */
    public SOSPermissions() {
    }

    /**
     * 
     * @param sOSPermissions
     */
    public SOSPermissions(SOSPermission sOSPermissions) {
        super();
        this.sOSPermissions = sOSPermissions;
    }

    /**
     * SOSPermission
     * <p>
     * 
     * 
     */
    @JsonProperty("SOSPermissions")
    public SOSPermission getSOSPermissions() {
        return sOSPermissions;
    }

    /**
     * SOSPermission
     * <p>
     * 
     * 
     */
    @JsonProperty("SOSPermissions")
    public void setSOSPermissions(SOSPermission sOSPermissions) {
        this.sOSPermissions = sOSPermissions;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("sOSPermissions", sOSPermissions).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(sOSPermissions).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SOSPermissions) == false) {
            return false;
        }
        SOSPermissions rhs = ((SOSPermissions) other);
        return new EqualsBuilder().append(sOSPermissions, rhs.sOSPermissions).isEquals();
    }

}
