
package com.sos.joc.model.dailyplan;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sos.joc.model.audit.AuditParams;
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
    "submissionHistoryIds",
    "scheduleFolders",
    "workflowFolders",
    "schedulePaths",
    "workflowPaths",
    "workflowTags",
    "orderTags",
    "orderIds",
    "controllerIds",
    "states",
    "late",
    "dailyPlanDateFrom",
    "dailyPlanDateTo",
    "expandCycleOrders",
    "auditLog"
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
    /**
     * tags
     * <p>
     * 
     * 
     */
    @JsonProperty("workflowTags")
    @JsonAlias({
        "tags"
    })
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> workflowTags = null;
    /**
     * tags
     * <p>
     * 
     * 
     */
    @JsonProperty("orderTags")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> orderTags = null;
    @JsonProperty("orderIds")
    private List<String> orderIds = null;
    @JsonProperty("controllerIds")
    private List<String> controllerIds = null;
    @JsonProperty("states")
    private List<DailyPlanOrderStateText> states = null;
    @JsonProperty("late")
    private Boolean late;
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
     * for internal use only: controls if the cycle order should be expanded in the answer
     * 
     */
    @JsonProperty("expandCycleOrders")
    @JsonPropertyDescription("for internal use only: controls if the cycle order should be expanded in the answer")
    private Boolean expandCycleOrders = false;
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

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

    /**
     * tags
     * <p>
     * 
     * 
     */
    @JsonProperty("workflowTags")
    public Set<String> getWorkflowTags() {
        return workflowTags;
    }

    /**
     * tags
     * <p>
     * 
     * 
     */
    @JsonProperty("workflowTags")
    public void setWorkflowTags(Set<String> workflowTags) {
        this.workflowTags = workflowTags;
    }

    /**
     * tags
     * <p>
     * 
     * 
     */
    @JsonProperty("orderTags")
    public Set<String> getOrderTags() {
        return orderTags;
    }

    /**
     * tags
     * <p>
     * 
     * 
     */
    @JsonProperty("orderTags")
    public void setOrderTags(Set<String> orderTags) {
        this.orderTags = orderTags;
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
    public List<DailyPlanOrderStateText> getStates() {
        return states;
    }

    @JsonProperty("states")
    public void setStates(List<DailyPlanOrderStateText> states) {
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

    /**
     * for internal use only: controls if the cycle order should be expanded in the answer
     * 
     */
    @JsonProperty("expandCycleOrders")
    public Boolean getExpandCycleOrders() {
        return expandCycleOrders;
    }

    /**
     * for internal use only: controls if the cycle order should be expanded in the answer
     * 
     */
    @JsonProperty("expandCycleOrders")
    public void setExpandCycleOrders(Boolean expandCycleOrders) {
        this.expandCycleOrders = expandCycleOrders;
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
        return new ToStringBuilder(this).append("submissionHistoryIds", submissionHistoryIds).append("scheduleFolders", scheduleFolders).append("workflowFolders", workflowFolders).append("schedulePaths", schedulePaths).append("workflowPaths", workflowPaths).append("workflowTags", workflowTags).append("orderTags", orderTags).append("orderIds", orderIds).append("controllerIds", controllerIds).append("states", states).append("late", late).append("dailyPlanDateFrom", dailyPlanDateFrom).append("dailyPlanDateTo", dailyPlanDateTo).append("expandCycleOrders", expandCycleOrders).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(dailyPlanDateTo).append(auditLog).append(scheduleFolders).append(orderTags).append(states).append(expandCycleOrders).append(schedulePaths).append(late).append(workflowFolders).append(controllerIds).append(submissionHistoryIds).append(workflowPaths).append(workflowTags).append(orderIds).append(dailyPlanDateFrom).toHashCode();
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
        return new EqualsBuilder().append(dailyPlanDateTo, rhs.dailyPlanDateTo).append(auditLog, rhs.auditLog).append(scheduleFolders, rhs.scheduleFolders).append(orderTags, rhs.orderTags).append(states, rhs.states).append(expandCycleOrders, rhs.expandCycleOrders).append(schedulePaths, rhs.schedulePaths).append(late, rhs.late).append(workflowFolders, rhs.workflowFolders).append(controllerIds, rhs.controllerIds).append(submissionHistoryIds, rhs.submissionHistoryIds).append(workflowPaths, rhs.workflowPaths).append(workflowTags, rhs.workflowTags).append(orderIds, rhs.orderIds).append(dailyPlanDateFrom, rhs.dailyPlanDateFrom).isEquals();
    }

}
