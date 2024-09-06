
package com.sos.joc.model.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.WorkflowTags;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * history collection of tasks
 * <p>
 * one item per started task
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "history",
    "workflowTagsPerWorkflow"
})
public class TaskHistory {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date deliveryDate;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("history")
    private List<TaskHistoryItem> history = new ArrayList<TaskHistoryItem>();
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
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    public Date getDeliveryDate() {
        return deliveryDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("history")
    public List<TaskHistoryItem> getHistory() {
        return history;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("history")
    public void setHistory(List<TaskHistoryItem> history) {
        this.history = history;
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
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("history", history).append("workflowTagsPerWorkflow", workflowTagsPerWorkflow).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(workflowTagsPerWorkflow).append(history).append(deliveryDate).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof TaskHistory) == false) {
            return false;
        }
        TaskHistory rhs = ((TaskHistory) other);
        return new EqualsBuilder().append(workflowTagsPerWorkflow, rhs.workflowTagsPerWorkflow).append(history, rhs.history).append(deliveryDate, rhs.deliveryDate).isEquals();
    }

}
