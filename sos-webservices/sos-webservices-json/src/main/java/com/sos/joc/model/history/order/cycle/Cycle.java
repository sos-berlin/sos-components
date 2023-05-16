
package com.sos.joc.model.history.order.cycle;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Moved
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "prepared"
})
public class Cycle {

    /**
     * Moved
     * <p>
     * 
     * 
     */
    @JsonProperty("prepared")
    private CyclePrepared prepared;

    /**
     * Moved
     * <p>
     * 
     * 
     */
    @JsonProperty("prepared")
    public CyclePrepared getPrepared() {
        return prepared;
    }

    /**
     * Moved
     * <p>
     * 
     * 
     */
    @JsonProperty("prepared")
    public void setPrepared(CyclePrepared prepared) {
        this.prepared = prepared;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("prepared", prepared).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(prepared).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Cycle) == false) {
            return false;
        }
        Cycle rhs = ((Cycle) other);
        return new EqualsBuilder().append(prepared, rhs.prepared).isEquals();
    }

}
