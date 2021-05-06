
package com.sos.inventory.model.instruction;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * lock
 * <p>
 * instruction with fixed property 'TYPE':'Lock'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "lockName",
    "lockedWorkflow",
    "count"
})
public class Lock
    extends Instruction
{

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
     * @param count
     * @param lockedWorkflow
     * 
     * @param lockName
     */
    public Lock(String lockName, Instructions lockedWorkflow, Integer count) {
        super();
        this.lockName = lockName;
        this.lockedWorkflow = lockedWorkflow;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("lockName", lockName).append("lockedWorkflow", lockedWorkflow).append("count", count).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(count).append(lockedWorkflow).append(lockName).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(count, rhs.count).append(lockedWorkflow, rhs.lockedWorkflow).append(lockName, rhs.lockName).isEquals();
    }

}
