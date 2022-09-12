
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
 * SOSPermission
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "SOSpermission"
})
public class SOSPermission {

    @JsonProperty("SOSpermission")
    private List<String> sOSpermission = new ArrayList<String>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public SOSPermission() {
    }

    /**
     * 
     * @param sOSpermission
     */
    public SOSPermission(List<String> sOSpermission) {
        super();
        this.sOSpermission = sOSpermission;
    }

    @JsonProperty("SOSpermission")
    public List<String> getSOSpermission() {
        return sOSpermission;
    }

    @JsonProperty("SOSpermission")
    public void setSOSpermission(List<String> sOSpermission) {
        this.sOSpermission = sOSpermission;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("sOSpermission", sOSpermission).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(sOSpermission).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SOSPermission) == false) {
            return false;
        }
        SOSPermission rhs = ((SOSPermission) other);
        return new EqualsBuilder().append(sOSpermission, rhs.sOSpermission).isEquals();
    }

}
