
package com.sos.js7.converter.js1.common.json.schedule;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * super class for schedule and run_time
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonXmlRootElement(localName = "abstract_schedule")
@JsonPropertyOrder({
    "timeZone",
    "begin",
    "end",
    "letRun",
    "runOnce",
    "periods",
    "ats",
    "dates",
    "weekdays",
    "monthdays",
    "ultimos",
    "months",
    "holidays",
    "calendars"
})
public abstract class AbstractSchedule {

    @JsonProperty("timeZone")
    @JacksonXmlProperty(localName = "time_zone", isAttribute = true)
    private String timeZone;
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
    @JacksonXmlProperty(localName = "once", isAttribute = true)
    private String runOnce = "false";
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
    @JsonProperty("ats")
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "at", isAttribute = false)
    private List<At> ats = null;
    @JsonProperty("dates")
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "date", isAttribute = false)
    private List<Date> dates = null;
    /**
     * weekdays
     * <p>
     * 
     * 
     */
    @JsonProperty("weekdays")
    @JacksonXmlProperty(localName = "weekdays", isAttribute = false)
    private Weekdays weekdays;
    /**
     * monthdays
     * <p>
     * 
     * 
     */
    @JsonProperty("monthdays")
    @JacksonXmlProperty(localName = "monthdays", isAttribute = false)
    private Monthdays monthdays;
    /**
     * ultimos
     * <p>
     * 
     * 
     */
    @JsonProperty("ultimos")
    @JacksonXmlProperty(localName = "ultimos", isAttribute = false)
    private Ultimos ultimos;
    /**
     * months
     * <p>
     * 
     * 
     */
    @JsonProperty("months")
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "month", isAttribute = false)
    private List<Month> months = null;
    /**
     * holidays
     * <p>
     * 
     * 
     */
    @JsonProperty("holidays")
    @JacksonXmlProperty(localName = "holidays", isAttribute = false)
    private Holidays holidays;
    @JsonProperty("calendars")
    @JacksonXmlCData
    @JacksonXmlProperty(localName = "calendars", isAttribute = false)
    private String calendars;

    @JsonProperty("timeZone")
    @JacksonXmlProperty(localName = "time_zone", isAttribute = true)
    public String getTimeZone() {
        return timeZone;
    }

    @JsonProperty("timeZone")
    @JacksonXmlProperty(localName = "time_zone", isAttribute = true)
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

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
    @JacksonXmlProperty(localName = "once", isAttribute = true)
    public String getRunOnce() {
        return runOnce;
    }

    /**
     * possible values: yes, no, 1, 0, true, false
     * 
     */
    @JsonProperty("runOnce")
    @JacksonXmlProperty(localName = "once", isAttribute = true)
    public void setRunOnce(String runOnce) {
        this.runOnce = runOnce;
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

    @JsonProperty("ats")
    @JacksonXmlProperty(localName = "at", isAttribute = false)
    public List<At> getAts() {
        return ats;
    }

    @JsonProperty("ats")
    @JacksonXmlProperty(localName = "at", isAttribute = false)
    public void setAts(List<At> ats) {
        this.ats = ats;
    }

    @JsonProperty("dates")
    @JacksonXmlProperty(localName = "date", isAttribute = false)
    public List<Date> getDates() {
        return dates;
    }

    @JsonProperty("dates")
    @JacksonXmlProperty(localName = "date", isAttribute = false)
    public void setDates(List<Date> dates) {
        this.dates = dates;
    }

    /**
     * weekdays
     * <p>
     * 
     * 
     */
    @JsonProperty("weekdays")
    @JacksonXmlProperty(localName = "weekdays", isAttribute = false)
    public Weekdays getWeekdays() {
        return weekdays;
    }

    /**
     * weekdays
     * <p>
     * 
     * 
     */
    @JsonProperty("weekdays")
    @JacksonXmlProperty(localName = "weekdays", isAttribute = false)
    public void setWeekdays(Weekdays weekdays) {
        this.weekdays = weekdays;
    }

    /**
     * monthdays
     * <p>
     * 
     * 
     */
    @JsonProperty("monthdays")
    @JacksonXmlProperty(localName = "monthdays", isAttribute = false)
    public Monthdays getMonthdays() {
        return monthdays;
    }

    /**
     * monthdays
     * <p>
     * 
     * 
     */
    @JsonProperty("monthdays")
    @JacksonXmlProperty(localName = "monthdays", isAttribute = false)
    public void setMonthdays(Monthdays monthdays) {
        this.monthdays = monthdays;
    }

    /**
     * ultimos
     * <p>
     * 
     * 
     */
    @JsonProperty("ultimos")
    @JacksonXmlProperty(localName = "ultimos", isAttribute = false)
    public Ultimos getUltimos() {
        return ultimos;
    }

    /**
     * ultimos
     * <p>
     * 
     * 
     */
    @JsonProperty("ultimos")
    @JacksonXmlProperty(localName = "ultimos", isAttribute = false)
    public void setUltimos(Ultimos ultimos) {
        this.ultimos = ultimos;
    }

    /**
     * months
     * <p>
     * 
     * 
     */
    @JsonProperty("months")
    @JacksonXmlProperty(localName = "month", isAttribute = false)
    public List<Month> getMonths() {
        return months;
    }

    /**
     * months
     * <p>
     * 
     * 
     */
    @JsonProperty("months")
    @JacksonXmlProperty(localName = "month", isAttribute = false)
    public void setMonths(List<Month> months) {
        this.months = months;
    }

    /**
     * holidays
     * <p>
     * 
     * 
     */
    @JsonProperty("holidays")
    @JacksonXmlProperty(localName = "holidays", isAttribute = false)
    public Holidays getHolidays() {
        return holidays;
    }

    /**
     * holidays
     * <p>
     * 
     * 
     */
    @JsonProperty("holidays")
    @JacksonXmlProperty(localName = "holidays", isAttribute = false)
    public void setHolidays(Holidays holidays) {
        this.holidays = holidays;
    }

    @JsonProperty("calendars")
    @JacksonXmlCData
    @JacksonXmlProperty(localName = "calendars", isAttribute = false)
    public String getCalendars() {
        return calendars;
    }

    @JsonProperty("calendars")
    @JacksonXmlCData
    @JacksonXmlProperty(localName = "calendars", isAttribute = false)
    public void setCalendars(String calendars) {
        this.calendars = calendars;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("timeZone", timeZone).append("begin", begin).append("end", end).append("letRun", letRun).append("runOnce", runOnce).append("periods", periods).append("ats", ats).append("dates", dates).append("weekdays", weekdays).append("monthdays", monthdays).append("ultimos", ultimos).append("months", months).append("holidays", holidays).append("calendars", calendars).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(letRun).append(ats).append(months).append(weekdays).append(timeZone).append(dates).append(runOnce).append(holidays).append(calendars).append(periods).append(end).append(monthdays).append(begin).append(ultimos).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AbstractSchedule) == false) {
            return false;
        }
        AbstractSchedule rhs = ((AbstractSchedule) other);
        return new EqualsBuilder().append(letRun, rhs.letRun).append(ats, rhs.ats).append(months, rhs.months).append(weekdays, rhs.weekdays).append(timeZone, rhs.timeZone).append(dates, rhs.dates).append(runOnce, rhs.runOnce).append(holidays, rhs.holidays).append(calendars, rhs.calendars).append(periods, rhs.periods).append(end, rhs.end).append(monthdays, rhs.monthdays).append(begin, rhs.begin).append(ultimos, rhs.ultimos).isEquals();
    }

}
