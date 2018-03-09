
package com.sos.joc.model.calendar;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * holidays
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "nationalCalendar",
    "dates"
})
public class Holidays {

    @JsonProperty("nationalCalendar")
    @JacksonXmlProperty(localName = "nationalCalendar")
    private String nationalCalendar;
    @JsonProperty("dates")
    @JacksonXmlProperty(localName = "date")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "dates")
    private List<String> dates = null;

    @JsonProperty("nationalCalendar")
    @JacksonXmlProperty(localName = "nationalCalendar")
    public String getNationalCalendar() {
        return nationalCalendar;
    }

    @JsonProperty("nationalCalendar")
    @JacksonXmlProperty(localName = "nationalCalendar")
    public void setNationalCalendar(String nationalCalendar) {
        this.nationalCalendar = nationalCalendar;
    }

    @JsonProperty("dates")
    @JacksonXmlProperty(localName = "date")
    public List<String> getDates() {
        return dates;
    }

    @JsonProperty("dates")
    @JacksonXmlProperty(localName = "date")
    public void setDates(List<String> dates) {
        this.dates = dates;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("nationalCalendar", nationalCalendar).append("dates", dates).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(nationalCalendar).append(dates).toHashCode();
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
        return new EqualsBuilder().append(nationalCalendar, rhs.nationalCalendar).append(dates, rhs.dates).isEquals();
    }

}
