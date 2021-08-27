
package com.sos.joc.model.order;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.calendar.AssignedCalendars;
import com.sos.inventory.model.calendar.AssignedNonWorkingDayCalendars;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Order Template Dates Filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "calendars",
    "nonWorkingDayCalendars",
    "dateFrom",
    "dateTo",
    "timeZone"
})
public class ScheduleDatesFilter {

    /**
     * Assigned Calendars
     * <p>
     * 
     * 
     */
    @JsonProperty("calendars")
    private List<AssignedCalendars> calendars = null;
    /**
     * Assigned Non Working Calendars List
     * <p>
     * 
     * 
     */
    @JsonProperty("nonWorkingDayCalendars")
    private List<AssignedNonWorkingDayCalendars> nonWorkingDayCalendars = null;
    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("dateFrom")
    @JsonPropertyDescription("ISO date YYYY-MM-DD")
    private String dateFrom;
    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("dateTo")
    @JsonPropertyDescription("ISO date YYYY-MM-DD")
    private String dateTo;
    /**
     * string without < and >
     * <p>
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * (Required)
     * 
     */
    @JsonProperty("timeZone")
    @JsonPropertyDescription("see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones")
    private String timeZone;

    /**
     * Assigned Calendars
     * <p>
     * 
     * 
     */
    @JsonProperty("calendars")
    public List<AssignedCalendars> getCalendars() {
        return calendars;
    }

    /**
     * Assigned Calendars
     * <p>
     * 
     * 
     */
    @JsonProperty("calendars")
    public void setCalendars(List<AssignedCalendars> calendars) {
        this.calendars = calendars;
    }

    /**
     * Assigned Non Working Calendars List
     * <p>
     * 
     * 
     */
    @JsonProperty("nonWorkingDayCalendars")
    public List<AssignedNonWorkingDayCalendars> getNonWorkingDayCalendars() {
        return nonWorkingDayCalendars;
    }

    /**
     * Assigned Non Working Calendars List
     * <p>
     * 
     * 
     */
    @JsonProperty("nonWorkingDayCalendars")
    public void setNonWorkingDayCalendars(List<AssignedNonWorkingDayCalendars> nonWorkingDayCalendars) {
        this.nonWorkingDayCalendars = nonWorkingDayCalendars;
    }

    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("dateFrom")
    public String getDateFrom() {
        return dateFrom;
    }

    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("dateFrom")
    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("dateTo")
    public String getDateTo() {
        return dateTo;
    }

    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("dateTo")
    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }

    /**
     * string without < and >
     * <p>
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * (Required)
     * 
     */
    @JsonProperty("timeZone")
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * string without < and >
     * <p>
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * (Required)
     * 
     */
    @JsonProperty("timeZone")
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("calendars", calendars).append("nonWorkingDayCalendars", nonWorkingDayCalendars).append("dateFrom", dateFrom).append("dateTo", dateTo).append("timeZone", timeZone).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(dateTo).append(timeZone).append(dateFrom).append(calendars).append(nonWorkingDayCalendars).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ScheduleDatesFilter) == false) {
            return false;
        }
        ScheduleDatesFilter rhs = ((ScheduleDatesFilter) other);
        return new EqualsBuilder().append(dateTo, rhs.dateTo).append(timeZone, rhs.timeZone).append(dateFrom, rhs.dateFrom).append(calendars, rhs.calendars).append(nonWorkingDayCalendars, rhs.nonWorkingDayCalendars).isEquals();
    }

}
