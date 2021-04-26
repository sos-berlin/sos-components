
package com.sos.sign.model.instruction;

import com.fasterxml.jackson.annotation.JsonAlias;
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
    "lockPath",
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
    @JsonProperty("lockPath")
    @JsonAlias({
        "lockId",
        "lockName"
    })
    private String lockPath;
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
     * @param lockPath
     * @param lockedWorkflow
     */
    public Lock(String lockPath, Instructions lockedWorkflow, Integer count) {
        super();
        this.lockPath = lockPath;
        this.lockedWorkflow = lockedWorkflow;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("lockPath", lockPath).append("lockedWorkflow", lockedWorkflow).append("count", count).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(count).append(lockPath).append(lockedWorkflow).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(count, rhs.count).append(lockPath, rhs.lockPath).append(lockedWorkflow, rhs.lockedWorkflow).isEquals();
    }

}
