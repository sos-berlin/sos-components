
package com.sos.inventory.model.instruction;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.instruction.schedule.CycleSchedule;


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
    @JsonProperty("onlyOnePeriod")
    private Boolean onlyOnePeriod = false;

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
     */
    public Cycle(Instructions cycleWorkflow, CycleSchedule schedule) {
        super();
        this.cycleWorkflow = cycleWorkflow;
        this.schedule = schedule;
        this.onlyOnePeriod = false;
    }
    
    /**
     * 
     * @param cycleWorkflow
     * @param schedule
     * @param onlyOnePeriod
     */
    public Cycle(Instructions cycleWorkflow, CycleSchedule schedule, Boolean onlyOnePeriod) {
        super();
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

    @JsonProperty("onlyOnePeriod")
    public Boolean getOnlyOnePeriod() {
        return onlyOnePeriod;
    }

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
