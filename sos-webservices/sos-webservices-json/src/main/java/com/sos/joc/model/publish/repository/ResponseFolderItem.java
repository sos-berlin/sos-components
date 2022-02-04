
package com.sos.joc.model.publish.repository;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * ResponseFolderItem
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "folder",
    "objectName",
    "objectType",
    "lastModified"
})
public class ResponseFolderItem {

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
     * configuration types
     * <p>
     * 
     * 
     */
    @JsonProperty("objectType")
    private ConfigurationType objectType;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("folder", folder).append("objectName", objectName).append("objectType", objectType).append("lastModified", lastModified).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(objectName).append(folder).append(lastModified).append(objectType).toHashCode();
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
        return new EqualsBuilder().append(objectName, rhs.objectName).append(folder, rhs.folder).append(lastModified, rhs.lastModified).append(objectType, rhs.objectType).isEquals();
    }

}
