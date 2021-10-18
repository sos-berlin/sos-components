
package com.sos.inventory.model.workflow;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.job.Environment;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "instructions",
    "result"
})
public class BranchWorkflow {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("instructions")
    private List<Instruction> instructions = null;
    /**
     * key-value pairs particularly to assign parameters to environment
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("result")
    @JsonPropertyDescription("a map for arbitrary key-value pairs")
    private Environment result;

    /**
     * No args constructor for use in serialization
     * 
     */
    public BranchWorkflow() {
    }

    /**
     * 
     * @param result
     * @param instructions
     */
    public BranchWorkflow(List<Instruction> instructions, Environment result) {
        super();
        this.instructions = instructions;
        this.result = result;
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

    /**
     * key-value pairs particularly to assign parameters to environment
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("result")
    public Environment getResult() {
        return result;
    }

    /**
     * key-value pairs particularly to assign parameters to environment
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("result")
    public void setResult(Environment result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("instructions", instructions).append("result", result).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(result).append(instructions).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof BranchWorkflow) == false) {
            return false;
        }
        BranchWorkflow rhs = ((BranchWorkflow) other);
        return new EqualsBuilder().append(result, rhs.result).append(instructions, rhs.instructions).isEquals();
    }

}
