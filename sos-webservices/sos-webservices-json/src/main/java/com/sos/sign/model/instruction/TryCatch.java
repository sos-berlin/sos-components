package com.sos.sign.model.instruction;

import java.util.Collections;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * try catch
 * <p>
 * instruction of TYPE:'Try'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
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
        super(null, null, new Instructions());
        setCatch(new Instructions(Collections.emptyList()));
    }
    
    /**
     * 
     * @param _try
     * 
     */
    public TryCatch(Instructions _try) {
        super(null, null, _try);
        setCatch(new Instructions(Collections.emptyList()));
    }
    
    /**
     * 
     * @param _try
     * @param _catch
     * 
     */
    public TryCatch(Instructions _try, Instructions _catch) {
        super(null, null, _try);
        setCatch(_catch);
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