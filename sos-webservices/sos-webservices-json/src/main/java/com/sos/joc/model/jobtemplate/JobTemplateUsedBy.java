
package com.sos.joc.model.jobtemplate;

import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * JobTemplate used by Workflows
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "path",
    "hash",
    "workflows"
})
public class JobTemplateUsedBy {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("name")
    private String name;
    /**
     * path
     * <p>
     * absolute path of an object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path of an object.")
    private String path;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("hash")
    private String hash;
    @JsonProperty("workflows")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<JobTemplateUsedByWorkflow> workflows = new LinkedHashSet<JobTemplateUsedByWorkflow>();

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
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
     * absolute path of an object.
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
    @JsonProperty("hash")
    public String getHash() {
        return hash;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("hash")
    public void setHash(String hash) {
        this.hash = hash;
    }

    @JsonProperty("workflows")
    public Set<JobTemplateUsedByWorkflow> getWorkflows() {
        return workflows;
    }

    @JsonProperty("workflows")
    public void setWorkflows(Set<JobTemplateUsedByWorkflow> workflows) {
        this.workflows = workflows;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("name", name).append("path", path).append("hash", hash).append("workflows", workflows).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).append(path).append(workflows).append(hash).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobTemplateUsedBy) == false) {
            return false;
        }
        JobTemplateUsedBy rhs = ((JobTemplateUsedBy) other);
        return new EqualsBuilder().append(name, rhs.name).append(path, rhs.path).append(workflows, rhs.workflows).append(hash, rhs.hash).isEquals();
    }

}
