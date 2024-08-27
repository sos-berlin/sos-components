
package com.sos.joc.model.inventory.references;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.controller.model.board.Board;
import com.sos.controller.model.fileordersource.FileOrderSource;
import com.sos.controller.model.jobresource.JobResource;
import com.sos.controller.model.jobtemplate.JobTemplate;
import com.sos.controller.model.lock.Lock;
import com.sos.controller.model.workflow.Workflow;
import com.sos.inventory.model.calendar.Calendar;
import com.sos.inventory.model.schedule.Schedule;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * references
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "isRenamed",
    "workflows",
    "fileOrderSources",
    "boards",
    "locks",
    "jobResources",
    "jobTemplates",
    "schedules",
    "calendars"
})
public class ResponseItems {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date deliveryDate;
    @JsonProperty("isRenamed")
    private Boolean isRenamed;
    @JsonProperty("workflows")
    private List<Workflow> workflows = new ArrayList<Workflow>();
    @JsonProperty("fileOrderSources")
    private List<FileOrderSource> fileOrderSources = new ArrayList<FileOrderSource>();
    @JsonProperty("boards")
    private List<Board> boards = new ArrayList<Board>();
    @JsonProperty("locks")
    private List<Lock> locks = new ArrayList<Lock>();
    @JsonProperty("jobResources")
    private List<JobResource> jobResources = new ArrayList<JobResource>();
    @JsonProperty("jobTemplates")
    private List<JobTemplate> jobTemplates = new ArrayList<JobTemplate>();
    @JsonProperty("schedules")
    private List<Schedule> schedules = new ArrayList<Schedule>();
    @JsonProperty("calendars")
    private List<Calendar> calendars = new ArrayList<Calendar>();

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    public Date getDeliveryDate() {
        return deliveryDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    @JsonProperty("isRenamed")
    public Boolean getIsRenamed() {
        return isRenamed;
    }

    @JsonProperty("isRenamed")
    public void setIsRenamed(Boolean isRenamed) {
        this.isRenamed = isRenamed;
    }

    @JsonProperty("workflows")
    public List<Workflow> getWorkflows() {
        return workflows;
    }

    @JsonProperty("workflows")
    public void setWorkflows(List<Workflow> workflows) {
        this.workflows = workflows;
    }

    @JsonProperty("fileOrderSources")
    public List<FileOrderSource> getFileOrderSources() {
        return fileOrderSources;
    }

    @JsonProperty("fileOrderSources")
    public void setFileOrderSources(List<FileOrderSource> fileOrderSources) {
        this.fileOrderSources = fileOrderSources;
    }

    @JsonProperty("boards")
    public List<Board> getBoards() {
        return boards;
    }

    @JsonProperty("boards")
    public void setBoards(List<Board> boards) {
        this.boards = boards;
    }

    @JsonProperty("locks")
    public List<Lock> getLocks() {
        return locks;
    }

    @JsonProperty("locks")
    public void setLocks(List<Lock> locks) {
        this.locks = locks;
    }

    @JsonProperty("jobResources")
    public List<JobResource> getJobResources() {
        return jobResources;
    }

    @JsonProperty("jobResources")
    public void setJobResources(List<JobResource> jobResources) {
        this.jobResources = jobResources;
    }

    @JsonProperty("jobTemplates")
    public List<JobTemplate> getJobTemplates() {
        return jobTemplates;
    }

    @JsonProperty("jobTemplates")
    public void setJobTemplates(List<JobTemplate> jobTemplates) {
        this.jobTemplates = jobTemplates;
    }

    @JsonProperty("schedules")
    public List<Schedule> getSchedules() {
        return schedules;
    }

    @JsonProperty("schedules")
    public void setSchedules(List<Schedule> schedules) {
        this.schedules = schedules;
    }

    @JsonProperty("calendars")
    public List<Calendar> getCalendars() {
        return calendars;
    }

    @JsonProperty("calendars")
    public void setCalendars(List<Calendar> calendars) {
        this.calendars = calendars;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("isRenamed", isRenamed).append("workflows", workflows).append("fileOrderSources", fileOrderSources).append("boards", boards).append("locks", locks).append("jobResources", jobResources).append("jobTemplates", jobTemplates).append("schedules", schedules).append("calendars", calendars).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(isRenamed).append(fileOrderSources).append(calendars).append(schedules).append(boards).append(jobTemplates).append(workflows).append(deliveryDate).append(jobResources).append(locks).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ResponseItems) == false) {
            return false;
        }
        ResponseItems rhs = ((ResponseItems) other);
        return new EqualsBuilder().append(isRenamed, rhs.isRenamed).append(fileOrderSources, rhs.fileOrderSources).append(calendars, rhs.calendars).append(schedules, rhs.schedules).append(boards, rhs.boards).append(jobTemplates, rhs.jobTemplates).append(workflows, rhs.workflows).append(deliveryDate, rhs.deliveryDate).append(jobResources, rhs.jobResources).append(locks, rhs.locks).isEquals();
    }

}
