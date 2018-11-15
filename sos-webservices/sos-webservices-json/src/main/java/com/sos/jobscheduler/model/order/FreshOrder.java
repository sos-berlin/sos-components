
package com.sos.jobscheduler.model.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.sos.jobscheduler.model.common.Variables;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * fresh Order
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "workflowPath",
    "scheduledAt",
    "variables"
})
public class FreshOrder {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    @JacksonXmlProperty(localName = "id")
    private String id;
    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("workflowPath")
    @JsonPropertyDescription("absolute path based on live folder of a JobScheduler object.")
    @JacksonXmlProperty(localName = "workflowPath")
    private String workflowPath;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("scheduledAt")
    @JacksonXmlProperty(localName = "scheduledAt")
    private Long scheduledAt;
    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("variables")
    @JsonPropertyDescription("a map for arbitrary key-value pairs")
    @JacksonXmlProperty(localName = "variables")
    private Variables variables;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    @JacksonXmlProperty(localName = "id")
    public String getId() {
        return id;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    @JacksonXmlProperty(localName = "id")
    public void setId(String id) {
        this.id = id;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("workflowPath")
    @JacksonXmlProperty(localName = "workflowPath")
    public String getWorkflowPath() {
        return workflowPath;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("workflowPath")
    @JacksonXmlProperty(localName = "workflowPath")
    public void setWorkflowPath(String workflowPath) {
        this.workflowPath = workflowPath;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("scheduledAt")
    @JacksonXmlProperty(localName = "scheduledAt")
    public Long getScheduledAt() {
        return scheduledAt;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("scheduledAt")
    @JacksonXmlProperty(localName = "scheduledAt")
    public void setScheduledAt(Long scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("variables")
    @JacksonXmlProperty(localName = "variables")
    public Variables getVariables() {
        return variables;
    }

    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("variables")
    @JacksonXmlProperty(localName = "variables")
    public void setVariables(Variables variables) {
        this.variables = variables;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("workflowPath", workflowPath).append("scheduledAt", scheduledAt).append("variables", variables).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(variables).append(id).append(workflowPath).append(scheduledAt).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FreshOrder) == false) {
            return false;
        }
        FreshOrder rhs = ((FreshOrder) other);
        return new EqualsBuilder().append(variables, rhs.variables).append(id, rhs.id).append(workflowPath, rhs.workflowPath).append(scheduledAt, rhs.scheduledAt).isEquals();
    }

}
