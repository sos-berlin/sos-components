
package com.sos.inventory.model.instruction;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * lock
 * <p>
 * instruction with fixed property 'TYPE':'Lock'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "lockName",
    "count",
    "lockedWorkflow",
    "demands"
})
public class Lock
    extends Instruction
{

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
     * instructions
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("lockedWorkflow")
    private Instructions lockedWorkflow;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("demands")
    private List<LockDemand> demands = null;

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
     * @param demands
     */
    public Lock(String lockName, Integer count, Instructions lockedWorkflow, List<LockDemand> demands) {
        super();
        this.lockName = lockName;
        this.count = count;
        this.lockedWorkflow = lockedWorkflow;
        this.demands = demands;
    }

    @JsonProperty("lockName")
    public String getLockName() {
        return lockName;
    }

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
     * 
     * (Required)
     * 
     */
    @JsonProperty("demands")
    public List<LockDemand> getDemands() {
        return demands;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("demands")
    public void setDemands(List<LockDemand> demands) {
        this.demands = demands;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("lockName", lockName).append("count", count).append("lockedWorkflow", lockedWorkflow).append("demands", demands).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(count).append(lockedWorkflow).append(lockName).append(demands).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(count, rhs.count).append(lockedWorkflow, rhs.lockedWorkflow).append(lockName, rhs.lockName).append(demands, rhs.demands).isEquals();
    }

}
