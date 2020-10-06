
package com.sos.joc.model.inventory.common;

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
 * Filter Delete Draft
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "path",
    "recursive",
    "objectTypes"
})
public class RequestFolder {

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
    @JsonProperty("recursive")
    private Boolean recursive = false;
    @JsonProperty("objectTypes")
    private List<ConfigurationType> objectTypes = new ArrayList<ConfigurationType>();

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

    @JsonProperty("recursive")
    public Boolean getRecursive() {
        return recursive;
    }

    @JsonProperty("recursive")
    public void setRecursive(Boolean recursive) {
        this.recursive = recursive;
    }

    @JsonProperty("objectTypes")
    public List<ConfigurationType> getObjectTypes() {
        return objectTypes;
    }

    @JsonProperty("objectTypes")
    public void setObjectTypes(List<ConfigurationType> objectTypes) {
        this.objectTypes = objectTypes;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("path", path).append("recursive", recursive).append("objectTypes", objectTypes).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(path).append(objectTypes).append(recursive).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RequestFolder) == false) {
            return false;
        }
        RequestFolder rhs = ((RequestFolder) other);
        return new EqualsBuilder().append(path, rhs.path).append(objectTypes, rhs.objectTypes).append(recursive, rhs.recursive).isEquals();
    }

}
