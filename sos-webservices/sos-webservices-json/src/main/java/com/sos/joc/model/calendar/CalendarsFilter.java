
package com.sos.joc.model.calendar;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.calendar.CalendarType;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.common.Folder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * calendars filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "calendarIds",
    "calendarPaths",
    "compact",
    "type",
    "regex",
    "folders",
    "auditLog"
})
public class CalendarsFilter {

    @JsonProperty("calendarIds")
    private List<Long> calendarIds = null;
    @JsonProperty("calendarPaths")
    private List<String> calendarPaths = null;
    /**
     * compact parameter
     * <p>
     * controls if the object's data is compact or detailed
     * 
     */
    @JsonProperty("compact")
    @JsonPropertyDescription("controls if the object's data is compact or detailed")
    private Boolean compact = false;
    /**
     * calendar type
     * <p>
     * 
     * 
     */
    @JsonProperty("type")
    private CalendarType type;
    /**
     * filter with regex
     * <p>
     * regular expression to filter JobScheduler objects by matching the path
     * 
     */
    @JsonProperty("regex")
    @JsonPropertyDescription("regular expression to filter JobScheduler objects by matching the path")
    private String regex;
    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    private List<Folder> folders = null;
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    @JsonProperty("calendarIds")
    public List<Long> getCalendarIds() {
        return calendarIds;
    }

    @JsonProperty("calendarIds")
    public void setCalendarIds(List<Long> calendarIds) {
        this.calendarIds = calendarIds;
    }

    @JsonProperty("calendarPaths")
    public List<String> getCalendarPaths() {
        return calendarPaths;
    }

    @JsonProperty("calendarPaths")
    public void setCalendarPaths(List<String> calendarPaths) {
        this.calendarPaths = calendarPaths;
    }

    /**
     * compact parameter
     * <p>
     * controls if the object's data is compact or detailed
     * 
     */
    @JsonProperty("compact")
    public Boolean getCompact() {
        return compact;
    }

    /**
     * compact parameter
     * <p>
     * controls if the object's data is compact or detailed
     * 
     */
    @JsonProperty("compact")
    public void setCompact(Boolean compact) {
        this.compact = compact;
    }

    /**
     * calendar type
     * <p>
     * 
     * 
     */
    @JsonProperty("type")
    public CalendarType getType() {
        return type;
    }

    /**
     * calendar type
     * <p>
     * 
     * 
     */
    @JsonProperty("type")
    public void setType(CalendarType type) {
        this.type = type;
    }

    /**
     * filter with regex
     * <p>
     * regular expression to filter JobScheduler objects by matching the path
     * 
     */
    @JsonProperty("regex")
    public String getRegex() {
        return regex;
    }

    /**
     * filter with regex
     * <p>
     * regular expression to filter JobScheduler objects by matching the path
     * 
     */
    @JsonProperty("regex")
    public void setRegex(String regex) {
        this.regex = regex;
    }

    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    public List<Folder> getFolders() {
        return folders;
    }

    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    public void setFolders(List<Folder> folders) {
        this.folders = folders;
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
        return new ToStringBuilder(this).append("calendarIds", calendarIds).append("calendarPaths", calendarPaths).append("compact", compact).append("type", type).append("regex", regex).append("folders", folders).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(calendarIds).append(regex).append(folders).append(compact).append(auditLog).append(calendarPaths).append(type).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CalendarsFilter) == false) {
            return false;
        }
        CalendarsFilter rhs = ((CalendarsFilter) other);
        return new EqualsBuilder().append(calendarIds, rhs.calendarIds).append(regex, rhs.regex).append(folders, rhs.folders).append(compact, rhs.compact).append(auditLog, rhs.auditLog).append(calendarPaths, rhs.calendarPaths).append(type, rhs.type).isEquals();
    }

}
