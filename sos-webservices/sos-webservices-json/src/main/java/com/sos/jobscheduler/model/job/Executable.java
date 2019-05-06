
package com.sos.jobscheduler.model.job;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * executable
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE"
})
public class Executable {

    /**
     * executeType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    private ExecuteType tYPE;

    /**
     * executeType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    public ExecuteType getTYPE() {
        return tYPE;
    }

    /**
     * executeType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    public void setTYPE(ExecuteType tYPE) {
        this.tYPE = tYPE;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(tYPE).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Executable) == false) {
            return false;
        }
        Executable rhs = ((Executable) other);
        return new EqualsBuilder().append(tYPE, rhs.tYPE).isEquals();
    }

}
