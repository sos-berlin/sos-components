
package com.sos.joc.model.schedule;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.sos.joc.model.common.Folder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * schedulesFilter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobschedulerId",
    "schedules",
    "regex",
    "folders",
    "states"
})
public class SchedulesFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    private String jobschedulerId;
    @JsonProperty("schedules")
    @JacksonXmlProperty(localName = "schedule")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "schedules")
    private List<SchedulePath> schedules = new ArrayList<SchedulePath>();
    /**
     * filter with regex
     * <p>
     * regular expression to filter JobScheduler objects by matching the path
     * 
     */
    @JsonProperty("regex")
    @JsonPropertyDescription("regular expression to filter JobScheduler objects by matching the path")
    @JacksonXmlProperty(localName = "regex")
    private String regex;
    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    @JacksonXmlProperty(localName = "folder")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "folders")
    private List<Folder> folders = new ArrayList<Folder>();
    @JsonProperty("states")
    @JacksonXmlProperty(localName = "state")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "states")
    private List<ScheduleStateText> states = new ArrayList<ScheduleStateText>();

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

    @JsonProperty("schedules")
    @JacksonXmlProperty(localName = "schedule")
    public List<SchedulePath> getSchedules() {
        return schedules;
    }

    @JsonProperty("schedules")
    @JacksonXmlProperty(localName = "schedule")
    public void setSchedules(List<SchedulePath> schedules) {
        this.schedules = schedules;
    }

    /**
     * filter with regex
     * <p>
     * regular expression to filter JobScheduler objects by matching the path
     * 
     */
    @JsonProperty("regex")
    @JacksonXmlProperty(localName = "regex")
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
    @JacksonXmlProperty(localName = "regex")
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
    @JacksonXmlProperty(localName = "folder")
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
    @JacksonXmlProperty(localName = "folder")
    public void setFolders(List<Folder> folders) {
        this.folders = folders;
    }

    @JsonProperty("states")
    @JacksonXmlProperty(localName = "state")
    public List<ScheduleStateText> getStates() {
        return states;
    }

    @JsonProperty("states")
    @JacksonXmlProperty(localName = "state")
    public void setStates(List<ScheduleStateText> states) {
        this.states = states;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("schedules", schedules).append("regex", regex).append("folders", folders).append("states", states).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(regex).append(folders).append(jobschedulerId).append(schedules).append(states).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SchedulesFilter) == false) {
            return false;
        }
        SchedulesFilter rhs = ((SchedulesFilter) other);
        return new EqualsBuilder().append(regex, rhs.regex).append(folders, rhs.folders).append(jobschedulerId, rhs.jobschedulerId).append(schedules, rhs.schedules).append(states, rhs.states).isEquals();
    }

}
