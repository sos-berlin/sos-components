
package com.sos.js7.converter.js1.common.json.calendars;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "day", "weekOfMonth" })
public class WeeklyDay {

    /** dayOfWeek number
     * <p>
     * digit from 0-6, 0=Sunday, 1=Monday, ..., 6=Saturday */
    @JsonProperty("day")
    @JsonPropertyDescription("digit from 0-6, 0=Sunday, 1=Monday, ..., 6=Saturday")
    private Integer day;
    @JsonProperty("weekOfMonth")
    private Integer weekOfMonth;

    /** dayOfWeek number
     * <p>
     * digit from 0-6, 0=Sunday, 1=Monday, ..., 6=Saturday */
    @JsonProperty("day")
    public Integer getDay() {
        return day;
    }

    /** dayOfWeek number
     * <p>
     * digit from 0-6, 0=Sunday, 1=Monday, ..., 6=Saturday */
    @JsonProperty("day")
    public void setDay(Integer day) {
        this.day = day;
    }

    @JsonProperty("weekOfMonth")
    public Integer getWeekOfMonth() {
        return weekOfMonth;
    }

    @JsonProperty("weekOfMonth")
    public void setWeekOfMonth(Integer weekOfMonth) {
        this.weekOfMonth = weekOfMonth;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("day", day).append("weekOfMonth", weekOfMonth).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(day).append(weekOfMonth).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof WeeklyDay) == false) {
            return false;
        }
        WeeklyDay rhs = ((WeeklyDay) other);
        return new EqualsBuilder().append(day, rhs.day).append(weekOfMonth, rhs.weekOfMonth).isEquals();
    }

}
