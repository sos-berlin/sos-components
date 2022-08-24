
package com.sos.joc.model.jobtemplate.propagate;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * JobTemplate propagate filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "path",
    "workflows"
})
public class JobTemplatePropagateFilter {

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("path")
    private String path;
    @JsonProperty("workflows")
    private List<String> workflows = new ArrayList<String>();

    /**
     * string without < and >
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
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    @JsonProperty("workflows")
    public List<String> getWorkflows() {
        return workflows;
    }

    @JsonProperty("workflows")
    public void setWorkflows(List<String> workflows) {
        this.workflows = workflows;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("path", path).append("workflows", workflows).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(path).append(workflows).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobTemplatePropagateFilter) == false) {
            return false;
        }
        JobTemplatePropagateFilter rhs = ((JobTemplatePropagateFilter) other);
        return new EqualsBuilder().append(path, rhs.path).append(workflows, rhs.workflows).isEquals();
    }

}
