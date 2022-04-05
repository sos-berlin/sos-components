
package com.sos.joc.model.tree;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


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
    @JsonProperty("repoControlled")
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

    @JsonProperty("repoControlled")
    public Boolean getRepoControlled() {
        return repoControlled;
    }

    @JsonProperty("repoControlled")
    public void setRepoControlled(Boolean permitted) {
        this.repoControlled = permitted;
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
        return new ToStringBuilder(this).append("path", path).append("name", name).append("deleted", deleted).append("permitted", permitted).append("lockedBy", lockedBy).append("lockedSince", lockedSince).append("folders", folders).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(path).append(deleted).append(folders).append(permitted).append(lockedBy).append(lockedSince).append(name).toHashCode();
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
        return new EqualsBuilder().append(path, rhs.path).append(deleted, rhs.deleted).append(folders, rhs.folders).append(permitted, rhs.permitted).append(lockedBy, rhs.lockedBy).append(lockedSince, rhs.lockedSince).append(name, rhs.name).isEquals();
    }

}
