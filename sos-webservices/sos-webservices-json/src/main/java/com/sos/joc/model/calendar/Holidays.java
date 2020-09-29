
package com.sos.joc.model.calendar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("nationalCalendar")
    private String nationalCalendar;
    @JsonProperty("dates")
    private List<String> dates = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("nationalCalendar")
    public String getNationalCalendar() {
        return nationalCalendar;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("nationalCalendar")
    public void setNationalCalendar(String nationalCalendar) {
        this.nationalCalendar = nationalCalendar;
    }

    @JsonProperty("dates")
    public List<String> getDates() {
        return dates;
    }

    @JsonProperty("dates")
    public void setDates(List<String> dates) {
        this.dates = dates;
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
        return new ToStringBuilder(this).append("nationalCalendar", nationalCalendar).append("dates", dates).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(dates).append(additionalProperties).append(nationalCalendar).toHashCode();
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
        return new EqualsBuilder().append(dates, rhs.dates).append(additionalProperties, rhs.additionalProperties).append(nationalCalendar, rhs.nationalCalendar).isEquals();
    }

}
