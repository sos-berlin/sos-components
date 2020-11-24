
package com.sos.joc.model.publish;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.jobscheduler.model.deploy.DeployType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * DeployConfiguration
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "path",
    "objectType",
    "commitId"
})
public class DeployConfiguration {

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
     * deployType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("objectType")
    private DeployType objectType = DeployType.fromValue("Workflow");
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("commitId")
    private String commitId;

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
     * deployType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("objectType")
    public DeployType getObjectType() {
        return objectType;
    }

    /**
     * deployType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("objectType")
    public void setObjectType(DeployType objectType) {
        this.objectType = objectType;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("commitId")
    public String getCommitId() {
        return commitId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("commitId")
    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("path", path).append("objectType", objectType).append("commitId", commitId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(path).append(commitId).append(objectType).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DeployConfiguration) == false) {
            return false;
        }
        DeployConfiguration rhs = ((DeployConfiguration) other);
        return new EqualsBuilder().append(path, rhs.path).append(commitId, rhs.commitId).append(objectType, rhs.objectType).isEquals();
    }

}
