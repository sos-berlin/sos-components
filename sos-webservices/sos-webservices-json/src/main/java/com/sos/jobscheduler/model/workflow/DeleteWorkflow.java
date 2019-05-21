
package com.sos.jobscheduler.model.workflow;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.jobscheduler.model.deploy.DeleteObject;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * delete workflow
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "path"
})
public class DeleteWorkflow
    extends DeleteObject
{

    /**
     * path
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("path")
    private String path;

    /**
     * No args constructor for use in serialization
     * 
     */
    public DeleteWorkflow() {
    }

    /**
     * 
     * @param path
     */
    public DeleteWorkflow(String path) {
        super();
        this.path = path;
    }

    /**
     * path
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /**
     * path
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("path", path).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(path).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DeleteWorkflow) == false) {
            return false;
        }
        DeleteWorkflow rhs = ((DeleteWorkflow) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(path, rhs.path).isEquals();
    }

}
