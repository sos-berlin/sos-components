
package com.sos.inventory.model.instruction;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Segment
 * <p>
 * instruction with fixed property 'TYPE':'Segment'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "label",
    "block"
})
public class Segment
    extends Instruction
{

    /**
     * instructions
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("block")
    private Instructions block;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Segment() {
    }

    /**
     * 
     * @param block
     */
    public Segment(Instructions block) {
        this.block = block;
    }

    /**
     * instructions
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("block")
    public Instructions getBlock() {
        return block;
    }

    /**
     * instructions
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("block")
    public void setBlock(Instructions block) {
        this.block = block;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("block", block).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(block).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Segment) == false) {
            return false;
        }
        Segment rhs = ((Segment) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(block, rhs.block).isEquals();
    }

}
