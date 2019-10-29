
package com.sos.jobscheduler.model.deploy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sos.jobscheduler.model.common.ClassHelper;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * abstract delete object
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE"
})
@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME, 
		include = JsonTypeInfo.As.PROPERTY, 
		property = "TYPE",
		visible = true)
@JsonSubTypes({ 
	@JsonSubTypes.Type(value = com.sos.jobscheduler.model.workflow.DeleteWorkflow.class, name = "WorkflowPath"),
	@JsonSubTypes.Type(value = com.sos.jobscheduler.model.agent.DeleteAgentRef.class, name = "AgentRefPath")})
public abstract class DeleteObject
    extends ClassHelper
{

    /**
     * deleteType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    @JsonIgnore
    private DeleteType tYPE;

    /**
     * No args constructor for use in serialization
     * 
     */
    public DeleteObject() {
    }

    /**
     * 
     * @param tYPE
     */
    public DeleteObject(DeleteType tYPE) {
        super();
        this.tYPE = tYPE;
    }

    /**
     * deleteType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    @JsonIgnore
    public DeleteType getTYPE() {
        return tYPE;
    }

    /**
     * deleteType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    public void setTYPE(DeleteType tYPE) {
        this.tYPE = tYPE;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("tYPE", tYPE).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(tYPE).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DeleteObject) == false) {
            return false;
        }
        DeleteObject rhs = ((DeleteObject) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(tYPE, rhs.tYPE).isEquals();
    }

}
