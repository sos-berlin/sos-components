
package com.sos.sign.model.instruction;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "lockPath",
    "count"
})
public class LockDemand {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("lockPath")
    @JsonAlias({
        "lockId",
        "lockName"
    })
    private String lockPath;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("count")
    private Integer count;

    /**
     * No args constructor for use in serialization
     * 
     */
    public LockDemand() {
    }

    /**
     * 
     * @param count
     * @param lockPath
     */
    public LockDemand(String lockPath, Integer count) {
        super();
        this.lockPath = lockPath;
        this.count = count;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("lockPath")
    public String getLockPath() {
        return lockPath;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("lockPath")
    public void setLockPath(String lockPath) {
        this.lockPath = lockPath;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("count")
    public Integer getCount() {
        return count;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("count")
    public void setCount(Integer count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("lockPath", lockPath).append("count", count).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(lockPath).append(count).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof LockDemand) == false) {
            return false;
        }
        LockDemand rhs = ((LockDemand) other);
        return new EqualsBuilder().append(lockPath, rhs.lockPath).append(count, rhs.count).isEquals();
    }

}
