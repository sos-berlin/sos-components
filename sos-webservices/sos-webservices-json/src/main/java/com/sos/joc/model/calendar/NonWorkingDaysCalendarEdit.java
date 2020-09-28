
package com.sos.joc.model.calendar;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.inventory.ConfigurationObject;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * NonWorkingDays Calendar Edit configuration
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "configuration"
})
public class NonWorkingDaysCalendarEdit
    extends ConfigurationObject
{

    /**
     * calendar
     * <p>
     * 
     * 
     */
    @JsonProperty("configuration")
    private Calendar configuration;

    /**
     * calendar
     * <p>
     * 
     * 
     */
    @JsonProperty("configuration")
    public Calendar getConfiguration() {
        return configuration;
    }

    /**
     * calendar
     * <p>
     * 
     * 
     */
    @JsonProperty("configuration")
    public void setConfiguration(Calendar configuration) {
        this.configuration = configuration;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("configuration", configuration).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(configuration).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof NonWorkingDaysCalendarEdit) == false) {
            return false;
        }
        NonWorkingDaysCalendarEdit rhs = ((NonWorkingDaysCalendarEdit) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(configuration, rhs.configuration).isEquals();
    }

}
