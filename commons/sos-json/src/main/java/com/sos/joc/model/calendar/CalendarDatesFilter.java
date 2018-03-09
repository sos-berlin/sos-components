
package com.sos.joc.model.calendar;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * calendarDatesFilter
 * <p>
 * one of the fields calendar and path is required.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobschedulerId",
    "calendar",
    "id",
    "path",
    "dateFrom",
    "dateTo"
})
public class CalendarDatesFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    private String jobschedulerId;
    /**
     * calendar
     * <p>
     * 
     * 
     */
    @JsonProperty("calendar")
    @JacksonXmlProperty(localName = "calendar")
    private Calendar calendar;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    @JacksonXmlProperty(localName = "id")
    private Long id;
    @JsonProperty("path")
    @JacksonXmlProperty(localName = "path")
    private String path;
    @JsonProperty("dateFrom")
    @JacksonXmlProperty(localName = "dateFrom")
    private String dateFrom;
    @JsonProperty("dateTo")
    @JacksonXmlProperty(localName = "dateTo")
    private String dateTo;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }

    /**
     * calendar
     * <p>
     * 
     * 
     */
    @JsonProperty("calendar")
    @JacksonXmlProperty(localName = "calendar")
    public Calendar getCalendar() {
        return calendar;
    }

    /**
     * calendar
     * <p>
     * 
     * 
     */
    @JsonProperty("calendar")
    @JacksonXmlProperty(localName = "calendar")
    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    @JacksonXmlProperty(localName = "id")
    public Long getId() {
        return id;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    @JacksonXmlProperty(localName = "id")
    public void setId(Long id) {
        this.id = id;
    }

    @JsonProperty("path")
    @JacksonXmlProperty(localName = "path")
    public String getPath() {
        return path;
    }

    @JsonProperty("path")
    @JacksonXmlProperty(localName = "path")
    public void setPath(String path) {
        this.path = path;
    }

    @JsonProperty("dateFrom")
    @JacksonXmlProperty(localName = "dateFrom")
    public String getDateFrom() {
        return dateFrom;
    }

    @JsonProperty("dateFrom")
    @JacksonXmlProperty(localName = "dateFrom")
    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    @JsonProperty("dateTo")
    @JacksonXmlProperty(localName = "dateTo")
    public String getDateTo() {
        return dateTo;
    }

    @JsonProperty("dateTo")
    @JacksonXmlProperty(localName = "dateTo")
    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("calendar", calendar).append("id", id).append("path", path).append("dateFrom", dateFrom).append("dateTo", dateTo).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(calendar).append(path).append(dateTo).append(id).append(jobschedulerId).append(dateFrom).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CalendarDatesFilter) == false) {
            return false;
        }
        CalendarDatesFilter rhs = ((CalendarDatesFilter) other);
        return new EqualsBuilder().append(calendar, rhs.calendar).append(path, rhs.path).append(dateTo, rhs.dateTo).append(id, rhs.id).append(jobschedulerId, rhs.jobschedulerId).append(dateFrom, rhs.dateFrom).isEquals();
    }

}
