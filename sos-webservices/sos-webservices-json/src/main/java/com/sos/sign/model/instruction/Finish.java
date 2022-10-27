
package com.sos.sign.model.instruction;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.instruction.InstructionType;
import com.sos.sign.model.common.Outcome;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * finish
 * <p>
 * instruction with fixed property 'TYPE':'Finish'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "message",
    "outcome"
})
public class Finish
    extends Instruction
{

    @JsonProperty("message")
    private String message;
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
     * @param message
     * @param tYPE
     * @param outcome
     */
    public Finish(String message, Outcome outcome, InstructionType tYPE) {
        super(tYPE);
        this.message = message;
        this.outcome = outcome;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    @JsonProperty("message")
    public void setMessage(String message) {
        this.message = message;
    }

    @JsonProperty("outcome")
    public Outcome getOutcome() {
        return outcome;
    }

    @JsonProperty("outcome")
    public void setOutcome(Outcome outcome) {
        this.outcome = outcome;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("message", message).append("outcome", outcome).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(message).append(outcome).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(message, rhs.message).append(outcome, rhs.outcome).isEquals();
    }

}
