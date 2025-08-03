
package com.sos.joc.model.dailyplan;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.Folder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Daily Plan Order Filter Definition
 * <p>
 * Define the filter to get orders from the daily plan
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "scheduleFolders",
    "workflowFolders",
    "schedulePaths",
    "workflowPaths",
    "orderIds",
    "controllerIds",
    "dailyPlanDateFrom",
    "dailyPlanDateTo"
})
public class DailyPlanOrderFilterBase {

    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("scheduleFolders")
    private List<Folder> scheduleFolders = null;
    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("workflowFolders")
    private List<Folder> workflowFolders = null;
    @JsonProperty("schedulePaths")
    private List<String> schedulePaths = null;
    @JsonProperty("workflowPaths")
    private List<String> workflowPaths = null;
    @JsonProperty("orderIds")
    private List<String> orderIds = null;
    @JsonProperty("controllerIds")
    private List<String> controllerIds = null;
    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("dailyPlanDateFrom")
    @JsonPropertyDescription("ISO date YYYY-MM-DD")
    private String dailyPlanDateFrom;
    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("dailyPlanDateTo")
    @JsonPropertyDescription("ISO date YYYY-MM-DD")
    private String dailyPlanDateTo;

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

    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("dailyPlanDateFrom")
    public String getDailyPlanDateFrom() {
        return dailyPlanDateFrom;
    }

    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("dailyPlanDateFrom")
    public void setDailyPlanDateFrom(String dailyPlanDateFrom) {
        this.dailyPlanDateFrom = dailyPlanDateFrom;
    }

    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("dailyPlanDateTo")
    public String getDailyPlanDateTo() {
        return dailyPlanDateTo;
    }

    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("dailyPlanDateTo")
    public void setDailyPlanDateTo(String dailyPlanDateTo) {
        this.dailyPlanDateTo = dailyPlanDateTo;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("scheduleFolders", scheduleFolders).append("workflowFolders", workflowFolders).append("schedulePaths", schedulePaths).append("workflowPaths", workflowPaths).append("orderIds", orderIds).append("controllerIds", controllerIds).append("dailyPlanDateFrom", dailyPlanDateFrom).append("dailyPlanDateTo", dailyPlanDateTo).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(schedulePaths).append(dailyPlanDateTo).append(workflowFolders).append(controllerIds).append(workflowPaths).append(orderIds).append(dailyPlanDateFrom).append(scheduleFolders).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DailyPlanOrderFilterBase) == false) {
            return false;
        }
        DailyPlanOrderFilterBase rhs = ((DailyPlanOrderFilterBase) other);
        return new EqualsBuilder().append(schedulePaths, rhs.schedulePaths).append(dailyPlanDateTo, rhs.dailyPlanDateTo).append(workflowFolders, rhs.workflowFolders).append(controllerIds, rhs.controllerIds).append(workflowPaths, rhs.workflowPaths).append(orderIds, rhs.orderIds).append(dailyPlanDateFrom, rhs.dailyPlanDateFrom).append(scheduleFolders, rhs.scheduleFolders).isEquals();
    }

}
