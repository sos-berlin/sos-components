
package com.sos.joc.model.calendar;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
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
    "absoluteRepeat",
    "whenHoliday"
})
public class Period {

    @JsonProperty("singleStart")
    @JacksonXmlProperty(localName = "singleStart")
    private String singleStart;
    @JsonProperty("begin")
    @JacksonXmlProperty(localName = "begin")
    private String begin;
    @JsonProperty("end")
    @JacksonXmlProperty(localName = "end")
    private String end;
    @JsonProperty("repeat")
    @JacksonXmlProperty(localName = "repeat")
    private String repeat;
    @JsonProperty("absoluteRepeat")
    @JacksonXmlProperty(localName = "absoluteRepeat")
    private String absoluteRepeat;
    @JsonProperty("whenHoliday")
    @JacksonXmlProperty(localName = "whenHoliday")
    private String whenHoliday;

    @JsonProperty("singleStart")
    @JacksonXmlProperty(localName = "singleStart")
    public String getSingleStart() {
        return singleStart;
    }

    @JsonProperty("singleStart")
    @JacksonXmlProperty(localName = "singleStart")
    public void setSingleStart(String singleStart) {
        this.singleStart = singleStart;
    }

    @JsonProperty("begin")
    @JacksonXmlProperty(localName = "begin")
    public String getBegin() {
        return begin;
    }

    @JsonProperty("begin")
    @JacksonXmlProperty(localName = "begin")
    public void setBegin(String begin) {
        this.begin = begin;
    }

    @JsonProperty("end")
    @JacksonXmlProperty(localName = "end")
    public String getEnd() {
        return end;
    }

    @JsonProperty("end")
    @JacksonXmlProperty(localName = "end")
    public void setEnd(String end) {
        this.end = end;
    }

    @JsonProperty("repeat")
    @JacksonXmlProperty(localName = "repeat")
    public String getRepeat() {
        return repeat;
    }

    @JsonProperty("repeat")
    @JacksonXmlProperty(localName = "repeat")
    public void setRepeat(String repeat) {
        this.repeat = repeat;
    }

    @JsonProperty("absoluteRepeat")
    @JacksonXmlProperty(localName = "absoluteRepeat")
    public String getAbsoluteRepeat() {
        return absoluteRepeat;
    }

    @JsonProperty("absoluteRepeat")
    @JacksonXmlProperty(localName = "absoluteRepeat")
    public void setAbsoluteRepeat(String absoluteRepeat) {
        this.absoluteRepeat = absoluteRepeat;
    }

    @JsonProperty("whenHoliday")
    @JacksonXmlProperty(localName = "whenHoliday")
    public String getWhenHoliday() {
        return whenHoliday;
    }

    @JsonProperty("whenHoliday")
    @JacksonXmlProperty(localName = "whenHoliday")
    public void setWhenHoliday(String whenHoliday) {
        this.whenHoliday = whenHoliday;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("singleStart", singleStart).append("begin", begin).append("end", end).append("repeat", repeat).append("absoluteRepeat", absoluteRepeat).append("whenHoliday", whenHoliday).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(singleStart).append(repeat).append(end).append(absoluteRepeat).append(begin).append(whenHoliday).toHashCode();
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
        return new EqualsBuilder().append(singleStart, rhs.singleStart).append(repeat, rhs.repeat).append(end, rhs.end).append(absoluteRepeat, rhs.absoluteRepeat).append(begin, rhs.begin).append(whenHoliday, rhs.whenHoliday).isEquals();
    }

}
