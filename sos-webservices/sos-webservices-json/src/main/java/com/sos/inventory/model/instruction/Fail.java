
package com.sos.inventory.model.instruction;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * fail
 * <p>
 * instruction with fixed property 'TYPE':'Fail' and optional outcome with fixed property 'TYPE':'Failed'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "outcome"
})
public class Fail
    extends Instruction
{

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
    public Fail() {
    }

    /**
     * 
     * @param position
     * 
     * @param outcome
     */
    public Fail(Outcome outcome) {
        super();
        this.outcome = outcome;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("outcome", outcome).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(outcome).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Fail) == false) {
            return false;
        }
        Fail rhs = ((Fail) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(outcome, rhs.outcome).isEquals();
    }

}
