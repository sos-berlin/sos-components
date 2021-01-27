
package com.sos.webservices.order.initiator.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.Folder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Daily Plan  Order Filter Definition
 * <p>
 * Define the selector to get schedules
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerIds",
    "scheduleNames",
    "workflowNames",
    "schedulePaths",
    "workflowPaths",
    "folders"
})
public class SchedulesSelector {

    @JsonProperty("controllerIds")
    private List<String> controllerIds = null;
    @JsonProperty("scheduleNames")
    private List<String> scheduleNames = null;
    @JsonProperty("workflowNames")
    private List<String> workflowNames = null;
    @JsonProperty("schedulePaths")
    private List<String> schedulePaths = null;
    @JsonProperty("workflowPaths")
    private List<String> workflowPaths = null;
    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    private List<Folder> folders = null;

    @JsonProperty("controllerIds")
    public List<String> getControllerIds() {
        return controllerIds;
    }

    @JsonProperty("controllerIds")
    public void setControllerIds(List<String> controllerIds) {
        this.controllerIds = controllerIds;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerIds", controllerIds).append("scheduleNames", scheduleNames).append("workflowNames", workflowNames).append("schedulePaths", schedulePaths).append("workflowPaths", workflowPaths).append("folders", folders).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(workflowNames).append(schedulePaths).append(folders).append(controllerIds).append(scheduleNames).append(workflowPaths).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SchedulesSelector) == false) {
            return false;
        }
        SchedulesSelector rhs = ((SchedulesSelector) other);
        return new EqualsBuilder().append(workflowNames, rhs.workflowNames).append(schedulePaths, rhs.schedulePaths).append(folders, rhs.folders).append(controllerIds, rhs.controllerIds).append(scheduleNames, rhs.scheduleNames).append(workflowPaths, rhs.workflowPaths).isEquals();
    }

}
