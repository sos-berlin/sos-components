
package com.sos.joc.model.publish;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Deploy load filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobschedulerId",
    "objectType",
    "path"
})
public class DeployLoadFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    private String jobschedulerId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("objectType")
    private String objectType;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("path")
    private String path;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("objectType")
    public String getObjectType() {
        return objectType;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("objectType")
    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /**
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
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("objectType", objectType).append("path", path).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(path).append(jobschedulerId).append(objectType).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DeployLoadFilter) == false) {
            return false;
        }
        DeployLoadFilter rhs = ((DeployLoadFilter) other);
        return new EqualsBuilder().append(path, rhs.path).append(jobschedulerId, rhs.jobschedulerId).append(objectType, rhs.objectType).isEquals();
    }

}
