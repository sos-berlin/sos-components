
package com.sos.joc.model.calendar;

import java.util.HashMap;
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
 * periods
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "singleStart",
    "begin",
    "end",
    "repeat",
    "whenHoliday"
})
public class Period {

    @JsonProperty("singleStart")
    private String singleStart;
    @JsonProperty("begin")
    private String begin;
    @JsonProperty("end")
    private String end;
    @JsonProperty("repeat")
    private String repeat;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("whenHoliday")
    private String whenHoliday;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("singleStart")
    public String getSingleStart() {
        return singleStart;
    }

    @JsonProperty("singleStart")
    public void setSingleStart(String singleStart) {
        this.singleStart = singleStart;
    }

    @JsonProperty("begin")
    public String getBegin() {
        return begin;
    }

    @JsonProperty("begin")
    public void setBegin(String begin) {
        this.begin = begin;
    }

    @JsonProperty("end")
    public String getEnd() {
        return end;
    }

    @JsonProperty("end")
    public void setEnd(String end) {
        this.end = end;
    }

    @JsonProperty("repeat")
    public String getRepeat() {
        return repeat;
    }

    @JsonProperty("repeat")
    public void setRepeat(String repeat) {
        this.repeat = repeat;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("whenHoliday")
    public String getWhenHoliday() {
        return whenHoliday;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("whenHoliday")
    public void setWhenHoliday(String whenHoliday) {
        this.whenHoliday = whenHoliday;
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
        return new ToStringBuilder(this).append("singleStart", singleStart).append("begin", begin).append("end", end).append("repeat", repeat).append("whenHoliday", whenHoliday).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(singleStart).append(repeat).append(end).append(additionalProperties).append(begin).append(whenHoliday).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Period) == false) {
            return false;
        }
        Period rhs = ((Period) other);
        return new EqualsBuilder().append(singleStart, rhs.singleStart).append(repeat, rhs.repeat).append(end, rhs.end).append(additionalProperties, rhs.additionalProperties).append(begin, rhs.begin).append(whenHoliday, rhs.whenHoliday).isEquals();
    }

}
