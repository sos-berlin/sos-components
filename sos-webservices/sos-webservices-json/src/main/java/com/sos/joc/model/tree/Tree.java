
package com.sos.joc.model.tree;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
 * folder
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "path",
    "name",
    "deleted",
    "permitted",
    "repoControlled",
    "lockedBy",
    "lockedSince",
    "permissions",
    "folders"
})
public class Tree {

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
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    private String name;
    @JsonProperty("deleted")
    private Boolean deleted;
    @JsonProperty("permitted")
    private Boolean permitted = true;
    /**
     * only for top level folder in the reponse
     * 
     */
    @JsonProperty("repoControlled")
    @JsonPropertyDescription("only for top level folder in the reponse")
    private Boolean repoControlled;
    @JsonProperty("lockedBy")
    private String lockedBy;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("lockedSince")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date lockedSince;
    @JsonProperty("permissions")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> permissions = null;
    @JsonProperty("folders")
    private List<Tree> folders = new ArrayList<Tree>();

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
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("deleted")
    public Boolean getDeleted() {
        return deleted;
    }

    @JsonProperty("deleted")
    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    @JsonProperty("permitted")
    public Boolean getPermitted() {
        return permitted;
    }

    @JsonProperty("permitted")
    public void setPermitted(Boolean permitted) {
        this.permitted = permitted;
    }

    /**
     * only for top level folder in the reponse
     * 
     */
    @JsonProperty("repoControlled")
    public Boolean getRepoControlled() {
        return repoControlled;
    }

    /**
     * only for top level folder in the reponse
     * 
     */
    @JsonProperty("repoControlled")
    public void setRepoControlled(Boolean repoControlled) {
        this.repoControlled = repoControlled;
    }

    @JsonProperty("lockedBy")
    public String getLockedBy() {
        return lockedBy;
    }

    @JsonProperty("lockedBy")
    public void setLockedBy(String lockedBy) {
        this.lockedBy = lockedBy;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("lockedSince")
    public Date getLockedSince() {
        return lockedSince;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("lockedSince")
    public void setLockedSince(Date lockedSince) {
        this.lockedSince = lockedSince;
    }

    @JsonProperty("permissions")
    public Set<String> getPermissions() {
        return permissions;
    }

    @JsonProperty("permissions")
    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    @JsonProperty("folders")
    public List<Tree> getFolders() {
        return folders;
    }

    @JsonProperty("folders")
    public void setFolders(List<Tree> folders) {
        this.folders = folders;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("path", path).append("name", name).append("deleted", deleted).append("permitted", permitted).append("repoControlled", repoControlled).append("lockedBy", lockedBy).append("lockedSince", lockedSince).append("permissions", permissions).append("folders", folders).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(path).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Tree) == false) {
            return false;
        }
        Tree rhs = ((Tree) other);
        return new EqualsBuilder().append(path, rhs.path).isEquals();
    }

}
