
package com.sos.inventory.model.calendar;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "calendarName",
    "calendarPath"
})
public class AssignedNonWorkingDayCalendars {

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("calendarName")
    private String calendarName;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("calendarPath")
    private String calendarPath;

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("calendarName")
    public String getCalendarName() {
        return calendarName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("calendarName")
    public void setCalendarName(String calendarName) {
        this.calendarName = calendarName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("calendarPath")
    public String getCalendarPath() {
        return calendarPath;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("calendarPath")
    public void setCalendarPath(String calendarPath) {
        this.calendarPath = calendarPath;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("calendarName", calendarName).append("calendarPath", calendarPath).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(calendarName).append(calendarPath).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AssignedNonWorkingDayCalendars) == false) {
            return false;
        }
        AssignedNonWorkingDayCalendars rhs = ((AssignedNonWorkingDayCalendars) other);
        return new EqualsBuilder().append(calendarName, rhs.calendarName).append(calendarPath, rhs.calendarPath).isEquals();
    }

}
