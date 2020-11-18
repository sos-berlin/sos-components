
package com.sos.joc.model.lock;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * lockConfigurationFilter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "lock"
})
public class LockConfigurationFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("lock")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String lock;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("lock")
    public String getLock() {
        return lock;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("lock")
    public void setLock(String lock) {
        this.lock = lock;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("lock", lock).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllerId).append(lock).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof LockConfigurationFilter) == false) {
            return false;
        }
        LockConfigurationFilter rhs = ((LockConfigurationFilter) other);
        return new EqualsBuilder().append(controllerId, rhs.controllerId).append(lock, rhs.lock).isEquals();
    }

}
