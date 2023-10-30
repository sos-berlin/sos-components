
package com.sos.joc.model.calendar;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.calendar.Calendar;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * calendarDatesFilter
 * <p>
 * one of the fields: calendar, id or path is required.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "calendar",
    "id",
    "path",
    "dateFrom",
    "dateTo"
})
public class CalendarDatesFilter {

    /**
     * calendar
     * <p>
     * 
     * 
     */
    @JsonProperty("calendar")
    private Calendar calendar;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    private Long id;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("path")
    private String path;
    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("dateFrom")
    @JsonPropertyDescription("ISO date YYYY-MM-DD")
    private String dateFrom;
    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("dateTo")
    @JsonPropertyDescription("ISO date YYYY-MM-DD")
    private String dateTo;

    /**
     * calendar
     * <p>
     * 
     * 
     */
    @JsonProperty("calendar")
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
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("dateFrom")
    public String getDateFrom() {
        return dateFrom;
    }

    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("dateFrom")
    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("dateTo")
    public String getDateTo() {
        return dateTo;
    }

    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("dateTo")
    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("calendar", calendar).append("id", id).append("path", path).append("dateFrom", dateFrom).append("dateTo", dateTo).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(calendar).append(dateTo).append(path).append(id).append(dateFrom).toHashCode();
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
        return new EqualsBuilder().append(calendar, rhs.calendar).append(dateTo, rhs.dateTo).append(path, rhs.path).append(id, rhs.id).append(dateFrom, rhs.dateFrom).isEquals();
    }

}
