
package com.sos.joc.model.dailyplan;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.order.OrderStateText;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Daily Plan  Order Filter Definition
 * <p>
 * Define the filter To get orders from the daily plan
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "submissionHistoryIds",
    "folders",
    "schedulePaths",
    "workflowPaths",
    "scheduleNames",
    "workflowNames",
    "orderIds",
    "controllerIds",
    "states",
    "late",
    "dailyPlanDate",
    "schedulesFolder"
})
public class DailyPlanOrderFilterDef {

    @JsonProperty("submissionHistoryIds")
    private List<Long> submissionHistoryIds = null;
    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    private List<Folder> folders = null;
    @JsonProperty("schedulePaths")
    private List<String> schedulePaths = null;
    @JsonProperty("workflowPaths")
    private List<String> workflowPaths = null;
    @JsonProperty("scheduleNames")
    private List<String> scheduleNames = null;
    @JsonProperty("workflowNames")
    private List<String> workflowNames = null;
    @JsonProperty("orderIds")
    private List<String> orderIds = null;
    @JsonProperty("controllerIds")
    private List<String> controllerIds = null;
    @JsonProperty("states")
    private List<OrderStateText> states = null;
    @JsonProperty("late")
    private Boolean late;
    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("dailyPlanDate")
    @JsonPropertyDescription("ISO date YYYY-MM-DD")
    private String dailyPlanDate;
    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("schedulesFolder")
    @JsonPropertyDescription("absolute path of an object.")
    private String schedulesFolder;

    @JsonProperty("submissionHistoryIds")
    public List<Long> getSubmissionHistoryIds() {
        return submissionHistoryIds;
    }

    @JsonProperty("submissionHistoryIds")
    public void setSubmissionHistoryIds(List<Long> submissionHistoryIds) {
        this.submissionHistoryIds = submissionHistoryIds;
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

    @JsonProperty("schedulePaths")
    public List<String> getSchedulePaths() {
        return schedulePaths;
    }

    @JsonProperty("schedulePaths")
    public void setSchedulePaths(List<String> schedulePaths) {
        this.schedulePaths = schedulePaths;
    }

    @JsonProperty("workflowPaths")
    public List<String> getWorkflowPaths() {
        return workflowPaths;
    }

    @JsonProperty("workflowPaths")
    public void setWorkflowPaths(List<String> workflowPaths) {
        this.workflowPaths = workflowPaths;
    }

    @JsonProperty("scheduleNames")
    public List<String> getScheduleNames() {
        return scheduleNames;
    }

    @JsonProperty("scheduleNames")
    public void setScheduleNames(List<String> scheduleNames) {
        this.scheduleNames = scheduleNames;
    }

    @JsonProperty("workflowNames")
    public List<String> getWorkflowNames() {
        return workflowNames;
    }

    @JsonProperty("workflowNames")
    public void setWorkflowNames(List<String> workflowNames) {
        this.workflowNames = workflowNames;
    }

    @JsonProperty("orderIds")
    public List<String> getOrderIds() {
        return orderIds;
    }

    @JsonProperty("orderIds")
    public void setOrderIds(List<String> orderIds) {
        this.orderIds = orderIds;
    }

    @JsonProperty("controllerIds")
    public List<String> getControllerIds() {
        return controllerIds;
    }

    @JsonProperty("controllerIds")
    public void setControllerIds(List<String> controllerIds) {
        this.controllerIds = controllerIds;
    }

    @JsonProperty("states")
    public List<OrderStateText> getStates() {
        return states;
    }

    @JsonProperty("states")
    public void setStates(List<OrderStateText> states) {
        this.states = states;
    }

    @JsonProperty("late")
    public Boolean getLate() {
        return late;
    }

    @JsonProperty("late")
    public void setLate(Boolean late) {
        this.late = late;
    }

    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("dailyPlanDate")
    public String getDailyPlanDate() {
        return dailyPlanDate;
    }

    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("dailyPlanDate")
    public void setDailyPlanDate(String dailyPlanDate) {
        this.dailyPlanDate = dailyPlanDate;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("schedulesFolder")
    public String getSchedulesFolder() {
        return schedulesFolder;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("schedulesFolder")
    public void setSchedulesFolder(String schedulesFolder) {
        this.schedulesFolder = schedulesFolder;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("submissionHistoryIds", submissionHistoryIds).append("folders", folders).append("schedulePaths", schedulePaths).append("workflowPaths", workflowPaths).append("scheduleNames", scheduleNames).append("workflowNames", workflowNames).append("orderIds", orderIds).append("controllerIds", controllerIds).append("states", states).append("late", late).append("dailyPlanDate", dailyPlanDate).append("schedulesFolder", schedulesFolder).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(folders).append(dailyPlanDate).append(states).append(workflowNames).append(schedulePaths).append(late).append(controllerIds).append(scheduleNames).append(submissionHistoryIds).append(workflowPaths).append(orderIds).append(schedulesFolder).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DailyPlanOrderFilterDef) == false) {
            return false;
        }
        DailyPlanOrderFilterDef rhs = ((DailyPlanOrderFilterDef) other);
        return new EqualsBuilder().append(folders, rhs.folders).append(dailyPlanDate, rhs.dailyPlanDate).append(states, rhs.states).append(workflowNames, rhs.workflowNames).append(schedulePaths, rhs.schedulePaths).append(late, rhs.late).append(controllerIds, rhs.controllerIds).append(scheduleNames, rhs.scheduleNames).append(submissionHistoryIds, rhs.submissionHistoryIds).append(workflowPaths, rhs.workflowPaths).append(orderIds, rhs.orderIds).append(schedulesFolder, rhs.schedulesFolder).isEquals();
    }

}
