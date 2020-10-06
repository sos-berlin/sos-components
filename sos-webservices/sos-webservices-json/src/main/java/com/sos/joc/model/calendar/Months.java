
package com.sos.joc.model.calendar;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * month
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "months",
    "from",
    "to",
    "weekdays",
    "monthdays",
    "ultimos"
})
public class Months {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("months")
    private List<Integer> months = null;
    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("from")
    @JsonPropertyDescription("ISO date YYYY-MM-DD")
    private String from;
    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("to")
    @JsonPropertyDescription("ISO date YYYY-MM-DD")
    private String to;
    @JsonProperty("weekdays")
    private List<WeekDays> weekdays = null;
    @JsonProperty("monthdays")
    private List<MonthDays> monthdays = null;
    @JsonProperty("ultimos")
    private List<MonthDays> ultimos = null;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("months")
    public List<Integer> getMonths() {
        return months;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("months")
    public void setMonths(List<Integer> months) {
        this.months = months;
    }

    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("from")
    public String getFrom() {
        return from;
    }

    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("from")
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("to")
    public String getTo() {
        return to;
    }

    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("to")
    public void setTo(String to) {
        this.to = to;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("months", months).append("from", from).append("to", to).append("weekdays", weekdays).append("monthdays", monthdays).append("ultimos", ultimos).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(months).append(weekdays).append(from).append(monthdays).append(to).append(ultimos).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Months) == false) {
            return false;
        }
        Months rhs = ((Months) other);
        return new EqualsBuilder().append(months, rhs.months).append(weekdays, rhs.weekdays).append(from, rhs.from).append(monthdays, rhs.monthdays).append(to, rhs.to).append(ultimos, rhs.ultimos).isEquals();
    }

}
