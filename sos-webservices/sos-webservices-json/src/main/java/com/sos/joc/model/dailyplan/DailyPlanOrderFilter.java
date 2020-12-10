
package com.sos.joc.model.dailyplan;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.Folder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Daily Plan  Order Filter
 * <p>
 * To get orders from the daily plan
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "overwrite",
    "withSubmit",
    "dailyPlanSubmissionHistoryIds",
    "folders",
    "schedulePaths",
    "workflowPaths",
    "orderIds",
    "controllerIds",
    "dailyPlanDate",
    "schedulesFolder"
})
public class DailyPlanOrderFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * overwrite parameter
     * <p>
     * controls if the order should be overwritten
     * 
     */
    @JsonProperty("overwrite")
    @JsonPropertyDescription("controls if the order should be overwritten")
    private Boolean overwrite = false;
    /**
     * withSubmit parameter
     * <p>
     * controls if the order should be submitted to the controller
     * 
     */
    @JsonProperty("withSubmit")
    @JsonPropertyDescription("controls if the order should be submitted to the controller")
    private Boolean withSubmit = true;
    @JsonProperty("dailyPlanSubmissionHistoryIds")
    private List<Long> dailyPlanSubmissionHistoryIds = null;
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
    @JsonProperty("orderIds")
    private List<String> orderIds = null;
    @JsonProperty("controllerIds")
    private List<String> controllerIds = null;
    @JsonProperty("dailyPlanDate")
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

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * overwrite parameter
     * <p>
     * controls if the order should be overwritten
     * 
     */
    @JsonProperty("overwrite")
    public Boolean getOverwrite() {
        return overwrite;
    }

    /**
     * overwrite parameter
     * <p>
     * controls if the order should be overwritten
     * 
     */
    @JsonProperty("overwrite")
    public void setOverwrite(Boolean overwrite) {
        this.overwrite = overwrite;
    }

    /**
     * withSubmit parameter
     * <p>
     * controls if the order should be submitted to the controller
     * 
     */
    @JsonProperty("withSubmit")
    public Boolean getWithSubmit() {
        return withSubmit;
    }

    /**
     * withSubmit parameter
     * <p>
     * controls if the order should be submitted to the controller
     * 
     */
    @JsonProperty("withSubmit")
    public void setWithSubmit(Boolean withSubmit) {
        this.withSubmit = withSubmit;
    }

    @JsonProperty("dailyPlanSubmissionHistoryIds")
    public List<Long> getDailyPlanSubmissionHistoryIds() {
        return dailyPlanSubmissionHistoryIds;
    }

    @JsonProperty("dailyPlanSubmissionHistoryIds")
    public void setDailyPlanSubmissionHistoryIds(List<Long> dailyPlanSubmissionHistoryIds) {
        this.dailyPlanSubmissionHistoryIds = dailyPlanSubmissionHistoryIds;
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

    @JsonProperty("dailyPlanDate")
    public String getDailyPlanDate() {
        return dailyPlanDate;
    }

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
        return new ToStringBuilder(this).append("controllerId", controllerId).append("overwrite", overwrite).append("withSubmit", withSubmit).append("dailyPlanSubmissionHistoryIds", dailyPlanSubmissionHistoryIds).append("folders", folders).append("schedulePaths", schedulePaths).append("workflowPaths", workflowPaths).append("orderIds", orderIds).append("controllerIds", controllerIds).append("dailyPlanDate", dailyPlanDate).append("schedulesFolder", schedulesFolder).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(schedulePaths).append(folders).append(dailyPlanDate).append(controllerId).append(controllerIds).append(withSubmit).append(workflowPaths).append(dailyPlanSubmissionHistoryIds).append(orderIds).append(schedulesFolder).append(overwrite).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DailyPlanOrderFilter) == false) {
            return false;
        }
        DailyPlanOrderFilter rhs = ((DailyPlanOrderFilter) other);
        return new EqualsBuilder().append(schedulePaths, rhs.schedulePaths).append(folders, rhs.folders).append(dailyPlanDate, rhs.dailyPlanDate).append(controllerId, rhs.controllerId).append(controllerIds, rhs.controllerIds).append(withSubmit, rhs.withSubmit).append(workflowPaths, rhs.workflowPaths).append(dailyPlanSubmissionHistoryIds, rhs.dailyPlanSubmissionHistoryIds).append(orderIds, rhs.orderIds).append(schedulesFolder, rhs.schedulesFolder).append(overwrite, rhs.overwrite).isEquals();
    }

}
