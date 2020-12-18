
package com.sos.joc.model.inventory.release;

import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sos.joc.model.inventory.common.ConfigurationType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * ResponseReleasableTreeItem
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "folder",
    "objectName",
    "account",
    "objectType",
    "valid",
    "deleted",
    "released",
    "releasableVersions"
})
public class ResponseReleasableTreeItem {

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
     * 
     */
    @JsonProperty("folder")
    @JsonPropertyDescription("absolute path of an object.")
    private String folder;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("objectName")
    private String objectName;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("account")
    private String account;
    /**
     * configuration types
     * <p>
     * 
     * 
     */
    @JsonProperty("objectType")
    private ConfigurationType objectType;
    @JsonProperty("valid")
    private Boolean valid;
    @JsonProperty("deleted")
    private Boolean deleted;
    @JsonProperty("released")
    private Boolean released;
    @JsonProperty("releasableVersions")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ResponseReleasableVersion> releasableVersions = new LinkedHashSet<ResponseReleasableVersion>();

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
     * 
     */
    @JsonProperty("folder")
    public String getFolder() {
        return folder;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("folder")
    public void setFolder(String folder) {
        this.folder = folder;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("objectName")
    public String getObjectName() {
        return objectName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("objectName")
    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("account")
    public String getAccount() {
        return account;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("account")
    public void setAccount(String account) {
        this.account = account;
    }

    /**
     * configuration types
     * <p>
     * 
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
     * 
     */
    @JsonProperty("objectType")
    public void setObjectType(ConfigurationType objectType) {
        this.objectType = objectType;
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

    @JsonProperty("released")
    public Boolean getReleased() {
        return released;
    }

    @JsonProperty("released")
    public void setReleased(Boolean released) {
        this.released = released;
    }

    @JsonProperty("releasableVersions")
    public Set<ResponseReleasableVersion> getReleasableVersions() {
        return releasableVersions;
    }

    @JsonProperty("releasableVersions")
    public void setReleasableVersions(Set<ResponseReleasableVersion> releasableVersions) {
        this.releasableVersions = releasableVersions;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("folder", folder).append("objectName", objectName).append("account", account).append("objectType", objectType).append("valid", valid).append("deleted", deleted).append("released", released).append("releasableVersions", releasableVersions).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(valid).append(folder).append(deleted).append(releasableVersions).append(objectName).append(id).append(account).append(released).append(objectType).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ResponseReleasableTreeItem) == false) {
            return false;
        }
        ResponseReleasableTreeItem rhs = ((ResponseReleasableTreeItem) other);
        return new EqualsBuilder().append(valid, rhs.valid).append(folder, rhs.folder).append(deleted, rhs.deleted).append(releasableVersions, rhs.releasableVersions).append(objectName, rhs.objectName).append(id, rhs.id).append(account, rhs.account).append(released, rhs.released).append(objectType, rhs.objectType).isEquals();
    }

}
