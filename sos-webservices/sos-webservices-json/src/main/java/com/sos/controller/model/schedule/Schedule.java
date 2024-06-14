
package com.sos.controller.model.schedule;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * schedule
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "valid"
})
public class Schedule
    extends com.sos.inventory.model.schedule.Schedule
{

    @JsonProperty("valid")
    private Boolean valid;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Schedule() {
    }

    @JsonProperty("valid")
    public Boolean getValid() {
        return valid;
    }

    @JsonProperty("valid")
    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("valid", valid).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(valid).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Schedule) == false) {
            return false;
        }
        Schedule rhs = ((Schedule) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(valid, rhs.valid).isEquals();
    }

}
