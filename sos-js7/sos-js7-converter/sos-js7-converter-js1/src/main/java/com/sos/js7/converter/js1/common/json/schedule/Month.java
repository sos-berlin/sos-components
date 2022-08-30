
package com.sos.js7.converter.js1.common.json.schedule;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonXmlRootElement(localName = "month")
@JsonPropertyOrder({
    "month",
    "periods",
    "weekdays",
    "monthdays",
    "ultimos"
})
public class Month {

    /**
     * unordered space separated list of 1-12 or january, february, march, april, may, june, july, august, september, october, november, december
     * 
     */
    @JsonProperty("month")
    @JsonPropertyDescription("unordered space separated list of 1-12 or january, february, march, april, may, june, july, august, september, october, november, december")
    @JacksonXmlProperty(localName = "month", isAttribute = true)
    private String month;
    /**
     * periods
     * <p>
     * 
     * 
     */
    @JsonProperty("periods")
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "period", isAttribute = false)
    private List<Period> periods = null;
    /**
     * weekdays
     * <p>
     * 
     * 
     */
    @JsonProperty("weekdays")
    @JacksonXmlProperty(localName = "weekdays", isAttribute = false)
    private Weekdays weekdays;
    /**
     * monthdays
     * <p>
     * 
     * 
     */
    @JsonProperty("monthdays")
    @JacksonXmlProperty(localName = "monthdays", isAttribute = false)
    private Monthdays monthdays;
    /**
     * ultimos
     * <p>
     * 
     * 
     */
    @JsonProperty("ultimos")
    @JacksonXmlProperty(localName = "ultimos", isAttribute = false)
    private Ultimos ultimos;

    /**
     * unordered space separated list of 1-12 or january, february, march, april, may, june, july, august, september, october, november, december
     * 
     */
    @JsonProperty("month")
    @JacksonXmlProperty(localName = "month", isAttribute = true)
    public String getMonth() {
        return month;
    }

    /**
     * unordered space separated list of 1-12 or january, february, march, april, may, june, july, august, september, october, november, december
     * 
     */
    @JsonProperty("month")
    @JacksonXmlProperty(localName = "month", isAttribute = true)
    public void setMonth(String month) {
        this.month = month;
    }

    /**
     * periods
     * <p>
     * 
     * 
     */
    @JsonProperty("periods")
    @JacksonXmlProperty(localName = "period", isAttribute = false)
    public List<Period> getPeriods() {
        return periods;
    }

    /**
     * periods
     * <p>
     * 
     * 
     */
    @JsonProperty("periods")
    @JacksonXmlProperty(localName = "period", isAttribute = false)
    public void setPeriods(List<Period> periods) {
        this.periods = periods;
    }

    /**
     * weekdays
     * <p>
     * 
     * 
     */
    @JsonProperty("weekdays")
    @JacksonXmlProperty(localName = "weekdays", isAttribute = false)
    public Weekdays getWeekdays() {
        return weekdays;
    }

    /**
     * weekdays
     * <p>
     * 
     * 
     */
    @JsonProperty("weekdays")
    @JacksonXmlProperty(localName = "weekdays", isAttribute = false)
    public void setWeekdays(Weekdays weekdays) {
        this.weekdays = weekdays;
    }

    /**
     * monthdays
     * <p>
     * 
     * 
     */
    @JsonProperty("monthdays")
    @JacksonXmlProperty(localName = "monthdays", isAttribute = false)
    public Monthdays getMonthdays() {
        return monthdays;
    }

    /**
     * monthdays
     * <p>
     * 
     * 
     */
    @JsonProperty("monthdays")
    @JacksonXmlProperty(localName = "monthdays", isAttribute = false)
    public void setMonthdays(Monthdays monthdays) {
        this.monthdays = monthdays;
    }

    /**
     * ultimos
     * <p>
     * 
     * 
     */
    @JsonProperty("ultimos")
    @JacksonXmlProperty(localName = "ultimos", isAttribute = false)
    public Ultimos getUltimos() {
        return ultimos;
    }

    /**
     * ultimos
     * <p>
     * 
     * 
     */
    @JsonProperty("ultimos")
    @JacksonXmlProperty(localName = "ultimos", isAttribute = false)
    public void setUltimos(Ultimos ultimos) {
        this.ultimos = ultimos;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("month", month).append("periods", periods).append("weekdays", weekdays).append("monthdays", monthdays).append("ultimos", ultimos).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(periods).append(monthdays).append(month).append(ultimos).append(weekdays).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Month) == false) {
            return false;
        }
        Month rhs = ((Month) other);
        return new EqualsBuilder().append(periods, rhs.periods).append(monthdays, rhs.monthdays).append(month, rhs.month).append(ultimos, rhs.ultimos).append(weekdays, rhs.weekdays).isEquals();
    }

}
