
package com.sos.joc.model.inventory.search;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Inventory search response item
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "path",
    "name",
    "objectType",
    "title",
    "valid",
    "deleted",
    "deployed",
    "released",
    "hasDeployments",
    "hasReleases",
    "controllerId"
})
public class ResponseSearchItem {

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    private Long id;
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
     * (Required)
     * 
     */
    @JsonProperty("name")
    private String name;
    /**
     * configuration types
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("objectType")
    private ConfigurationType objectType;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    private String title;
    @JsonProperty("valid")
    private Boolean valid;
    @JsonProperty("deleted")
    private Boolean deleted;
    @JsonProperty("deployed")
    private Boolean deployed;
    @JsonProperty("released")
    private Boolean released;
    @JsonProperty("hasDeployments")
    private Boolean hasDeployments;
    @JsonProperty("hasReleases")
    private Boolean hasReleases;
    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    public Long getId() {
        return id;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    public void setId(Long id) {
        this.id = id;
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
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * configuration types
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("objectType")
    public ConfigurationType getObjectType() {
        return objectType;
    }

    /**
     * configuration types
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("objectType")
    public void setObjectType(ConfigurationType objectType) {
        this.objectType = objectType;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    @JsonProperty("valid")
    public Boolean getValid() {
        return valid;
    }

    @JsonProperty("valid")
    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    @JsonProperty("deleted")
    public Boolean getDeleted() {
        return deleted;
    }

    @JsonProperty("deleted")
    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    @JsonProperty("deployed")
    public Boolean getDeployed() {
        return deployed;
    }

    @JsonProperty("deployed")
    public void setDeployed(Boolean deployed) {
        this.deployed = deployed;
    }

    @JsonProperty("released")
    public Boolean getReleased() {
        return released;
    }

    @JsonProperty("released")
    public void setReleased(Boolean released) {
        this.released = released;
    }

    @JsonProperty("hasDeployments")
    public Boolean getHasDeployments() {
        return hasDeployments;
    }

    @JsonProperty("hasDeployments")
    public void setHasDeployments(Boolean hasDeployments) {
        this.hasDeployments = hasDeployments;
    }

    @JsonProperty("hasReleases")
    public Boolean getHasReleases() {
        return hasReleases;
    }

    @JsonProperty("hasReleases")
    public void setHasReleases(Boolean hasReleases) {
        this.hasReleases = hasReleases;
    }

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("path", path).append("name", name).append("objectType", objectType).append("title", title).append("valid", valid).append("deleted", deleted).append("deployed", deployed).append("released", released).append("hasDeployments", hasDeployments).append("hasReleases", hasReleases).append("controllerId", controllerId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllerId).append(hasDeployments).append(deployed).append(title).append(objectType).append(valid).append(path).append(deleted).append(name).append(id).append(released).append(hasReleases).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ResponseSearchItem) == false) {
            return false;
        }
        ResponseSearchItem rhs = ((ResponseSearchItem) other);
        return new EqualsBuilder().append(controllerId, rhs.controllerId).append(hasDeployments, rhs.hasDeployments).append(deployed, rhs.deployed).append(title, rhs.title).append(objectType, rhs.objectType).append(valid, rhs.valid).append(path, rhs.path).append(deleted, rhs.deleted).append(name, rhs.name).append(id, rhs.id).append(released, rhs.released).append(hasReleases, rhs.hasReleases).isEquals();
    }

}
