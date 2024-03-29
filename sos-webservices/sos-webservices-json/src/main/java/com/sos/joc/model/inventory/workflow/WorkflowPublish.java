
package com.sos.joc.model.inventory.workflow;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.publish.ControllerObject;
import com.sos.sign.model.workflow.Workflow;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * JS Workflow Edit configuration
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "content"
})
public class WorkflowPublish
    extends ControllerObject
{

    /**
     * workflow
     * <p>
     * deploy object with fixed property 'TYPE':'Workflow'
     * 
     */
    @JsonProperty("content")
    @JsonPropertyDescription("deploy object with fixed property 'TYPE':'Workflow'")
    private Workflow content;

    /**
     * workflow
     * <p>
     * deploy object with fixed property 'TYPE':'Workflow'
     * 
     */
    @JsonProperty("content")
    public Workflow getContent() {
        return content;
    }

    /**
     * workflow
     * <p>
     * deploy object with fixed property 'TYPE':'Workflow'
     * 
     */
    @JsonProperty("content")
    public void setContent(Workflow content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("content", content).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(content).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof WorkflowPublish) == false) {
            return false;
        }
        WorkflowPublish rhs = ((WorkflowPublish) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(content, rhs.content).isEquals();
    }

}
