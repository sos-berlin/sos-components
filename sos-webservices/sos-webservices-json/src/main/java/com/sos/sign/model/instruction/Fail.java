
package com.sos.sign.model.instruction;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.common.Variables;
import com.sos.inventory.model.instruction.InstructionType;
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
    "namedValues",
    "uncatchable"
})
public class Fail
    extends Instruction
{

    @JsonProperty("message")
    private String message;
    @JsonProperty("namedValues")
    @JsonAlias({
        "outcome"
    })
    private Variables namedValues;
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
     * @param namedValues
     * @param uncatchable
     * @param message
     * @param tYPE
     */
    public Fail(String message, Variables namedValues, Boolean uncatchable, InstructionType tYPE) {
        super(tYPE);
        this.message = message;
        this.namedValues = namedValues;
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

    @JsonProperty("namedValues")
    public Variables getNamedValues() {
        return namedValues;
    }

    @JsonProperty("namedValues")
    public void setNamedValues(Variables namedValues) {
        this.namedValues = namedValues;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("message", message).append("namedValues", namedValues).append("uncatchable", uncatchable).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(uncatchable).append(message).append(namedValues).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(uncatchable, rhs.uncatchable).append(message, rhs.message).append(namedValues, rhs.namedValues).isEquals();
    }

}
