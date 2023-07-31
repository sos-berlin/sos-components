
package com.sos.inventory.model.instruction;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * Options
 * <p>
 * instruction with fixed property 'TYPE':'Options'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "stopOnFailure",
    "block"
})
public class Options
    extends Instruction
{

    @JsonProperty("stopOnFailure")
    private Boolean stopOnFailure;
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
    public Options() {
    }

    /**
     * 
     * @param stopOnFailure
     * @param block
     * 
     */
    public Options(Boolean stopOnFailure, Instructions block) {
        super();
        this.stopOnFailure = stopOnFailure;
        this.block = block;
    }

    @JsonProperty("stopOnFailure")
    public Boolean getStopOnFailure() {
        return stopOnFailure;
    }

    @JsonProperty("stopOnFailure")
    public void setStopOnFailure(Boolean stopOnFailure) {
        this.stopOnFailure = stopOnFailure;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("stopOnFailure", stopOnFailure).append("block", block).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(block).append(stopOnFailure).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Options) == false) {
            return false;
        }
        Options rhs = ((Options) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(block, rhs.block).append(stopOnFailure, rhs.stopOnFailure).isEquals();
    }

}
