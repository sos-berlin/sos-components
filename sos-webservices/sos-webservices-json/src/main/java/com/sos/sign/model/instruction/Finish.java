
package com.sos.sign.model.instruction;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * finish
 * <p>
 * instruction with fixed property 'TYPE':'Finish'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({

})
public class Finish
    extends Instruction
{


    /**
     * No args constructor for use in serialization
     * 
     */
    public Finish() {
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
        if ((other instanceof Finish) == false) {
            return false;
        }
        Finish rhs = ((Finish) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).isEquals();
    }

}
