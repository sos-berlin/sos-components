
package com.sos.sign.model.instruction;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.instruction.InstructionType;
import com.sos.inventory.model.instruction.schedule.CycleSchedule;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Cycle
 * <p>
 * instruction with fixed property 'TYPE':'Cycle'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "cycleWorkflow",
    "schedule",
    "onlyOnePeriod"
})
public class Cycle
    extends Instruction
{

    /**
     * instructions
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("cycleWorkflow")
    private Instructions cycleWorkflow;
    /**
     * Cycle Schedule
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("schedule")
    private CycleSchedule schedule;
    /**
     * default = false
     * 
     */
    @JsonProperty("onlyOnePeriod")
    @JsonPropertyDescription("default = false")
    private Boolean onlyOnePeriod;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Cycle() {
    }

    /**
     * 
     * @param cycleWorkflow
     * @param schedule
     * @param tYPE
     * @param onlyOnePeriod
     */
    public Cycle(Instructions cycleWorkflow, CycleSchedule schedule, Boolean onlyOnePeriod, InstructionType tYPE) {
        super(tYPE);
        this.cycleWorkflow = cycleWorkflow;
        this.schedule = schedule;
        this.onlyOnePeriod = onlyOnePeriod;
    }

    /**
     * instructions
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("cycleWorkflow")
    public Instructions getCycleWorkflow() {
        return cycleWorkflow;
    }

    /**
     * instructions
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("cycleWorkflow")
    public void setCycleWorkflow(Instructions cycleWorkflow) {
        this.cycleWorkflow = cycleWorkflow;
    }

    /**
     * Cycle Schedule
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("schedule")
    public CycleSchedule getSchedule() {
        return schedule;
    }

    /**
     * Cycle Schedule
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("schedule")
    public void setSchedule(CycleSchedule schedule) {
        this.schedule = schedule;
    }

    /**
     * default = false
     * 
     */
    @JsonProperty("onlyOnePeriod")
    public Boolean getOnlyOnePeriod() {
        return onlyOnePeriod;
    }

    /**
     * default = false
     * 
     */
    @JsonProperty("onlyOnePeriod")
    public void setOnlyOnePeriod(Boolean onlyOnePeriod) {
        this.onlyOnePeriod = onlyOnePeriod;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("cycleWorkflow", cycleWorkflow).append("schedule", schedule).append("onlyOnePeriod", onlyOnePeriod).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(cycleWorkflow).append(schedule).append(onlyOnePeriod).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Cycle) == false) {
            return false;
        }
        Cycle rhs = ((Cycle) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(cycleWorkflow, rhs.cycleWorkflow).append(schedule, rhs.schedule).append(onlyOnePeriod, rhs.onlyOnePeriod).isEquals();
    }

}
