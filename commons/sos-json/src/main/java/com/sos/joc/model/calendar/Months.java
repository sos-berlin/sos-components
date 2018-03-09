
package com.sos.joc.model.calendar;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
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
    @JacksonXmlProperty(localName = "month")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "months")
    private List<Integer> months = null;
    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("from")
    @JsonPropertyDescription("ISO date YYYY-MM-DD")
    @JacksonXmlProperty(localName = "from")
    private String from;
    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("to")
    @JsonPropertyDescription("ISO date YYYY-MM-DD")
    @JacksonXmlProperty(localName = "to")
    private String to;
    @JsonProperty("weekdays")
    @JacksonXmlProperty(localName = "weekday")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "weekdays")
    private List<WeekDays> weekdays = null;
    @JsonProperty("monthdays")
    @JacksonXmlProperty(localName = "monthday")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "monthdays")
    private List<MonthDays> monthdays = null;
    @JsonProperty("ultimos")
    @JacksonXmlProperty(localName = "ultimo")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "ultimos")
    private List<MonthDays> ultimos = null;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("months")
    @JacksonXmlProperty(localName = "month")
    public List<Integer> getMonths() {
        return months;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("months")
    @JacksonXmlProperty(localName = "month")
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
    @JacksonXmlProperty(localName = "from")
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
    @JacksonXmlProperty(localName = "from")
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
    @JacksonXmlProperty(localName = "to")
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
    @JacksonXmlProperty(localName = "to")
    public void setTo(String to) {
        this.to = to;
    }

    @JsonProperty("weekdays")
    @JacksonXmlProperty(localName = "weekday")
    public List<WeekDays> getWeekdays() {
        return weekdays;
    }

    @JsonProperty("weekdays")
    @JacksonXmlProperty(localName = "weekday")
    public void setWeekdays(List<WeekDays> weekdays) {
        this.weekdays = weekdays;
    }

    @JsonProperty("monthdays")
    @JacksonXmlProperty(localName = "monthday")
    public List<MonthDays> getMonthdays() {
        return monthdays;
    }

    @JsonProperty("monthdays")
    @JacksonXmlProperty(localName = "monthday")
    public void setMonthdays(List<MonthDays> monthdays) {
        this.monthdays = monthdays;
    }

    @JsonProperty("ultimos")
    @JacksonXmlProperty(localName = "ultimo")
    public List<MonthDays> getUltimos() {
        return ultimos;
    }

    @JsonProperty("ultimos")
    @JacksonXmlProperty(localName = "ultimo")
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
