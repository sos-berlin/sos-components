
package com.sos.webservices.order.initiator.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Controller Id
 * <p>
 * Only Controller Id
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId"
})
public class ControllId {

    /**
     * filename
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;

    /**
     * filename
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * filename
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllerId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ControllId) == false) {
            return false;
        }
        ControllId rhs = ((ControllId) other);
        return new EqualsBuilder().append(controllerId, rhs.controllerId).isEquals();
    }

}
