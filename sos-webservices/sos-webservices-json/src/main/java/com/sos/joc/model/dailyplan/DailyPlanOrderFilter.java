
package com.sos.joc.model.dailyplan;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
    "submissionHistoryId",
    "schedules",
    "workflowPaths",
    "orderIds",
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
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("submissionHistoryId")
    private Long submissionHistoryId;
    @JsonProperty("schedules")
    private List<String> schedules = null;
    @JsonProperty("workflowPaths")
    private List<String> workflowPaths = null;
    @JsonProperty("orderIds")
    private List<String> orderIds = null;
    @JsonProperty("dailyPlanDate")
    private String dailyPlanDate;
    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("schedulesFolder")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
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

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("submissionHistoryId")
    public Long getSubmissionHistoryId() {
        return submissionHistoryId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("submissionHistoryId")
    public void setSubmissionHistoryId(Long submissionHistoryId) {
        this.submissionHistoryId = submissionHistoryId;
    }

    @JsonProperty("schedules")
    public List<String> getSchedules() {
        return schedules;
    }

    @JsonProperty("schedules")
    public void setSchedules(List<String> schedules) {
        this.schedules = schedules;
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
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("schedulesFolder")
    public String getSchedulesFolder() {
        return schedulesFolder;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("schedulesFolder")
    public void setSchedulesFolder(String schedulesFolder) {
        this.schedulesFolder = schedulesFolder;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("overwrite", overwrite).append("withSubmit", withSubmit).append("submissionHistoryId", submissionHistoryId).append("schedules", schedules).append("workflowPaths", workflowPaths).append("orderIds", orderIds).append("dailyPlanDate", dailyPlanDate).append("schedulesFolder", schedulesFolder).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(dailyPlanDate).append(controllerId).append(submissionHistoryId).append(schedules).append(withSubmit).append(workflowPaths).append(orderIds).append(schedulesFolder).append(overwrite).toHashCode();
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
        return new EqualsBuilder().append(dailyPlanDate, rhs.dailyPlanDate).append(controllerId, rhs.controllerId).append(submissionHistoryId, rhs.submissionHistoryId).append(schedules, rhs.schedules).append(withSubmit, rhs.withSubmit).append(workflowPaths, rhs.workflowPaths).append(orderIds, rhs.orderIds).append(schedulesFolder, rhs.schedulesFolder).append(overwrite, rhs.overwrite).isEquals();
    }

}
