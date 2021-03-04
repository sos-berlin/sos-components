
package com.sos.controller.model.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * outcome
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "namedValues",
    "outcome"
})
public class Outcome {

    /**
     * Succeeded, Failed, Disrupted, Cancelled, Killed
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    @JsonPropertyDescription("Succeeded, Failed, Disrupted, Cancelled, Killed")
    private String tYPE;
    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("namedValues")
    @JsonPropertyDescription("a map for arbitrary key-value pairs")
    private Variables namedValues;
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
     * @param namedValues
     * @param tYPE
     * @param outcome
     */
    public Outcome(String tYPE, Variables namedValues, Outcome outcome) {
        super();
        this.tYPE = tYPE;
        this.namedValues = namedValues;
        this.outcome = outcome;
    }

    /**
     * Succeeded, Failed, Disrupted, Cancelled, Killed
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    public String getTYPE() {
        return tYPE;
    }

    /**
     * Succeeded, Failed, Disrupted, Cancelled, Killed
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    public void setTYPE(String tYPE) {
        this.tYPE = tYPE;
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
        return new ToStringBuilder(this).append("tYPE", tYPE).append("namedValues", namedValues).append("outcome", outcome).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(tYPE).append(outcome).append(namedValues).toHashCode();
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
        return new EqualsBuilder().append(tYPE, rhs.tYPE).append(outcome, rhs.outcome).append(namedValues, rhs.namedValues).isEquals();
    }

}
