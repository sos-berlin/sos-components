
package com.sos.jobscheduler.model.instruction;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * implicit retry
 * <p>
 * instruction with fixed property 'TYPE':'Retry' (used in catch block of try instruction)
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RetryInCatch
    extends Instruction
{


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
        if ((other instanceof RetryInCatch) == false) {
            return false;
        }
        RetryInCatch rhs = ((RetryInCatch) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).isEquals();
    }

}
