
package com.sos.jobscheduler.model.instruction;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * instruction
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE"
})
public class Instruction implements IInstruction
{

    /**
     * instructionType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    private InstructionType tYPE;

    /**
     * instructionType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    public InstructionType getTYPE() {
        return tYPE;
    }

    /**
     * instructionType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    public void setTYPE(InstructionType tYPE) {
        this.tYPE = tYPE;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(tYPE).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Instruction) == false) {
            return false;
        }
        Instruction rhs = ((Instruction) other);
        return new EqualsBuilder().append(tYPE, rhs.tYPE).isEquals();
    }

}
