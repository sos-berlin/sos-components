
package com.sos.joc.model.dailyplan.projections;

import java.util.ArrayList;
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
 * daily plan projections request
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "date",
    "controllerIds",
    "schedulePaths",
    "scheduleFolders",
    "workflowPaths",
    "workflowFolders",
    "withoutStartTime"
})
public class ProjectionsDayRequest {

    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * (Required)
     * 
     */
    @JsonProperty("date")
    @JsonPropertyDescription("ISO date YYYY-MM-DD")
    private String date;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("controllerIds")
    private List<String> controllerIds = new ArrayList<String>();
    @JsonProperty("schedulePaths")
    private List<String> schedulePaths = new ArrayList<String>();
    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("scheduleFolders")
    private List<Folder> scheduleFolders = new ArrayList<Folder>();
    @JsonProperty("workflowPaths")
    private List<String> workflowPaths = new ArrayList<String>();
    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("workflowFolders")
    private List<Folder> workflowFolders = new ArrayList<Folder>();
    @JsonProperty("withoutStartTime")
    private Boolean withoutStartTime = false;

    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * (Required)
     * 
     */
    @JsonProperty("date")
    public String getDate() {
        return date;
    }

    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * (Required)
     * 
     */
    @JsonProperty("date")
    public void setDate(String date) {
        this.date = date;
    }

    @JsonProperty("controllerIds")
    public List<String> getControllerIds() {
        return controllerIds;
    }

    @JsonProperty("controllerIds")
    public void setControllerIds(List<String> controllerIds) {
        this.controllerIds = controllerIds;
    }

    @JsonProperty("schedulePaths")
    public List<String> getSchedulePaths() {
        return schedulePaths;
    }

    @JsonProperty("schedulePaths")
    public void setSchedulePaths(List<String> schedulePaths) {
        this.schedulePaths = schedulePaths;
    }

    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("scheduleFolders")
    public List<Folder> getScheduleFolders() {
        return scheduleFolders;
    }

    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("scheduleFolders")
    public void setScheduleFolders(List<Folder> scheduleFolders) {
        this.scheduleFolders = scheduleFolders;
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
    @JsonProperty("workflowFolders")
    public List<Folder> getWorkflowFolders() {
        return workflowFolders;
    }

    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("workflowFolders")
    public void setWorkflowFolders(List<Folder> workflowFolders) {
        this.workflowFolders = workflowFolders;
    }
    
    @JsonProperty("withoutStartTime")
    public Boolean getWithoutStartTime() {
        return withoutStartTime;
    }

    @JsonProperty("withoutStartTime")
    public void setWithoutStartTime(Boolean withoutStartTime) {
        this.withoutStartTime = withoutStartTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("date", date).append("controllerIds", controllerIds).append("schedulePaths", schedulePaths).append("scheduleFolders", scheduleFolders).append("workflowPaths", workflowPaths).append("workflowFolders", workflowFolders).append("withoutStartTime", withoutStartTime).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(date).append(schedulePaths).append(workflowFolders).append(controllerIds).append(workflowPaths).append(scheduleFolders).append(withoutStartTime).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ProjectionsDayRequest) == false) {
            return false;
        }
        ProjectionsDayRequest rhs = ((ProjectionsDayRequest) other);
        return new EqualsBuilder().append(date, rhs.date).append(schedulePaths, rhs.schedulePaths).append(workflowFolders, rhs.workflowFolders).append(controllerIds, rhs.controllerIds).append(workflowPaths, rhs.workflowPaths).append(scheduleFolders, rhs.scheduleFolders).append(withoutStartTime, rhs.withoutStartTime).isEquals();
    }

}
