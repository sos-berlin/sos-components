
package com.sos.inventory.model.instruction;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * instructions
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "instructions"
})
public class Instructions {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("instructions")
    private List<Instruction> instructions = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Instructions() {
    }

    /**
     * 
     * @param instructions
     */
    public Instructions(List<Instruction> instructions) {
        super();
        this.instructions = instructions;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("instructions")
    public List<Instruction> getInstructions() {
        return instructions;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("instructions")
    public void setInstructions(List<Instruction> instructions) {
        this.instructions = instructions;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("instructions", instructions).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(instructions).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Instructions) == false) {
            return false;
        }
        Instructions rhs = ((Instructions) other);
        return new EqualsBuilder().append(instructions, rhs.instructions).isEquals();
    }

}
