
package com.sos.jobscheduler.model.workflow;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * workflowId
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "path",
    "versionId"
})
public class WorkflowId {

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String path;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("versionId")
    private String versionId;

    /**
     * No args constructor for use in serialization
     * 
     */
    public WorkflowId() {
    }

    /**
     * 
     * @param path
     * @param versionId
     */
    public WorkflowId(String path, String versionId) {
        super();
        this.path = path;
        this.versionId = versionId;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
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
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("versionId")
    public String getVersionId() {
        return versionId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("versionId")
    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("path", path).append("versionId", versionId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(path).append(versionId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof WorkflowId) == false) {
            return false;
        }
        WorkflowId rhs = ((WorkflowId) other);
        return new EqualsBuilder().append(path, rhs.path).append(versionId, rhs.versionId).isEquals();
    }

}
