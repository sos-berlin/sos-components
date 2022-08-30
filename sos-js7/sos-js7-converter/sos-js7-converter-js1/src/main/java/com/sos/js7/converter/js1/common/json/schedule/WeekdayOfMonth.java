
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
@JacksonXmlRootElement(localName = "weekday")
@JsonPropertyOrder({
    "day",
    "which",
    "periods"
})
public class WeekdayOfMonth {

    /**
     * [01234567]|(so(nntag)?)|(mo(ntag)?)|(di(enstag)?)|(mi(ttwoch)?)|(do(nnerstag)?)|(fr(eitag)?)|(sa(mstag)?)|(sun(day)?)|(mon(day)?)|(tue(sday)?)|(wed(nesday)?)|(thu(rsday)?)|(fri(day)?)|(sat(urday)?)
     * (Required)
     * 
     */
    @JsonProperty("day")
    @JsonPropertyDescription("[01234567]|(so(nntag)?)|(mo(ntag)?)|(di(enstag)?)|(mi(ttwoch)?)|(do(nnerstag)?)|(fr(eitag)?)|(sa(mstag)?)|(sun(day)?)|(mon(day)?)|(tue(sday)?)|(wed(nesday)?)|(thu(rsday)?)|(fri(day)?)|(sat(urday)?)")
    @JacksonXmlProperty(localName = "day", isAttribute = true)
    private String day;
    /**
     * possible value: -4, -3, -2, -1, 1, 2, 3, 4
     * (Required)
     * 
     */
    @JsonProperty("which")
    @JsonPropertyDescription("possible value: -4, -3, -2, -1, 1, 2, 3, 4")
    @JacksonXmlProperty(localName = "which", isAttribute = true)
    private Integer which;
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
     * [01234567]|(so(nntag)?)|(mo(ntag)?)|(di(enstag)?)|(mi(ttwoch)?)|(do(nnerstag)?)|(fr(eitag)?)|(sa(mstag)?)|(sun(day)?)|(mon(day)?)|(tue(sday)?)|(wed(nesday)?)|(thu(rsday)?)|(fri(day)?)|(sat(urday)?)
     * (Required)
     * 
     */
    @JsonProperty("day")
    @JacksonXmlProperty(localName = "day", isAttribute = true)
    public String getDay() {
        return day;
    }

    /**
     * [01234567]|(so(nntag)?)|(mo(ntag)?)|(di(enstag)?)|(mi(ttwoch)?)|(do(nnerstag)?)|(fr(eitag)?)|(sa(mstag)?)|(sun(day)?)|(mon(day)?)|(tue(sday)?)|(wed(nesday)?)|(thu(rsday)?)|(fri(day)?)|(sat(urday)?)
     * (Required)
     * 
     */
    @JsonProperty("day")
    @JacksonXmlProperty(localName = "day", isAttribute = true)
    public void setDay(String day) {
        this.day = day;
    }

    /**
     * possible value: -4, -3, -2, -1, 1, 2, 3, 4
     * (Required)
     * 
     */
    @JsonProperty("which")
    @JacksonXmlProperty(localName = "which", isAttribute = true)
    public Integer getWhich() {
        return which;
    }

    /**
     * possible value: -4, -3, -2, -1, 1, 2, 3, 4
     * (Required)
     * 
     */
    @JsonProperty("which")
    @JacksonXmlProperty(localName = "which", isAttribute = true)
    public void setWhich(Integer which) {
        this.which = which;
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
        return new ToStringBuilder(this).append("day", day).append("which", which).append("periods", periods).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(which).append(periods).append(day).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof WeekdayOfMonth) == false) {
            return false;
        }
        WeekdayOfMonth rhs = ((WeekdayOfMonth) other);
        return new EqualsBuilder().append(which, rhs.which).append(periods, rhs.periods).append(day, rhs.day).isEquals();
    }

}
