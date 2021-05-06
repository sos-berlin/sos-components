
package com.sos.inventory.model.instruction;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * instructions
 * <p>
 * only for the validation, not used as pojo
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "instructions"
})
public class OptionalInstructions {

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
    public OptionalInstructions() {
    }

    /**
     * 
     * @param instructions
     */
    public OptionalInstructions(List<Instruction> instructions) {
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
        if ((other instanceof OptionalInstructions) == false) {
            return false;
        }
        OptionalInstructions rhs = ((OptionalInstructions) other);
        return new EqualsBuilder().append(instructions, rhs.instructions).isEquals();
    }

}
