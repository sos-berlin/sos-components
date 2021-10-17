
package com.sos.inventory.model.instruction.schedule;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Cycle Schedule
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "schemes"
})
public class CycleSchedule {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("schemes")
    private List<Scheme> schemes = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public CycleSchedule() {
    }

    /**
     * 
     * @param schemes
     */
    public CycleSchedule(List<Scheme> schemes) {
        super();
        this.schemes = schemes;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("schemes")
    public List<Scheme> getSchemes() {
        return schemes;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("schemes")
    public void setSchemes(List<Scheme> schemes) {
        this.schemes = schemes;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("schemes", schemes).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(schemes).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CycleSchedule) == false) {
            return false;
        }
        CycleSchedule rhs = ((CycleSchedule) other);
        return new EqualsBuilder().append(schemes, rhs.schemes).isEquals();
    }

}
