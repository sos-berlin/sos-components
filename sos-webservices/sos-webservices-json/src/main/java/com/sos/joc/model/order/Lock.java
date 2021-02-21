
package com.sos.joc.model.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "lockId",
    "limit",
    "count",
    "lockState"
})
public class Lock {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("lockId")
    private String lockId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("limit")
    private Integer limit = 1;
    @JsonProperty("count")
    private Integer count;
    @JsonProperty("lockState")
    private LockState lockState;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("lockId")
    public String getLockId() {
        return lockId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("lockId")
    public void setLockId(String lockId) {
        this.lockId = lockId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("limit")
    public Integer getLimit() {
        return limit;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("limit")
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    @JsonProperty("count")
    public Integer getCount() {
        return count;
    }

    @JsonProperty("count")
    public void setCount(Integer count) {
        this.count = count;
    }

    @JsonProperty("lockState")
    public LockState getLockState() {
        return lockState;
    }

    @JsonProperty("lockState")
    public void setLockState(LockState lockState) {
        this.lockState = lockState;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("lockId", lockId).append("limit", limit).append("count", count).append("lockState", lockState).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(lockId).append(limit).append(count).append(lockState).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Lock) == false) {
            return false;
        }
        Lock rhs = ((Lock) other);
        return new EqualsBuilder().append(lockId, rhs.lockId).append(limit, rhs.limit).append(count, rhs.count).append(lockState, rhs.lockState).isEquals();
    }

}
