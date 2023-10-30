
package com.sos.joc.model.history.order.attached;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.history.order.common.WaitingForAdmission;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Moved
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "waitingForAdmission"
})
public class Attached {

    /**
     * Moved
     * <p>
     * 
     * 
     */
    @JsonProperty("waitingForAdmission")
    private WaitingForAdmission waitingForAdmission;

    /**
     * Moved
     * <p>
     * 
     * 
     */
    @JsonProperty("waitingForAdmission")
    public WaitingForAdmission getWaitingForAdmission() {
        return waitingForAdmission;
    }

    /**
     * Moved
     * <p>
     * 
     * 
     */
    @JsonProperty("waitingForAdmission")
    public void setWaitingForAdmission(WaitingForAdmission waitingForAdmission) {
        this.waitingForAdmission = waitingForAdmission;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("waitingForAdmission", waitingForAdmission).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(waitingForAdmission).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Attached) == false) {
            return false;
        }
        Attached rhs = ((Attached) other);
        return new EqualsBuilder().append(waitingForAdmission, rhs.waitingForAdmission).isEquals();
    }

}
