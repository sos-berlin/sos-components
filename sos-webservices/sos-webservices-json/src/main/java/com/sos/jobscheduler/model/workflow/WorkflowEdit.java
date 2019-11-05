
package com.sos.jobscheduler.model.workflow;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.deploy.JSObjectEdit;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


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
public class WorkflowEdit
    extends JSObjectEdit
{

    @JsonProperty("content")
    private Workflow content;

    @JsonProperty("content")
    public Workflow getContent() {
        return content;
    }

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
        if ((other instanceof WorkflowEdit) == false) {
            return false;
        }
        WorkflowEdit rhs = ((WorkflowEdit) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(content, rhs.content).isEquals();
    }

}
