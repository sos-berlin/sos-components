
package com.sos.inventory.model.instruction;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * lock
 * <p>
 * instruction with fixed property 'TYPE':'Lock'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "lockId",
    "lockedWorkflow",
    "count"
})
public class Lock
    extends Instruction
{

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("lockId")
    private String lockId;
    /**
     * instructions
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("lockedWorkflow")
    private Instructions lockedWorkflow;
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
    public Lock() {
    }

    /**
     * 
     * @param lockId
     * @param count
     * @param lockedWorkflow
     * 
     */
    public Lock(String lockId, Instructions lockedWorkflow, Integer count) {
        super();
        this.lockId = lockId;
        this.lockedWorkflow = lockedWorkflow;
        this.count = count;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("lockId")
    public String getLockId() {
        return lockId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("lockId")
    public void setLockId(String lockId) {
        this.lockId = lockId;
    }

    /**
     * instructions
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("lockedWorkflow")
    public Instructions getLockedWorkflow() {
        return lockedWorkflow;
    }

    /**
     * instructions
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("lockedWorkflow")
    public void setLockedWorkflow(Instructions lockedWorkflow) {
        this.lockedWorkflow = lockedWorkflow;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("lockId", lockId).append("lockedWorkflow", lockedWorkflow).append("count", count).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(lockId).append(count).append(lockedWorkflow).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(lockId, rhs.lockId).append(count, rhs.count).append(lockedWorkflow, rhs.lockedWorkflow).isEquals();
    }

}
