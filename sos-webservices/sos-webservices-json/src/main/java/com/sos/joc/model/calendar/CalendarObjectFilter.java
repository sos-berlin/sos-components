
package com.sos.joc.model.calendar;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * calendarObjectFilter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "calendar",
    "auditLog"
})
public class CalendarObjectFilter {

    /**
     * calendar
     * <p>
     * 
     * 
     */
    @JsonProperty("calendar")
    private Calendar calendar;
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

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
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    public AuditParams getAuditLog() {
        return auditLog;
    }

    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    public void setAuditLog(AuditParams auditLog) {
        this.auditLog = auditLog;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("calendar", calendar).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(calendar).append(auditLog).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CalendarObjectFilter) == false) {
            return false;
        }
        CalendarObjectFilter rhs = ((CalendarObjectFilter) other);
        return new EqualsBuilder().append(calendar, rhs.calendar).append(auditLog, rhs.auditLog).isEquals();
    }

}
