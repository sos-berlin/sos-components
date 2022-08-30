
package com.sos.js7.converter.js1.common.json.schedule;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * period
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonXmlRootElement(localName = "period")
@JsonPropertyOrder({
    "begin",
    "end",
    "singleStart",
    "letRun",
    "runOnce",
    "repeat",
    "absoluteRepeat",
    "whenHoliday"
})
public class Period {

    /**
     * pattern: [0-9]{1,2}:[0-9]{2}(:[0-9]{2})?
     * 
     */
    @JsonProperty("begin")
    @JsonPropertyDescription("pattern: [0-9]{1,2}:[0-9]{2}(:[0-9]{2})?")
    @JacksonXmlProperty(localName = "begin", isAttribute = true)
    private String begin;
    /**
     * pattern: [0-9]{1,2}:[0-9]{2}(:[0-9]{2})?
     * 
     */
    @JsonProperty("end")
    @JsonPropertyDescription("pattern: [0-9]{1,2}:[0-9]{2}(:[0-9]{2})?")
    @JacksonXmlProperty(localName = "end", isAttribute = true)
    private String end;
    /**
     * pattern: [0-9]{1,2}:[0-9]{2}(:[0-9]{2})?
     * 
     */
    @JsonProperty("singleStart")
    @JsonPropertyDescription("pattern: [0-9]{1,2}:[0-9]{2}(:[0-9]{2})?")
    @JacksonXmlProperty(localName = "single_start", isAttribute = true)
    private String singleStart;
    /**
     * possible values: yes, no, 1, 0, true, false
     * 
     */
    @JsonProperty("letRun")
    @JsonPropertyDescription("possible values: yes, no, 1, 0, true, false")
    @JacksonXmlProperty(localName = "let_run", isAttribute = true)
    private String letRun = "false";
    /**
     * possible values: yes, no, 1, 0, true, false
     * 
     */
    @JsonProperty("runOnce")
    @JsonPropertyDescription("possible values: yes, no, 1, 0, true, false")
    @JacksonXmlProperty(localName = "start_once", isAttribute = true)
    private String runOnce = "false";
    /**
     * pattern: ([0-9]+)|([0-9]+:[0-9]{2}(:[0-9]{2})?)
     * 
     */
    @JsonProperty("repeat")
    @JsonPropertyDescription("pattern: ([0-9]+)|([0-9]+:[0-9]{2}(:[0-9]{2})?)")
    @JacksonXmlProperty(localName = "repeat", isAttribute = true)
    private String repeat;
    /**
     * pattern: ([0-9]+)|([0-9]+:[0-9]{2}(:[0-9]{2})?)
     * 
     */
    @JsonProperty("absoluteRepeat")
    @JsonPropertyDescription("pattern: ([0-9]+)|([0-9]+:[0-9]{2}(:[0-9]{2})?)")
    @JacksonXmlProperty(localName = "absolute_repeat", isAttribute = true)
    private String absoluteRepeat;
    /**
     * possible values: suppress (default), ignore_holiday, previous_non_holiday, next_non_holiday
     * 
     */
    @JsonProperty("whenHoliday")
    @JsonPropertyDescription("possible values: suppress (default), ignore_holiday, previous_non_holiday, next_non_holiday")
    @JacksonXmlProperty(localName = "when_holiday", isAttribute = true)
    private String whenHoliday = "suppress";

    /**
     * pattern: [0-9]{1,2}:[0-9]{2}(:[0-9]{2})?
     * 
     */
    @JsonProperty("begin")
    @JacksonXmlProperty(localName = "begin", isAttribute = true)
    public String getBegin() {
        return begin;
    }

    /**
     * pattern: [0-9]{1,2}:[0-9]{2}(:[0-9]{2})?
     * 
     */
    @JsonProperty("begin")
    @JacksonXmlProperty(localName = "begin", isAttribute = true)
    public void setBegin(String begin) {
        this.begin = begin;
    }

    /**
     * pattern: [0-9]{1,2}:[0-9]{2}(:[0-9]{2})?
     * 
     */
    @JsonProperty("end")
    @JacksonXmlProperty(localName = "end", isAttribute = true)
    public String getEnd() {
        return end;
    }

    /**
     * pattern: [0-9]{1,2}:[0-9]{2}(:[0-9]{2})?
     * 
     */
    @JsonProperty("end")
    @JacksonXmlProperty(localName = "end", isAttribute = true)
    public void setEnd(String end) {
        this.end = end;
    }

