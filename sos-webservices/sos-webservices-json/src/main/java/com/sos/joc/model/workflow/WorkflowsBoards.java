
package com.sos.joc.model.workflow;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.controller.model.workflow.WorkflowBoards;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * workflows with boards
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "surveyDate",
    "postingWorkflows",
    "expectingWorkflows",
    "consumingWorkflows"
})
public class WorkflowsBoards {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("deliveryDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date deliveryDate;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("surveyDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date surveyDate;
    @JsonProperty("postingWorkflows")
    private List<WorkflowBoards> postingWorkflows = new ArrayList<WorkflowBoards>();
    @JsonProperty("expectingWorkflows")
    private List<WorkflowBoards> expectingWorkflows = new ArrayList<WorkflowBoards>();
    @JsonProperty("consumingWorkflows")
    private List<WorkflowBoards> consumingWorkflows = new ArrayList<WorkflowBoards>();

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
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
     * 
     */
    @JsonProperty("deliveryDate")
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("surveyDate")
    public Date getSurveyDate() {
        return surveyDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("surveyDate")
    public void setSurveyDate(Date surveyDate) {
        this.surveyDate = surveyDate;
    }

    @JsonProperty("postingWorkflows")
    public List<WorkflowBoards> getPostingWorkflows() {
        return postingWorkflows;
    }

    @JsonProperty("postingWorkflows")
    public void setPostingWorkflows(List<WorkflowBoards> postingWorkflows) {
        this.postingWorkflows = postingWorkflows;
    }

    @JsonProperty("expectingWorkflows")
    public List<WorkflowBoards> getExpectingWorkflows() {
        return expectingWorkflows;
    }

    @JsonProperty("expectingWorkflows")
    public void setExpectingWorkflows(List<WorkflowBoards> expectingWorkflows) {
        this.expectingWorkflows = expectingWorkflows;
    }

    @JsonProperty("consumingWorkflows")
    public List<WorkflowBoards> getConsumingWorkflows() {
        return consumingWorkflows;
    }

    @JsonProperty("consumingWorkflows")
    public void setConsumingWorkflows(List<WorkflowBoards> consumingWorkflows) {
        this.consumingWorkflows = consumingWorkflows;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("surveyDate", surveyDate).append("postingWorkflows", postingWorkflows).append("expectingWorkflows", expectingWorkflows).append("consumingWorkflows", consumingWorkflows).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(expectingWorkflows).append(deliveryDate).append(surveyDate).append(consumingWorkflows).append(postingWorkflows).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof WorkflowsBoards) == false) {
            return false;
        }
        WorkflowsBoards rhs = ((WorkflowsBoards) other);
        return new EqualsBuilder().append(expectingWorkflows, rhs.expectingWorkflows).append(deliveryDate, rhs.deliveryDate).append(surveyDate, rhs.surveyDate).append(consumingWorkflows, rhs.consumingWorkflows).append(postingWorkflows, rhs.postingWorkflows).isEquals();
    }

}
