
package com.sos.joc.model.publish.folder;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * ExportFolderForSigningFilter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "objectTypes",
    "folders",
    "recursive",
    "withoutDrafts",
    "withoutDeployed"
})
public class ExportFolderForSigning {

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("objectTypes")
    private List<ConfigurationType> objectTypes = new ArrayList<ConfigurationType>();
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("folders")
    private List<String> folders = new ArrayList<String>();
    @JsonProperty("recursive")
    private Boolean recursive = false;
    @JsonProperty("withoutDrafts")
    private Boolean withoutDrafts = false;
    @JsonProperty("withoutDeployed")
    private Boolean withoutDeployed = false;

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("objectTypes")
    public List<ConfigurationType> getObjectTypes() {
        return objectTypes;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("objectTypes")
    public void setObjectTypes(List<ConfigurationType> objectTypes) {
        this.objectTypes = objectTypes;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("folders")
    public List<String> getFolders() {
        return folders;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("folders")
    public void setFolders(List<String> folders) {
        this.folders = folders;
    }

    @JsonProperty("recursive")
    public Boolean getRecursive() {
        return recursive;
    }

    @JsonProperty("recursive")
    public void setRecursive(Boolean recursive) {
        this.recursive = recursive;
    }

    @JsonProperty("withoutDrafts")
    public Boolean getWithoutDrafts() {
        return withoutDrafts;
    }

    @JsonProperty("withoutDrafts")
    public void setWithoutDrafts(Boolean withoutDrafts) {
        this.withoutDrafts = withoutDrafts;
    }

    @JsonProperty("withoutDeployed")
    public Boolean getWithoutDeployed() {
        return withoutDeployed;
    }

    @JsonProperty("withoutDeployed")
    public void setWithoutDeployed(Boolean withoutDeployed) {
        this.withoutDeployed = withoutDeployed;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("objectTypes", objectTypes).append("folders", folders).append("recursive", recursive).append("withoutDrafts", withoutDrafts).append("withoutDeployed", withoutDeployed).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(folders).append(controllerId).append(withoutDeployed).append(withoutDrafts).append(objectTypes).append(recursive).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ExportFolderForSigning) == false) {
            return false;
        }
        ExportFolderForSigning rhs = ((ExportFolderForSigning) other);
        return new EqualsBuilder().append(folders, rhs.folders).append(controllerId, rhs.controllerId).append(withoutDeployed, rhs.withoutDeployed).append(withoutDrafts, rhs.withoutDrafts).append(objectTypes, rhs.objectTypes).append(recursive, rhs.recursive).isEquals();
    }

}
