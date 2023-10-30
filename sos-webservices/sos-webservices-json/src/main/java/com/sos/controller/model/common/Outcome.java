
package com.sos.controller.model.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.common.Variables;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * outcome
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "message",
    "namedValues",
    "reason",
    "outcome"
})
public class Outcome {

    /**
     * Succeeded, Failed, Disrupted, Cancelled, Killed, TimedOut
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    @JsonPropertyDescription("Succeeded, Failed, Disrupted, Cancelled, Killed, TimedOut")
    private String tYPE;
    @JsonProperty("message")
    private String message;
    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("namedValues")
    @JsonPropertyDescription("a map for arbitrary key-value pairs")
    private Variables namedValues;
    @JsonProperty("reason")
    private OutcomeReason reason;
    /**
     * outcome
     * <p>
     * 
     * 
     */
    @JsonProperty("outcome")
    private Outcome outcome;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Outcome() {
    }

    /**
     * 
     * @param reason
     * @param namedValues
     * @param tYPE
     * @param message
     * @param outcome
     */
    public Outcome(String tYPE, String message, Variables namedValues, OutcomeReason reason, Outcome outcome) {
        super();
        this.tYPE = tYPE;
        this.message = message;
        this.namedValues = namedValues;
        this.reason = reason;
        this.outcome = outcome;
    }

    /**
     * Succeeded, Failed, Disrupted, Cancelled, Killed, TimedOut
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    public String getTYPE() {
        return tYPE;
    }

    /**
     * Succeeded, Failed, Disrupted, Cancelled, Killed, TimedOut
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    public void setTYPE(String tYPE) {
        this.tYPE = tYPE;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    @JsonProperty("message")
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("namedValues")
    public Variables getNamedValues() {
        return namedValues;
    }

    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("namedValues")
    public void setNamedValues(Variables namedValues) {
        this.namedValues = namedValues;
    }

    @JsonProperty("reason")
    public OutcomeReason getReason() {
        return reason;
    }

    @JsonProperty("reason")
    public void setReason(OutcomeReason reason) {
        this.reason = reason;
    }

    /**
     * outcome
     * <p>
     * 
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
     * 
     */
    @JsonProperty("outcome")
    public void setOutcome(Outcome outcome) {
        this.outcome = outcome;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).append("message", message).append("namedValues", namedValues).append("reason", reason).append("outcome", outcome).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(reason).append(tYPE).append(message).append(outcome).append(namedValues).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Outcome) == false) {
            return false;
        }
        Outcome rhs = ((Outcome) other);
        return new EqualsBuilder().append(reason, rhs.reason).append(tYPE, rhs.tYPE).append(message, rhs.message).append(outcome, rhs.outcome).append(namedValues, rhs.namedValues).isEquals();
    }

}
