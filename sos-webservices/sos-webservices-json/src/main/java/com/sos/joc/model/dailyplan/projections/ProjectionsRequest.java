
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
    "dateFrom",
    "dateTo",
    "controllerIds",
    "schedulePaths",
    "scheduleFolders",
    "workflowPaths",
    "workflowFolders",
    "withoutStartTime"
})
public class ProjectionsRequest {

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
        return new ToStringBuilder(this).append("dateFrom", dateFrom).append("dateTo", dateTo).append("controllerIds", controllerIds).append("schedulePaths", schedulePaths).append("scheduleFolders", scheduleFolders).append("workflowPaths", workflowPaths).append("workflowFolders", workflowFolders).append("withoutStartTime", withoutStartTime).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(schedulePaths).append(workflowFolders).append(controllerIds).append(dateTo).append(workflowPaths).append(dateFrom).append(scheduleFolders).append(withoutStartTime).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ProjectionsRequest) == false) {
            return false;
        }
        ProjectionsRequest rhs = ((ProjectionsRequest) other);
        return new EqualsBuilder().append(schedulePaths, rhs.schedulePaths).append(workflowFolders, rhs.workflowFolders).append(controllerIds, rhs.controllerIds).append(dateTo, rhs.dateTo).append(workflowPaths, rhs.workflowPaths).append(dateFrom, rhs.dateFrom).append(scheduleFolders, rhs.scheduleFolders).append(withoutStartTime, rhs.withoutStartTime).isEquals();
    }

}
