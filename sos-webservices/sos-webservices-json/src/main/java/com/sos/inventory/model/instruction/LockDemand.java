
package com.sos.inventory.model.instruction;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "lockName",
    "count"
})
public class LockDemand {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("lockName")
    @JsonAlias({
        "lockId",
        "lockPath"
    })
    private String lockName;
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
     * @param lockName
     */
    public LockDemand(String lockName, Integer count) {
        super();
        this.lockName = lockName;
        this.count = count;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("lockName")
    public String getLockName() {
        return lockName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("lockName")
    public void setLockName(String lockName) {
        this.lockName = lockName;
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
        return new ToStringBuilder(this).append("lockName", lockName).append("count", count).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(count).append(lockName).toHashCode();
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
        return new EqualsBuilder().append(count, rhs.count).append(lockName, rhs.lockName).isEquals();
    }

}
