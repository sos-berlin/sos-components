
package com.sos.sign.model.workflow;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "id",
    "workflow"
})
public class Branch {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    private String id;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflow")
    private BranchWorkflow workflow;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Branch() {
    }

    /**
     * 
     * @param workflow
     * @param id
     */
    public Branch(String id, BranchWorkflow workflow) {
        super();
        this.id = id;
        this.workflow = workflow;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflow")
    public BranchWorkflow getWorkflow() {
        return workflow;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflow")
    public void setWorkflow(BranchWorkflow workflow) {
        this.workflow = workflow;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("workflow", workflow).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(workflow).append(id).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Branch) == false) {
            return false;
        }
        Branch rhs = ((Branch) other);
        return new EqualsBuilder().append(workflow, rhs.workflow).append(id, rhs.id).isEquals();
    }

}
