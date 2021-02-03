
package com.sos.inventory.model.instruction;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * implicit end
 * <p>
 * instruction with fixed property 'TYPE':'ImplicitEnd'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({

})
public class ImplicitEnd
    extends Instruction
{


    /**
     * No args constructor for use in serialization
     * 
     */
    public ImplicitEnd() {
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
        if ((other instanceof ImplicitEnd) == false) {
            return false;
        }
        ImplicitEnd rhs = ((ImplicitEnd) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).isEquals();
    }

}
