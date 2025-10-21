
package com.sos.inventory.model.calendar;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/** periods
 * <p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "singleStart", "begin", "end", "repeat", "whenHoliday" })
public class Period {

    @JsonProperty("singleStart")
    private String singleStart;
    @JsonProperty("begin")
    private String begin;
    @JsonProperty("end")
    private String end;
    @JsonProperty("repeat")
    private String repeat;

    @JsonIgnore
    private boolean absoluteRepeat;

    /** whenHoliday type
     * <p>
     * default: SUPPRESS */
    @JsonProperty("whenHoliday")
    @JsonPropertyDescription("default: SUPPRESS")
    private WhenHolidayType whenHoliday;

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

    /** whenHoliday type
     * <p>
     * default: SUPPRESS */
    @JsonProperty("whenHoliday")
    public WhenHolidayType getWhenHoliday() {
        return whenHoliday;
    }

    /** whenHoliday type
     * <p>
     * default: SUPPRESS */
    @JsonProperty("whenHoliday")
    public void setWhenHoliday(WhenHolidayType whenHoliday) {
        this.whenHoliday = whenHoliday;
    }

    @JsonIgnore
    public boolean getAbsoluteRepeat() {
        return absoluteRepeat;
    }

    public void setAbsoluteRepeat(boolean val) {
        absoluteRepeat = val;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("singleStart", singleStart).append("begin", begin).append("end", end).append("repeat", repeat).append(
                "whenHoliday", whenHoliday).append("absoluteRepeat", absoluteRepeat).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(end).append(singleStart).append(begin).append(whenHoliday).append(repeat).append(absoluteRepeat)
                .toHashCode();
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
        return new EqualsBuilder().append(end, rhs.end).append(singleStart, rhs.singleStart).append(begin, rhs.begin).append(whenHoliday,
                rhs.whenHoliday).append(repeat, rhs.repeat).append(absoluteRepeat, rhs.absoluteRepeat).isEquals();
    }

}
