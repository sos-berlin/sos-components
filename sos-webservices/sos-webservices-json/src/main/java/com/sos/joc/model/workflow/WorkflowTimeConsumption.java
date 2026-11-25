
package com.sos.joc.model.workflow;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.controller.model.workflow.WorkflowId;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * workflow
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "path",
    "versionId",
    "statistics"
})
public class WorkflowTimeConsumption
    extends WorkflowId
{

    /**
     * Unit of max, min, average is seconds
     * (Required)
     * 
     */
    @JsonProperty("statistics")
    @JsonPropertyDescription("Unit of max, min, average is seconds")
    private WorkflowTimeStatistics statistics;

    /**
     * Unit of max, min, average is seconds
     * (Required)
     * 
     */
    @JsonProperty("statistics")
    public WorkflowTimeStatistics getStatistics() {
        return statistics;
    }

    /**
     * Unit of max, min, average is seconds
     * (Required)
     * 
     */
    @JsonProperty("statistics")
    public void setStatistics(WorkflowTimeStatistics statistics) {
        this.statistics = statistics;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("statistics", statistics).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof WorkflowTimeConsumption) == false) {
            return false;
        }
        WorkflowTimeConsumption rhs = ((WorkflowTimeConsumption) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).isEquals();
    }

}
