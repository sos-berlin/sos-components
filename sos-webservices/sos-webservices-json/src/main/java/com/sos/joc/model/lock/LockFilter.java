
package com.sos.joc.model.lock;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * lock request filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "lockPath"
})
public class LockFilter {

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
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("lockPath")
    private String lockPath;

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

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("lockPath")
    public String getLockPath() {
        return lockPath;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("lockPath")
    public void setLockPath(String lockPath) {
        this.lockPath = lockPath;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("lockPath", lockPath).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllerId).append(lockPath).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof LockFilter) == false) {
            return false;
        }
        LockFilter rhs = ((LockFilter) other);
        return new EqualsBuilder().append(controllerId, rhs.controllerId).append(lockPath, rhs.lockPath).isEquals();
    }

}
