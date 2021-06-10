
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
    "arguments",
    "uncatchable"
})
public class Fail
    extends Instruction
{

    @JsonProperty("message")
    private String message;
    @JsonProperty("arguments")
    @JsonAlias({
        "namedValues"
    })
    private Variables arguments;
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
     * @param arguments
     * @param uncatchable
     * @param message
     */
    public Fail(String message, Variables arguments, Boolean uncatchable) {
        super();
        this.message = message;
        this.arguments = arguments;
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

    @JsonProperty("arguments")
    public Variables getArguments() {
        return arguments;
    }

    @JsonProperty("arguments")
    public void setArguments(Variables arguments) {
        this.arguments = arguments;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("message", message).append("arguments", arguments).append("uncatchable", uncatchable).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(arguments).append(uncatchable).append(message).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(arguments, rhs.arguments).append(uncatchable, rhs.uncatchable).append(message, rhs.message).isEquals();
    }

}
