
package com.sos.joc.model.inventory.common;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.controller.model.common.SyncState;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * folder item
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
    "workflowNames",
    "deleted",
    "deployed",
    "released",
    "modified",
    "hasDeployments",
    "hasReleases",
    "syncState"
})
public class ResponseFolderItem {

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
    /**
     * additional field only for schedules
     * 
     */
    @JsonProperty("workflowNames")
    @JsonPropertyDescription("additional field only for schedules")
    private List<String> workflowNames = new ArrayList<String>();
    @JsonProperty("deleted")
    private Boolean deleted;
    @JsonProperty("deployed")
    private Boolean deployed;
    @JsonProperty("released")
    private Boolean released;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("modified")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date modified;
    @JsonProperty("hasDeployments")
    private Boolean hasDeployments;
    @JsonProperty("hasReleases")
    private Boolean hasReleases;
    /**
     * sync state
     * <p>
     * 
     * 
     */
    @JsonProperty("syncState")
    private SyncState syncState;

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

    /**
     * additional field only for schedules
     * 
     */
    @JsonProperty("workflowNames")
    public List<String> getWorkflowNames() {
        return workflowNames;
    }

    /**
     * additional field only for schedules
     * 
     */
    @JsonProperty("workflowNames")
    public void setWorkflowNames(List<String> workflowNames) {
        this.workflowNames = workflowNames;
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

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("modified")
    public Date getModified() {
        return modified;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("modified")
    public void setModified(Date modified) {
        this.modified = modified;
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
     * sync state
     * <p>
     * 
     * 
     */
    @JsonProperty("syncState")
    public SyncState getSyncState() {
        return syncState;
    }

    /**
     * sync state
     * <p>
     * 
     * 
     */
    @JsonProperty("syncState")
    public void setSyncState(SyncState syncState) {
        this.syncState = syncState;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("path", path).append("name", name).append("objectType", objectType).append("title", title).append("valid", valid).append("workflowNames", workflowNames).append("deleted", deleted).append("deployed", deployed).append("released", released).append("modified", modified).append("hasDeployments", hasDeployments).append("hasReleases", hasReleases).append("syncState", syncState).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(hasDeployments).append(syncState).append(deployed).append(title).append(objectType).append(valid).append(workflowNames).append(path).append(deleted).append(name).append(modified).append(id).append(released).append(hasReleases).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ResponseFolderItem) == false) {
            return false;
        }
        ResponseFolderItem rhs = ((ResponseFolderItem) other);
        return new EqualsBuilder().append(hasDeployments, rhs.hasDeployments).append(syncState, rhs.syncState).append(deployed, rhs.deployed).append(title, rhs.title).append(objectType, rhs.objectType).append(valid, rhs.valid).append(workflowNames, rhs.workflowNames).append(path, rhs.path).append(deleted, rhs.deleted).append(name, rhs.name).append(modified, rhs.modified).append(id, rhs.id).append(released, rhs.released).append(hasReleases, rhs.hasReleases).isEquals();
    }

}
