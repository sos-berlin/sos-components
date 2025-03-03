
package com.sos.controller.model.board;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.WorkflowTags;
import com.sos.joc.model.order.OrderV;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * notice
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "key",
    "endOfLife",
    "expectingOrders",
    "workflowTagsPerWorkflow",
    "state"
})
public class Notice {

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    private String id;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("key")
    private String key;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("endOfLife")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date endOfLife;
    @JsonProperty("expectingOrders")
    private List<OrderV> expectingOrders = null;
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
     * NoticeState
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    private NoticeState state;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Notice() {
    }

    /**
     * 
     * @param workflowTagsPerWorkflow
     * @param id
     * @param state
     * @param key
     * @param endOfLife
     * @param expectingOrders
     */
    public Notice(String id, String key, Date endOfLife, List<OrderV> expectingOrders, WorkflowTags workflowTagsPerWorkflow, NoticeState state) {
        super();
        this.id = id;
        this.key = key;
        this.endOfLife = endOfLife;
        this.expectingOrders = expectingOrders;
        this.workflowTagsPerWorkflow = workflowTagsPerWorkflow;
        this.state = state;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("key")
    public String getKey() {
        return key;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("key")
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("endOfLife")
    public Date getEndOfLife() {
        return endOfLife;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("endOfLife")
    public void setEndOfLife(Date endOfLife) {
        this.endOfLife = endOfLife;
    }

    @JsonProperty("expectingOrders")
    public List<OrderV> getExpectingOrders() {
        return expectingOrders;
    }

    @JsonProperty("expectingOrders")
    public void setExpectingOrders(List<OrderV> expectingOrders) {
        this.expectingOrders = expectingOrders;
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

    /**
     * NoticeState
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public NoticeState getState() {
        return state;
    }

    /**
     * NoticeState
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public void setState(NoticeState state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("key", key).append("endOfLife", endOfLife).append("expectingOrders", expectingOrders).append("workflowTagsPerWorkflow", workflowTagsPerWorkflow).append("state", state).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Notice) == false) {
            return false;
        }
        Notice rhs = ((Notice) other);
        return new EqualsBuilder().append(id, rhs.id).isEquals();
    }

}
