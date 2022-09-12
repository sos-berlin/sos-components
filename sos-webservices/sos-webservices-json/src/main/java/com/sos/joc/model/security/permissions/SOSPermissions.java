
package com.sos.joc.model.security.permissions;

import java.util.ArrayList;
import java.util.List;
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
    "sosPermissions"
})
public class SOSPermissions {

    @JsonProperty("sosPermissions")
    private List<String> sosPermissions = new ArrayList<String>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public SOSPermissions() {
    }

    /**
     * 
     * @param sosPermissions
     */
    public SOSPermissions(List<String> sosPermissions) {
        super();
        this.sosPermissions = sosPermissions;
    }

    @JsonProperty("sosPermissions")
    public List<String> getSosPermissions() {
        return sosPermissions;
    }

    @JsonProperty("sosPermissions")
    public void setSosPermissions(List<String> sosPermissions) {
        this.sosPermissions = sosPermissions;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("sosPermissions", sosPermissions).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(sosPermissions).toHashCode();
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
        return new EqualsBuilder().append(sosPermissions, rhs.sosPermissions).isEquals();
    }

}
