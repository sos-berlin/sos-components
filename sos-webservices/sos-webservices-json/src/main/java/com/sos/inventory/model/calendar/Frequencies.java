
package com.sos.inventory.model.calendar;

import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * frequencies
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "dates",
    "weekdays",
    "monthdays",
    "ultimos",
    "months",
    "holidays",
    "repetitions",
    "nonWorkingDayCalendars"
})
public class Frequencies {

    @JsonProperty("dates")
    private List<String> dates = null;
    @JsonProperty("weekdays")
    private List<WeekDays> weekdays = null;
    @JsonProperty("monthdays")
    private List<MonthDays> monthdays = null;
    @JsonProperty("ultimos")
    private List<MonthDays> ultimos = null;
    @JsonProperty("months")
    private List<Months> months = null;
    @JsonProperty("holidays")
    private List<Holidays> holidays = null;
    @JsonProperty("repetitions")
    private List<Repetition> repetitions = null;
    @JsonProperty("nonWorkingDayCalendars")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> nonWorkingDayCalendars = null;

    @JsonProperty("dates")
    public List<String> getDates() {
        return dates;
    }

    @JsonProperty("dates")
    public void setDates(List<String> dates) {
        this.dates = dates;
    }

    @JsonProperty("weekdays")
    public List<WeekDays> getWeekdays() {
        return weekdays;
    }

    @JsonProperty("weekdays")
    public void setWeekdays(List<WeekDays> weekdays) {
        this.weekdays = weekdays;
    }

    @JsonProperty("monthdays")
    public List<MonthDays> getMonthdays() {
        return monthdays;
    }

    @JsonProperty("monthdays")
    public void setMonthdays(List<MonthDays> monthdays) {
        this.monthdays = monthdays;
    }

    @JsonProperty("ultimos")
    public List<MonthDays> getUltimos() {
        return ultimos;
    }

    @JsonProperty("ultimos")
    public void setUltimos(List<MonthDays> ultimos) {
        this.ultimos = ultimos;
    }

    @JsonProperty("months")
    public List<Months> getMonths() {
        return months;
    }

    @JsonProperty("months")
    public void setMonths(List<Months> months) {
        this.months = months;
    }

    @JsonProperty("holidays")
    public List<Holidays> getHolidays() {
        return holidays;
    }

    @JsonProperty("holidays")
    public void setHolidays(List<Holidays> holidays) {
        this.holidays = holidays;
    }

    @JsonProperty("repetitions")
    public List<Repetition> getRepetitions() {
        return repetitions;
    }

    @JsonProperty("repetitions")
    public void setRepetitions(List<Repetition> repetitions) {
        this.repetitions = repetitions;
    }

    @JsonProperty("nonWorkingDayCalendars")
    public Set<String> getNonWorkingDayCalendars() {
        return nonWorkingDayCalendars;
    }

    @JsonProperty("nonWorkingDayCalendars")
    public void setNonWorkingDayCalendars(Set<String> nonWorkingDayCalendars) {
        this.nonWorkingDayCalendars = nonWorkingDayCalendars;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("dates", dates).append("weekdays", weekdays).append("monthdays", monthdays).append("ultimos", ultimos).append("months", months).append("holidays", holidays).append("repetitions", repetitions).append("nonWorkingDayCalendars", nonWorkingDayCalendars).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(months).append(holidays).append(weekdays).append(nonWorkingDayCalendars).append(dates).append(monthdays).append(ultimos).append(repetitions).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Frequencies) == false) {
            return false;
        }
        Frequencies rhs = ((Frequencies) other);
        return new EqualsBuilder().append(months, rhs.months).append(holidays, rhs.holidays).append(weekdays, rhs.weekdays).append(nonWorkingDayCalendars, rhs.nonWorkingDayCalendars).append(dates, rhs.dates).append(monthdays, rhs.monthdays).append(ultimos, rhs.ultimos).append(repetitions, rhs.repetitions).isEquals();
    }

}
