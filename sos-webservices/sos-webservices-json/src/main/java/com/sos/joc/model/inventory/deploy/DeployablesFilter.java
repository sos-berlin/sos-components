
package com.sos.joc.model.inventory.deploy;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Filter Deployables
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "folder",
    "recursive",
    "objectTypes",
    "onlyValidObjects",
    "withVersions"
})
public class DeployablesFilter {

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("folder")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String folder;
    @JsonProperty("recursive")
    private Boolean recursive = false;
    @JsonProperty("objectTypes")
    private List<ConfigurationType> objectTypes = new ArrayList<ConfigurationType>();
    @JsonProperty("onlyValidObjects")
    private Boolean onlyValidObjects = false;
    @JsonProperty("withVersions")
    private Boolean withVersions = false;

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("folder")
    public String getFolder() {
        return folder;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
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

    @JsonProperty("objectTypes")
    public List<ConfigurationType> getObjectTypes() {
        return objectTypes;
    }

    @JsonProperty("objectTypes")
    public void setObjectTypes(List<ConfigurationType> objectTypes) {
        this.objectTypes = objectTypes;
    }

    @JsonProperty("onlyValidObjects")
    public Boolean getOnlyValidObjects() {
        return onlyValidObjects;
    }

    @JsonProperty("onlyValidObjects")
    public void setOnlyValidObjects(Boolean onlyValidObjects) {
        this.onlyValidObjects = onlyValidObjects;
    }

    @JsonProperty("withVersions")
    public Boolean getWithVersions() {
        return withVersions;
    }

    @JsonProperty("withVersions")
    public void setWithVersions(Boolean withVersions) {
        this.withVersions = withVersions;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("folder", folder).append("recursive", recursive).append("objectTypes", objectTypes).append("onlyValidObjects", onlyValidObjects).append("withVersions", withVersions).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(folder).append(objectTypes).append(recursive).append(onlyValidObjects).append(withVersions).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DeployablesFilter) == false) {
            return false;
        }
        DeployablesFilter rhs = ((DeployablesFilter) other);
        return new EqualsBuilder().append(folder, rhs.folder).append(objectTypes, rhs.objectTypes).append(recursive, rhs.recursive).append(onlyValidObjects, rhs.onlyValidObjects).append(withVersions, rhs.withVersions).isEquals();
    }

}
