
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
 * holidays
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonXmlRootElement(localName = "holidays")
@JsonPropertyOrder({
    "includes",
    "weekdays",
    "days"
})
public class Holidays {

    /**
     * include collection
     * <p>
     * 
     * 
     */
    @JsonProperty("includes")
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "include", isAttribute = false)
    private List<com.sos.js7.converter.js1.common.json.Include> includes = null;
    /**
     * weekdays
     * <p>
     * 
     * 
     */
    @JsonProperty("weekdays")
    @JacksonXmlProperty(localName = "weekdays", isAttribute = false)
    private Weekdays weekdays;
    @JsonProperty("days")
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "holiday", isAttribute = false)
    private List<Holiday> days = null;

    /**
     * include collection
     * <p>
     * 
     * 
     */
    @JsonProperty("includes")
    @JacksonXmlProperty(localName = "include", isAttribute = false)
    public List<com.sos.js7.converter.js1.common.json.Include> getIncludes() {
        return includes;
    }

    /**
     * include collection
     * <p>
     * 
     * 
     */
    @JsonProperty("includes")
    @JacksonXmlProperty(localName = "include", isAttribute = false)
    public void setIncludes(List<com.sos.js7.converter.js1.common.json.Include> includes) {
        this.includes = includes;
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

    @JsonProperty("days")
    @JacksonXmlProperty(localName = "holiday", isAttribute = false)
    public List<Holiday> getDays() {
        return days;
    }

    @JsonProperty("days")
    @JacksonXmlProperty(localName = "holiday", isAttribute = false)
    public void setDays(List<Holiday> days) {
        this.days = days;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("includes", includes).append("weekdays", weekdays).append("days", days).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(days).append(includes).append(weekdays).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Holidays) == false) {
            return false;
        }
        Holidays rhs = ((Holidays) other);
        return new EqualsBuilder().append(days, rhs.days).append(includes, rhs.includes).append(weekdays, rhs.weekdays).isEquals();
    }

}
