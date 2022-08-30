
package com.sos.js7.converter.js1.common.json.schedule;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * monthdays
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonXmlRootElement(localName = "monthdays")
@JsonPropertyOrder({
    "days",
    "weekdays"
})
public class Monthdays {

    /**
     * days
     * <p>
     * 
     * 
     */
    @JsonProperty("days")
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "day", isAttribute = false)
    private List<Day> days = null;
    @JsonProperty("weekdays")
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "weekday", isAttribute = false)
    private List<WeekdayOfMonth> weekdays = null;

    /**
     * days
     * <p>
     * 
     * 
     */
    @JsonProperty("days")
    @JacksonXmlProperty(localName = "day", isAttribute = false)
    public List<Day> getDays() {
        return days;
    }

    /**
     * days
     * <p>
     * 
     * 
     */
    @JsonProperty("days")
    @JacksonXmlProperty(localName = "day", isAttribute = false)
    public void setDays(List<Day> days) {
        this.days = days;
    }

    @JsonProperty("weekdays")
    @JacksonXmlProperty(localName = "weekday", isAttribute = false)
    public List<WeekdayOfMonth> getWeekdays() {
        return weekdays;
    }

    @JsonProperty("weekdays")
    @JacksonXmlProperty(localName = "weekday", isAttribute = false)
    public void setWeekdays(List<WeekdayOfMonth> weekdays) {
        this.weekdays = weekdays;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("days", days).append("weekdays", weekdays).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(days).append(weekdays).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Monthdays) == false) {
            return false;
        }
        Monthdays rhs = ((Monthdays) other);
        return new EqualsBuilder().append(days, rhs.days).append(weekdays, rhs.weekdays).isEquals();
    }

}
