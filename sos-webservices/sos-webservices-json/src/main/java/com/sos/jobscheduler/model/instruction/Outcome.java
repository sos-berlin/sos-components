
package com.sos.jobscheduler.model.instruction;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    "result"
})
public class Outcome {

    /**
     * outcomeType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    private OutcomeType tYPE;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("result")
    private OutcomeResult result;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Outcome() {
    }

    /**
     * 
     * @param result
     * @param tYPE
     */
    public Outcome(OutcomeType tYPE, OutcomeResult result) {
        super();
        this.tYPE = tYPE;
        this.result = result;
    }

    /**
     * outcomeType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    public OutcomeType getTYPE() {
        return tYPE;
    }

    /**
     * outcomeType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    public void setTYPE(OutcomeType tYPE) {
        this.tYPE = tYPE;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("result")
    public OutcomeResult getResult() {
        return result;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("result")
    public void setResult(OutcomeResult result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).append("result", result).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(tYPE).append(result).toHashCode();
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
        return new EqualsBuilder().append(tYPE, rhs.tYPE).append(result, rhs.result).isEquals();
    }

}
