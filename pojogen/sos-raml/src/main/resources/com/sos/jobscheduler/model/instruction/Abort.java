
package com.sos.jobscheduler.model.instruction;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * abort
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "message",
    "returnCode",
    "uncatchable"
})
public class Abort
    extends Fail
{

    /**
     * No args constructor for use in serialization
     * 
     */
    public Abort() {
    	super(null, null, true);
    }

    /**
     * 
     * @param returnCode
     * @param message
     */
    public Abort(String message, Integer returnCode) {
        super(message, returnCode, true);
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
        if ((other instanceof Abort) == false) {
            return false;
        }
        //Abort rhs = ((Abort) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).isEquals();
    }

}
