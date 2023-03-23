
package com.sos.inventory.model.instruction;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * break
 * <p>
 * instruction with fixed property 'TYPE':'Break'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({

})
public class Break
    extends Instruction
{


    /**
     * No args constructor for use in serialization
     * 
     */
    public Break() {
    }

    /**
     * 
     * @param position
     * @param label
     * @param state
     * 
     * @param positionString
     */
    public Break(, List<Object> position, String positionString, String label, InstructionState state) {
        super(, position, positionString, label, state);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Break) == false) {
            return false;
        }
        Break rhs = ((Break) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).isEquals();
    }

}
