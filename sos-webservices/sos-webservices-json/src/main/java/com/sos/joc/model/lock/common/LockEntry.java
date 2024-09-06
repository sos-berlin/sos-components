
package com.sos.joc.model.lock.common;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.controller.model.lock.Lock;
import com.sos.joc.model.common.WorkflowTags;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * lock entry
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "lock",
    "acquiredLockCount",
    "ordersHoldingLocksCount",
    "ordersWaitingForLocksCount",
    "workflows",
    "workflowTagsPerWorkflow"
})
public class LockEntry {

    /**
     * lock
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("lock")
    private Lock lock;
    /**
     * non negative integer
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("acquiredLockCount")
    private Integer acquiredLockCount;
    /**
     * non negative integer
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("ordersHoldingLocksCount")
    private Integer ordersHoldingLocksCount;
    /**
     * non negative integer
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("ordersWaitingForLocksCount")
    private Integer ordersWaitingForLocksCount;
    @JsonProperty("workflows")
    private List<LockWorkflow> workflows = new ArrayList<LockWorkflow>();
    /**
     * workflow tags
     * <p>
     * a map of workflowName -> tags-array
     * 
     */
    @JsonProperty("workflowTagsPerWorkflow")
    @JsonPropertyDescription("a map of workflowName -> tags-array")
    private WorkflowTags workflowTagsPerWorkflow;

    /**
     * lock
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("lock")
    public Lock getLock() {
        return lock;
    }

    /**
     * lock
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("lock")
    public void setLock(Lock lock) {
        this.lock = lock;
    }

    /**
     * non negative integer
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("acquiredLockCount")
    public Integer getAcquiredLockCount() {
        return acquiredLockCount;
    }

    /**
     * non negative integer
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("acquiredLockCount")
    public void setAcquiredLockCount(Integer acquiredLockCount) {
        this.acquiredLockCount = acquiredLockCount;
    }

    /**
     * non negative integer
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("ordersHoldingLocksCount")
    public Integer getOrdersHoldingLocksCount() {
        return ordersHoldingLocksCount;
    }

    /**
     * non negative integer
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("ordersHoldingLocksCount")
    public void setOrdersHoldingLocksCount(Integer ordersHoldingLocksCount) {
        this.ordersHoldingLocksCount = ordersHoldingLocksCount;
    }

    /**
     * non negative integer
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("ordersWaitingForLocksCount")
    public Integer getOrdersWaitingForLocksCount() {
        return ordersWaitingForLocksCount;
    }

    /**
     * non negative integer
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("ordersWaitingForLocksCount")
    public void setOrdersWaitingForLocksCount(Integer ordersWaitingForLocksCount) {
        this.ordersWaitingForLocksCount = ordersWaitingForLocksCount;
    }

    @JsonProperty("workflows")
    public List<LockWorkflow> getWorkflows() {
        return workflows;
    }

    @JsonProperty("workflows")
    public void setWorkflows(List<LockWorkflow> workflows) {
        this.workflows = workflows;
    }

    /**
     * workflow tags
     * <p>
     * a map of workflowName -> tags-array
     * 
     */
    @JsonProperty("workflowTagsPerWorkflow")
    public WorkflowTags getWorkflowTagsPerWorkflow() {
        return workflowTagsPerWorkflow;
    }

    /**
     * workflow tags
     * <p>
     * a map of workflowName -> tags-array
     * 
     */
    @JsonProperty("workflowTagsPerWorkflow")
    public void setWorkflowTagsPerWorkflow(WorkflowTags workflowTagsPerWorkflow) {
        this.workflowTagsPerWorkflow = workflowTagsPerWorkflow;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("lock", lock).append("acquiredLockCount", acquiredLockCount).append("ordersHoldingLocksCount", ordersHoldingLocksCount).append("ordersWaitingForLocksCount", ordersWaitingForLocksCount).append("workflows", workflows).append("workflowTagsPerWorkflow", workflowTagsPerWorkflow).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(ordersWaitingForLocksCount).append(lock).append(workflowTagsPerWorkflow).append(workflows).append(acquiredLockCount).append(ordersHoldingLocksCount).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof LockEntry) == false) {
            return false;
        }
        LockEntry rhs = ((LockEntry) other);
        return new EqualsBuilder().append(ordersWaitingForLocksCount, rhs.ordersWaitingForLocksCount).append(lock, rhs.lock).append(workflowTagsPerWorkflow, rhs.workflowTagsPerWorkflow).append(workflows, rhs.workflows).append(acquiredLockCount, rhs.acquiredLockCount).append(ordersHoldingLocksCount, rhs.ordersHoldingLocksCount).isEquals();
    }

}
