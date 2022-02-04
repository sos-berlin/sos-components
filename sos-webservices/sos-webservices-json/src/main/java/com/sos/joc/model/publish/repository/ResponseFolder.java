
package com.sos.joc.model.publish.repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * ResponseFolders
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "lastModified",
    "name",
    "path",
    "folders",
    "items"
})
public class ResponseFolder {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("lastModified")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date lastModified;
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
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path of an object.")
    private String path;
    @JsonProperty("folders")
    private List<ResponseFolder> folders = new ArrayList<ResponseFolder>();
    @JsonProperty("items")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ResponseFolderItem> items = new LinkedHashSet<ResponseFolderItem>();

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("lastModified")
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("lastModified")
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

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

    @JsonProperty("folders")
    public List<ResponseFolder> getFolders() {
        return folders;
    }

    @JsonProperty("folders")
    public void setFolders(List<ResponseFolder> folders) {
        this.folders = folders;
    }

    @JsonProperty("items")
    public Set<ResponseFolderItem> getItems() {
        return items;
    }

    @JsonProperty("items")
    public void setItems(Set<ResponseFolderItem> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("lastModified", lastModified).append("name", name).append("path", path).append("folders", folders).append("items", items).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).append(path).append(lastModified).append(folders).append(items).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ResponseFolder) == false) {
            return false;
        }
        ResponseFolder rhs = ((ResponseFolder) other);
        return new EqualsBuilder().append(name, rhs.name).append(path, rhs.path).append(lastModified, rhs.lastModified).append(folders, rhs.folders).append(items, rhs.items).isEquals();
    }

}
