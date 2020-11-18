
package com.sos.webservices.order.initiator.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.calendar.Frequencies;
import com.sos.joc.model.calendar.Period;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "calendarPath",
    "timeZone",
    "includes",
    "excludes",
    "periods"
})
public class AssignedCalendars {

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
    @JsonProperty("timeZone")
    private String timeZone = "Etc/UTC";
    /**
     * frequencies
     * <p>
     * 
     * 
     */
    @JsonProperty("includes")
    private Frequencies includes;
    /**
     * frequencies
     * <p>
     * 
     * 
     */
    @JsonProperty("excludes")
    private Frequencies excludes;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("periods")
    private List<Period> periods = null;

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

    @JsonProperty("timeZone")
    public String getTimeZone() {
        return timeZone;
    }

    @JsonProperty("timeZone")
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * frequencies
     * <p>
     * 
     * 
     */
    @JsonProperty("includes")
    public Frequencies getIncludes() {
        return includes;
    }

    /**
     * frequencies
     * <p>
     * 
     * 
     */
    @JsonProperty("includes")
    public void setIncludes(Frequencies includes) {
        this.includes = includes;
    }

    /**
     * frequencies
     * <p>
     * 
     * 
     */
    @JsonProperty("excludes")
    public Frequencies getExcludes() {
        return excludes;
    }

    /**
     * frequencies
     * <p>
     * 
     * 
     */
    @JsonProperty("excludes")
    public void setExcludes(Frequencies excludes) {
        this.excludes = excludes;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("periods")
    public List<Period> getPeriods() {
        return periods;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("periods")
    public void setPeriods(List<Period> periods) {
        this.periods = periods;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("calendarPath", calendarPath).append("timeZone", timeZone).append("includes", includes).append("excludes", excludes).append("periods", periods).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(timeZone).append(periods).append(calendarPath).append(includes).append(excludes).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AssignedCalendars) == false) {
            return false;
        }
        AssignedCalendars rhs = ((AssignedCalendars) other);
        return new EqualsBuilder().append(timeZone, rhs.timeZone).append(periods, rhs.periods).append(calendarPath, rhs.calendarPath).append(includes, rhs.includes).append(excludes, rhs.excludes).isEquals();
    }

}
