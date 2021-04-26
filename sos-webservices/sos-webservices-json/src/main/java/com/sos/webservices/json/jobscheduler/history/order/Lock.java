
package com.sos.webservices.json.jobscheduler.history.order;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "lockName",
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
    @JsonProperty("lockName")
    @JsonAlias({ "lockPath", "lockId" })
    private String lockName;
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
        return new ToStringBuilder(this).append("lockName", lockName).append("limit", limit).append("count", count).append("lockState", lockState).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(limit).append(count).append(lockName).append(lockState).toHashCode();
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
        return new EqualsBuilder().append(limit, rhs.limit).append(count, rhs.count).append(lockName, rhs.lockName).append(lockState, rhs.lockState).isEquals();
    }

}
