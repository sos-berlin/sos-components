
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
 * DaysOfMonth for MonthDays or Ultimos
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "from",
    "to",
    "days",
    "weeklyDays"
})
public class MonthDays {

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
    @JsonProperty("days")
    private List<Integer> days = null;
    @JsonProperty("weeklyDays")
    private List<WeeklyDay> weeklyDays = null;

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

    @JsonProperty("days")
    public List<Integer> getDays() {
        return days;
    }

    @JsonProperty("days")
    public void setDays(List<Integer> days) {
        this.days = days;
    }

    @JsonProperty("weeklyDays")
    public List<WeeklyDay> getWeeklyDays() {
        return weeklyDays;
    }

    @JsonProperty("weeklyDays")
    public void setWeeklyDays(List<WeeklyDay> weeklyDays) {
        this.weeklyDays = weeklyDays;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("from", from).append("to", to).append("days", days).append("weeklyDays", weeklyDays).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(days).append(weeklyDays).append(from).append(to).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof MonthDays) == false) {
            return false;
        }
        MonthDays rhs = ((MonthDays) other);
        return new EqualsBuilder().append(days, rhs.days).append(weeklyDays, rhs.weeklyDays).append(from, rhs.from).append(to, rhs.to).isEquals();
    }

}
