
package com.sos.joc.model.inventory.read;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * read filter for workflows
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "workflowPath"
})
public class RequestWorkflowFilter {

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowPath")
    private String workflowPath;

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowPath")
    public String getWorkflowPath() {
        return workflowPath;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowPath")
    public void setWorkflowPath(String workflowPath) {
        this.workflowPath = workflowPath;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("workflowPath", workflowPath).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(workflowPath).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RequestWorkflowFilter) == false) {
            return false;
        }
        RequestWorkflowFilter rhs = ((RequestWorkflowFilter) other);
        return new EqualsBuilder().append(workflowPath, rhs.workflowPath).isEquals();
    }

}
