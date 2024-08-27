
package com.sos.controller.model.jobresource;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.job.Environment;


/**
 * delete JobResourcePath
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "path",
    "valid",
    "deployed"
})
public class JobResource
    extends com.sos.inventory.model.jobresource.JobResource
{

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path of an object.")
    private String path;
    @JsonProperty("valid")
    private Boolean valid;
    @JsonProperty("deployed")
    private Boolean deployed;

    /**
     * No args constructor for use in serialization
     * 
     */
    public JobResource() {
    }

    /**
     * 
     * @param valid
     * @param path
     * @param deployed
     * @param arguments
     * @param documentationName
     * @param env
     * @param title
     */
    public JobResource(String path, Boolean valid, Boolean deployed, Environment arguments, Environment env, String documentationName, String title) {
        super(arguments, env, documentationName, title);
        this.path = path;
        this.valid = valid;
        this.deployed = deployed;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    @JsonProperty("valid")
    public Boolean getValid() {
        return valid;
    }

    @JsonProperty("valid")
    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    @JsonProperty("deployed")
    public Boolean getDeployed() {
        return deployed;
    }

    @JsonProperty("deployed")
    public void setDeployed(Boolean deployed) {
        this.deployed = deployed;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("path", path).append("valid", valid).append("deployed", deployed).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(valid).append(path).append(deployed).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobResource) == false) {
            return false;
        }
        JobResource rhs = ((JobResource) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(valid, rhs.valid).append(path, rhs.path).append(deployed, rhs.deployed).isEquals();
    }

}
