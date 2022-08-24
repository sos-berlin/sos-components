
package com.sos.joc.model.jobtemplate.propagate;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Filter for Workflows updates from JobTemplates in specified folder
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "workflowPaths",
    "folder",
    "recursive"
})
public class WorkflowPropagateFilter
    extends JobTemplatesPropagateBaseFilter
{

    @JsonProperty("workflowPaths")
    private List<String> workflowPaths = new ArrayList<String>();
    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("folder")
    @JsonPropertyDescription("absolute path of an object.")
    private String folder;
    @JsonProperty("recursive")
    private Boolean recursive = false;

    @JsonProperty("workflowPaths")
    public List<String> getWorkflowPaths() {
        return workflowPaths;
    }

    @JsonProperty("workflowPaths")
    public void setWorkflowPaths(List<String> workflowPaths) {
        this.workflowPaths = workflowPaths;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("folder")
    public String getFolder() {
        return folder;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("folder")
    public void setFolder(String folder) {
        this.folder = folder;
    }

    @JsonProperty("recursive")
    public Boolean getRecursive() {
        return recursive;
    }

    @JsonProperty("recursive")
    public void setRecursive(Boolean recursive) {
        this.recursive = recursive;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("workflowPaths", workflowPaths).append("folder", folder).append("recursive", recursive).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(workflowPaths).append(folder).append(recursive).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof WorkflowPropagateFilter) == false) {
            return false;
        }
        WorkflowPropagateFilter rhs = ((WorkflowPropagateFilter) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(workflowPaths, rhs.workflowPaths).append(folder, rhs.folder).append(recursive, rhs.recursive).isEquals();
    }

}
