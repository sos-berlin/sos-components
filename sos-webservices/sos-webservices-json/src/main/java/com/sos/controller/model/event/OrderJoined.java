
package com.sos.controller.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.controller.model.common.Outcome;
import com.sos.controller.model.common.VariablesDiff;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * OrderJoined event
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "variablesDiff",
    "outcome"
})
public class OrderJoined
    extends Event
{

    /**
     * changes of key-value pairs
     * <p>
     * 
     * 
     */
    @JsonProperty("variablesDiff")
    private VariablesDiff variablesDiff;
    /**
     * outcome
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("outcome")
    private Outcome outcome;

    /**
     * No args constructor for use in serialization
     * 
     */
    public OrderJoined() {
    }

    /**
     * 
     * @param variablesDiff
     * @param eventId
     * 
     * @param outcome
     */
    public OrderJoined(VariablesDiff variablesDiff, Outcome outcome, Long eventId) {
        super(eventId);
        this.variablesDiff = variablesDiff;
        this.outcome = outcome;
    }

    /**
     * changes of key-value pairs
     * <p>
     * 
     * 
     */
    @JsonProperty("variablesDiff")
    public VariablesDiff getVariablesDiff() {
        return variablesDiff;
    }

    /**
     * changes of key-value pairs
     * <p>
     * 
     * 
     */
    @JsonProperty("variablesDiff")
    public void setVariablesDiff(VariablesDiff variablesDiff) {
        this.variablesDiff = variablesDiff;
    }

    /**
     * outcome
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("outcome")
    public Outcome getOutcome() {
        return outcome;
    }

    /**
     * outcome
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("outcome")
    public void setOutcome(Outcome outcome) {
        this.outcome = outcome;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("variablesDiff", variablesDiff).append("outcome", outcome).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(variablesDiff).append(outcome).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderJoined) == false) {
            return false;
        }
        OrderJoined rhs = ((OrderJoined) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(variablesDiff, rhs.variablesDiff).append(outcome, rhs.outcome).isEquals();
    }

}
