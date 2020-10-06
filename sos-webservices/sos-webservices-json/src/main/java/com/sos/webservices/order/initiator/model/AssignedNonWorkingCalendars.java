
package com.sos.webservices.order.initiator.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "calendarPath"
})
public class AssignedNonWorkingCalendars {

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("calendarPath")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String calendarPath;

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("calendarPath")
    public String getCalendarPath() {
        return calendarPath;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("calendarPath")
    public void setCalendarPath(String calendarPath) {
        this.calendarPath = calendarPath;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("calendarPath", calendarPath).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(calendarPath).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AssignedNonWorkingCalendars) == false) {
            return false;
        }
        AssignedNonWorkingCalendars rhs = ((AssignedNonWorkingCalendars) other);
        return new EqualsBuilder().append(calendarPath, rhs.calendarPath).isEquals();
    }

}
