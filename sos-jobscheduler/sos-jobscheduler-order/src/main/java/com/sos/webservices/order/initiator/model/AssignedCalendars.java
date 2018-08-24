
package com.sos.webservices.order.initiator.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "calendarPath",
    "periods"
})
public class AssignedCalendars {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("calendarPath")
    @JacksonXmlProperty(localName = "calendarPath")
    private String calendarPath;
    @JsonProperty("periods")
    @JacksonXmlProperty(localName = "period")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "periods")
    private List<Period> periods = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("calendarPath")
    @JacksonXmlProperty(localName = "calendarPath")
    public String getCalendarPath() {
        return calendarPath;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("calendarPath")
    @JacksonXmlProperty(localName = "calendarPath")
    public void setCalendarPath(String calendarPath) {
        this.calendarPath = calendarPath;
    }

    @JsonProperty("periods")
    @JacksonXmlProperty(localName = "period")
    public List<Period> getPeriods() {
        return periods;
    }

    @JsonProperty("periods")
    @JacksonXmlProperty(localName = "period")
    public void setPeriods(List<Period> periods) {
        this.periods = periods;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("calendarPath", calendarPath).append("periods", periods).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(periods).append(calendarPath).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AssignedCalendars) == false) {
            return false;
        }
        AssignedCalendars rhs = ((AssignedCalendars) other);
        return new EqualsBuilder().append(periods, rhs.periods).append(calendarPath, rhs.calendarPath).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