    /**
     * pattern: [0-9]{1,2}:[0-9]{2}(:[0-9]{2})?
     * 
     */
    @JsonProperty("singleStart")
    @JacksonXmlProperty(localName = "single_start", isAttribute = true)
    public String getSingleStart() {
        return singleStart;
    }

    /**
     * pattern: [0-9]{1,2}:[0-9]{2}(:[0-9]{2})?
     * 
     */
    @JsonProperty("singleStart")
    @JacksonXmlProperty(localName = "single_start", isAttribute = true)
    public void setSingleStart(String singleStart) {
        this.singleStart = singleStart;
    }

    /**
     * possible values: yes, no, 1, 0, true, false
     * 
     */
    @JsonProperty("letRun")
    @JacksonXmlProperty(localName = "let_run", isAttribute = true)
    public String getLetRun() {
        return letRun;
    }

    /**
     * possible values: yes, no, 1, 0, true, false
     * 
     */
    @JsonProperty("letRun")
    @JacksonXmlProperty(localName = "let_run", isAttribute = true)
    public void setLetRun(String letRun) {
        this.letRun = letRun;
    }

    /**
     * possible values: yes, no, 1, 0, true, false
     * 
     */
    @JsonProperty("runOnce")
    @JacksonXmlProperty(localName = "start_once", isAttribute = true)
    public String getRunOnce() {
        return runOnce;
    }

    /**
     * possible values: yes, no, 1, 0, true, false
     * 
     */
    @JsonProperty("runOnce")
    @JacksonXmlProperty(localName = "start_once", isAttribute = true)
    public void setRunOnce(String runOnce) {
        this.runOnce = runOnce;
    }

    /**
     * pattern: ([0-9]+)|([0-9]+:[0-9]{2}(:[0-9]{2})?)
     * 
     */
    @JsonProperty("repeat")
    @JacksonXmlProperty(localName = "repeat", isAttribute = true)
    public String getRepeat() {
        return repeat;
    }

    /**
     * pattern: ([0-9]+)|([0-9]+:[0-9]{2}(:[0-9]{2})?)
     * 
     */
    @JsonProperty("repeat")
    @JacksonXmlProperty(localName = "repeat", isAttribute = true)
    public void setRepeat(String repeat) {
        this.repeat = repeat;
    }

    /**
     * pattern: ([0-9]+)|([0-9]+:[0-9]{2}(:[0-9]{2})?)
     * 
     */
    @JsonProperty("absoluteRepeat")
    @JacksonXmlProperty(localName = "absolute_repeat", isAttribute = true)
    public String getAbsoluteRepeat() {
        return absoluteRepeat;
    }

    /**
     * pattern: ([0-9]+)|([0-9]+:[0-9]{2}(:[0-9]{2})?)
     * 
     */
    @JsonProperty("absoluteRepeat")
    @JacksonXmlProperty(localName = "absolute_repeat", isAttribute = true)
    public void setAbsoluteRepeat(String absoluteRepeat) {
        this.absoluteRepeat = absoluteRepeat;
    }

    /**
     * possible values: suppress (default), ignore_holiday, previous_non_holiday, next_non_holiday
     * 
     */
    @JsonProperty("whenHoliday")
    @JacksonXmlProperty(localName = "when_holiday", isAttribute = true)
    public String getWhenHoliday() {
        return whenHoliday;
    }

    /**
     * possible values: suppress (default), ignore_holiday, previous_non_holiday, next_non_holiday
     * 
     */
    @JsonProperty("whenHoliday")
    @JacksonXmlProperty(localName = "when_holiday", isAttribute = true)
    public void setWhenHoliday(String whenHoliday) {
        this.whenHoliday = whenHoliday;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("begin", begin).append("end", end).append("singleStart", singleStart).append("letRun", letRun).append("runOnce", runOnce).append("repeat", repeat).append("absoluteRepeat", absoluteRepeat).append("whenHoliday", whenHoliday).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(letRun).append(singleStart).append(repeat).append(end).append(absoluteRepeat).append(begin).append(runOnce).append(whenHoliday).toHashCode();
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
        return new EqualsBuilder().append(letRun, rhs.letRun).append(singleStart, rhs.singleStart).append(repeat, rhs.repeat).append(end, rhs.end).append(absoluteRepeat, rhs.absoluteRepeat).append(begin, rhs.begin).append(runOnce, rhs.runOnce).append(whenHoliday, rhs.whenHoliday).isEquals();
    }

}
