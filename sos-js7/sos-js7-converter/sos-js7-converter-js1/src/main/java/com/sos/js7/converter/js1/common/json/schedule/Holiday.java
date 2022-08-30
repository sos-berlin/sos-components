
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

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonXmlRootElement(localName = "holiday")
@JsonPropertyOrder({
    "date",
    "calendar",
    "periods"
})
public class Holiday {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("date")
    @JacksonXmlProperty(localName = "date", isAttribute = true)
    private String date;
    @JsonProperty("calendar")
    @JacksonXmlProperty(localName = "calendar", isAttribute = true)
    private String calendar;
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
     * 
     * (Required)
     * 
     */
    @JsonProperty("date")
    @JacksonXmlProperty(localName = "date", isAttribute = true)
    public String getDate() {
        return date;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("date")
    @JacksonXmlProperty(localName = "date", isAttribute = true)
    public void setDate(String date) {
        this.date = date;
    }

    @JsonProperty("calendar")
    @JacksonXmlProperty(localName = "calendar", isAttribute = true)
    public String getCalendar() {
        return calendar;
    }

    @JsonProperty("calendar")
    @JacksonXmlProperty(localName = "calendar", isAttribute = true)
    public void setCalendar(String calendar) {
        this.calendar = calendar;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("date", date).append("calendar", calendar).append("periods", periods).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(date).append(calendar).append(periods).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Holiday) == false) {
            return false;
        }
        Holiday rhs = ((Holiday) other);
        return new EqualsBuilder().append(date, rhs.date).append(calendar, rhs.calendar).append(periods, rhs.periods).isEquals();
    }

}
