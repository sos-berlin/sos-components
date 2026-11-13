
package com.sos.inventory.model.filetransfer;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.common.IInventoryObject;
import com.sos.joc.model.common.IConfigurationObject;
import com.sos.joc.model.common.IReleaseObject;

/** Manually maintained. Do not regenerate from schema.
 * <p>
 * File Transfer base
 * <p>
 * Abstract base for File Transfer Inventory releasable configuration objects */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({ "version", "path", "title", "documentationName" })
public abstract class AFileTransfer implements IInventoryObject, IConfigurationObject, IReleaseObject {

    /** inventory repository version
     * <p>
     * inventory repository version */
    @JsonProperty("version")
    @JsonPropertyDescription("inventory repository version")
    private String version = "1.7.2";
    /** path
     * <p>
     * absolute path of an object. */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path of an object.")
    private String path;
    /** string without < and >
     * <p>
     */
    @JsonProperty("title")
    private String title;
    /** string without < and >
     * <p>
     */
    @JsonProperty("documentationName")
    private String documentationName;

    /** No args constructor for use in serialization */
    public AFileTransfer() {
    }

    /** @param path
     * @param documentationName
     * @param title
     * @param version */
    public AFileTransfer(String version, String path, String title, String documentationName) {
        super();
        this.version = version;
        this.path = path;
        this.title = title;
        this.documentationName = documentationName;
    }

    /** inventory repository version
     * <p>
     * inventory repository version */
    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    /** inventory repository version
     * <p>
     * inventory repository version */
    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    /** path
     * <p>
     * absolute path of an object. */
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /** path
     * <p>
     * absolute path of an object. */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    /** string without < and >
     * <p>
     */
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    /** string without < and >
     * <p>
     */
    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    /** string without < and >
     * <p>
     */
    @JsonProperty("documentationName")
    public String getDocumentationName() {
        return documentationName;
    }

    /** string without < and >
     * <p>
     */
    @JsonProperty("documentationName")
    public void setDocumentationName(String documentationName) {
        this.documentationName = documentationName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("path", path).append("title", title).append("documentationName", documentationName).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(path).append(documentationName).append(title).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AFileTransfer) == false) {
            return false;
        }
        AFileTransfer rhs = ((AFileTransfer) other);
        return new EqualsBuilder().append(path, rhs.path).append(documentationName, rhs.documentationName).append(title, rhs.title).isEquals();
    }

    @Override
    /** Compares all fields of this object and its subclasses using reflection, excluding version, title, and documentationName. */
    public boolean sufficientlyEquals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AFileTransfer) == false) {
            return false;
        }
        return EqualsBuilder.reflectionEquals(this, other, "version", "title", "documentationName");
    }

}
