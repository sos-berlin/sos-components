
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
@JacksonXmlRootElement(localName = "day")
@JsonPropertyOrder({
    "day",
    "periods"
})
public class Day {

    /**
     * [01234567]|(so(nntag)?)|(mo(ntag)?)|(di(enstag)?)|(mi(ttwoch)?)|(do(nnerstag)?)|(fr(eitag)?)|(sa(mstag)?)|(sun(day)?)|(mon(day)?)|(tue(sday)?)|(wed(nesday)?)|(thu(rsday)?)|(fri(day)?)|(sat(urday)?) for weekdays (where sunday is 0 or 7) or 1-31 for monthdays and 0-30 for ultimos
     * (Required)
     * 
     */
    @JsonProperty("day")
    @JsonPropertyDescription("[01234567]|(so(nntag)?)|(mo(ntag)?)|(di(enstag)?)|(mi(ttwoch)?)|(do(nnerstag)?)|(fr(eitag)?)|(sa(mstag)?)|(sun(day)?)|(mon(day)?)|(tue(sday)?)|(wed(nesday)?)|(thu(rsday)?)|(fri(day)?)|(sat(urday)?) for weekdays (where sunday is 0 or 7) or 1-31 for monthdays and 0-30 for ultimos")
    @JacksonXmlProperty(localName = "day", isAttribute = true)
    private String day;
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
     * [01234567]|(so(nntag)?)|(mo(ntag)?)|(di(enstag)?)|(mi(ttwoch)?)|(do(nnerstag)?)|(fr(eitag)?)|(sa(mstag)?)|(sun(day)?)|(mon(day)?)|(tue(sday)?)|(wed(nesday)?)|(thu(rsday)?)|(fri(day)?)|(sat(urday)?) for weekdays (where sunday is 0 or 7) or 1-31 for monthdays and 0-30 for ultimos
     * (Required)
     * 
     */
    @JsonProperty("day")
    @JacksonXmlProperty(localName = "day", isAttribute = true)
    public String getDay() {
        return day;
    }

    /**
     * [01234567]|(so(nntag)?)|(mo(ntag)?)|(di(enstag)?)|(mi(ttwoch)?)|(do(nnerstag)?)|(fr(eitag)?)|(sa(mstag)?)|(sun(day)?)|(mon(day)?)|(tue(sday)?)|(wed(nesday)?)|(thu(rsday)?)|(fri(day)?)|(sat(urday)?) for weekdays (where sunday is 0 or 7) or 1-31 for monthdays and 0-30 for ultimos
     * (Required)
     * 
     */
    @JsonProperty("day")
    @JacksonXmlProperty(localName = "day", isAttribute = true)
    public void setDay(String day) {
        this.day = day;
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
        return new ToStringBuilder(this).append("day", day).append("periods", periods).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(day).append(periods).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Day) == false) {
            return false;
        }
        Day rhs = ((Day) other);
        return new EqualsBuilder().append(day, rhs.day).append(periods, rhs.periods).isEquals();
    }

}
