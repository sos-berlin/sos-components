
package com.sos.sign.model.instruction;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.instruction.InstructionType;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * sleep
 * <p>
 * instruction with fixed property 'TYPE':'Sleep'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "duration"
})
public class Sleep
    extends Instruction
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("duration")
    private String duration;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Sleep() {
    }

    /**
     * 
     * @param duration
     * @param tYPE
     */
    public Sleep(String duration, InstructionType tYPE) {
        super(tYPE);
        this.duration = duration;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("duration")
    public String getDuration() {
        return duration;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("duration")
    public void setDuration(String duration) {
        this.duration = duration;
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("duration", duration).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(duration).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Sleep) == false) {
            return false;
        }
        Sleep rhs = ((Sleep) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(duration, rhs.duration).isEquals();
    }

}
