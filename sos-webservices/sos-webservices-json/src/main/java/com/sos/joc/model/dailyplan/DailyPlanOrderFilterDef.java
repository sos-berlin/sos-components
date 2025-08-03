
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
    "workflowTags",
    "orderTags",
    "states",
    "late",
    "expandCycleOrders",
    "auditLog"
})
public class DailyPlanOrderFilterDef
    extends DailyPlanOrderFilterBase
{

    @JsonProperty("submissionHistoryIds")
    private List<Long> submissionHistoryIds = null;
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
    @JsonProperty("states")
    private List<DailyPlanOrderStateText> states = null;
    @JsonProperty("late")
    private Boolean late;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("submissionHistoryIds", submissionHistoryIds).append("workflowTags", workflowTags).append("orderTags", orderTags).append("states", states).append("late", late).append("expandCycleOrders", expandCycleOrders).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(late).append(auditLog).append(submissionHistoryIds).append(workflowTags).append(orderTags).append(states).append(expandCycleOrders).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(late, rhs.late).append(auditLog, rhs.auditLog).append(submissionHistoryIds, rhs.submissionHistoryIds).append(workflowTags, rhs.workflowTags).append(orderTags, rhs.orderTags).append(states, rhs.states).append(expandCycleOrders, rhs.expandCycleOrders).isEquals();
    }

}
