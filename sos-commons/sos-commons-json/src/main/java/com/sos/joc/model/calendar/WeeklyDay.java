
package com.sos.joc.model.calendar;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "day",
    "weekOfMonth"
})
public class WeeklyDay {

    /**
     * dayOfWeek number
     * <p>
     * digit from 0-6, 0=Sunday, 1=Monday, ..., 6=Saturday
     * 
     */
    @JsonProperty("day")
    @JsonPropertyDescription("digit from 0-6, 0=Sunday, 1=Monday, ..., 6=Saturday")
    @JacksonXmlProperty(localName = "day")
    private Integer day;
    @JsonProperty("weekOfMonth")
    @JacksonXmlProperty(localName = "weekOfMonth")
    private Integer weekOfMonth;

    /**
     * dayOfWeek number
     * <p>
     * digit from 0-6, 0=Sunday, 1=Monday, ..., 6=Saturday
     * 
     */
    @JsonProperty("day")
    @JacksonXmlProperty(localName = "day")
    public Integer getDay() {
        return day;
    }

    /**
     * dayOfWeek number
     * <p>
     * digit from 0-6, 0=Sunday, 1=Monday, ..., 6=Saturday
     * 
     */
    @JsonProperty("day")
    @JacksonXmlProperty(localName = "day")
    public void setDay(Integer day) {
        this.day = day;
    }

    @JsonProperty("weekOfMonth")
    @JacksonXmlProperty(localName = "weekOfMonth")
    public Integer getWeekOfMonth() {
        return weekOfMonth;
    }

    @JsonProperty("weekOfMonth")
    @JacksonXmlProperty(localName = "weekOfMonth")
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
