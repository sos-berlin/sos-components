
package com.sos.sign.model.instruction;

import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

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
    "lockedWorkflow",
    "demands"
})
public class Lock
    extends Instruction
{

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
     * @param lockedWorkflow
     * @param demands
     */
    public Lock(Instructions lockedWorkflow, List<LockDemand> demands) {
        super();
        this.lockedWorkflow = lockedWorkflow;
        this.demands = demands;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("lockedWorkflow", lockedWorkflow).append("demands", demands).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(lockedWorkflow).append(demands).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(lockedWorkflow, rhs.lockedWorkflow).append(demands, rhs.demands).isEquals();
    }

}
