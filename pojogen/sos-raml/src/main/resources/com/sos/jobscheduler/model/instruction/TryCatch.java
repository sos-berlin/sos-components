
package com.sos.jobscheduler.model.instruction;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * try catch
 * <p>
 * instruction of TYPE:'Try'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "maxTries",
    "retryDelays",
    "try",
    "catch"
})
public class TryCatch
    extends RetryCatch
{

    /**
     * No args constructor for use in serialization
     * 
     */
    public TryCatch() {
    	super(null, null, new ArrayList<Instruction>(), new ArrayList<Instruction>());
    }
    
    /**
     * 
     * @param _try
     * 
     */
    public TryCatch(List<Instruction> _try) {
    	super(null, null, _try, new ArrayList<Instruction>());
    }
    
    /**
     * 
     * @param _try
     * @param _catch
     * 
     */
    public TryCatch(List<Instruction> _try, List<Instruction> _catch) {
    	super(null, null, _try, _catch);
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
        if ((other instanceof TryCatch) == false) {
            return false;
        }
        return new EqualsBuilder().appendSuper(super.equals(other)).isEquals();
    }

}
