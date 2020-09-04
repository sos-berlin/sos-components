
package com.sos.jobscheduler.model.instruction;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * finish
 * <p>
 * instruction with fixed property 'TYPE':'Finish'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "outcome"
})
public class Finish
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
    public Finish() {
    }

    /**
     * 
     * @param tYPE
     * @param outcome
     */
    public Finish(Outcome outcome) {
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
        if ((other instanceof Finish) == false) {
            return false;
        }
        Finish rhs = ((Finish) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(outcome, rhs.outcome).isEquals();
    }

}
