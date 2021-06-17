
package com.sos.inventory.model.instruction;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.common.Variables;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * fail
 * <p>
 * instruction with fixed property 'TYPE':'Fail'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "message",
    "outcome",
    "uncatchable"
})
public class Fail
    extends Instruction
{

    @JsonProperty("message")
    private String message;
    @JsonProperty("outcome")
    @JsonAlias({
        "namedValues"
    })
    private Variables outcome;
    @JsonProperty("uncatchable")
    private Boolean uncatchable = false;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Fail() {
    }

    /**
     * 
     * @param uncatchable
     * @param message
     * 
     * @param outcome
     */
    public Fail(String message, Variables outcome, Boolean uncatchable) {
        super();
        this.message = message;
        this.outcome = outcome;
        this.uncatchable = uncatchable;
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
    public Variables getOutcome() {
        return outcome;
    }

    @JsonProperty("outcome")
    public void setOutcome(Variables outcome) {
        this.outcome = outcome;
    }

    @JsonProperty("uncatchable")
    public Boolean getUncatchable() {
        return uncatchable;
    }

    @JsonProperty("uncatchable")
    public void setUncatchable(Boolean uncatchable) {
        this.uncatchable = uncatchable;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("message", message).append("outcome", outcome).append("uncatchable", uncatchable).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(uncatchable).append(message).append(outcome).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(uncatchable, rhs.uncatchable).append(message, rhs.message).append(outcome, rhs.outcome).isEquals();
    }

}
