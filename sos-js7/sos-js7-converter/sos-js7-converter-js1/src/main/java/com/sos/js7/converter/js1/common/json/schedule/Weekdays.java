
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
 * weekdays
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonXmlRootElement(localName = "weekdays")
@JsonPropertyOrder({
    "days"
})
public class Weekdays {

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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("days", days).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(days).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Weekdays) == false) {
            return false;
        }
        Weekdays rhs = ((Weekdays) other);
        return new EqualsBuilder().append(days, rhs.days).isEquals();
    }

}
