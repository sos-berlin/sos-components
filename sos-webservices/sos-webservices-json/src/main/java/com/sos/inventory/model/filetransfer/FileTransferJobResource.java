
package com.sos.inventory.model.filetransfer;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.common.IInventoryObject;
import com.sos.inventory.model.filetransfer.profile.Profile;
import com.sos.inventory.model.filetransfer.settings.Settings;
import com.sos.joc.model.common.IConfigurationObject;
import com.sos.joc.model.common.IDeployObject;

/** Manually maintained. Do not regenerate from schema.
 * <p>
 * File Transfer Job Resource per profile
 * <p>
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
//@JsonPropertyOrder({ "TYPE", "version", "profile", "settings", "documentationName", "title" })
@JsonPropertyOrder({ "version", "profile", "settings", "documentationName", "title" })
public class FileTransferJobResource implements IInventoryObject, IConfigurationObject, IDeployObject {

    /** deployType
     * <p>
     */
    //@JsonProperty("TYPE")
    //private DeployType tYPE = DeployType.JOBRESOURCE;

    /** inventory repository version
     * <p>
     * inventory repository version */
    @JsonProperty("version")
    @JsonPropertyDescription("inventory repository version")
    private String version = "1.7.2";
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
    /** Profile
     * <p>
     * 
     * (Required) */
    @JsonProperty("profile")
    private Profile profile;
    /** File Transfer Job Resource additional settings
     * <p>
     */
    @JsonProperty("settings")
    private Settings settings;

    /** No args constructor for use in serialization */
    public FileTransferJobResource() {
    }

    /** @param settings
     * @param profile
     * @param documentationName
     * @param title
     * @param version */
    public FileTransferJobResource(String version, String title, String documentationName, Profile profile, Settings settings) {
        super();
        this.version = version;
        this.title = title;
        this.documentationName = documentationName;
        this.profile = profile;
        this.settings = settings;
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

    /** Profile
     * <p>
     * 
     * (Required) */
    @JsonProperty("profile")
    public Profile getProfile() {
        return profile;
    }

    /** Profile
     * <p>
     * 
     * (Required) */
    @JsonProperty("profile")
    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    /** File Transfer Job Resource additional settings
     * <p>
     */
    @JsonProperty("settings")
    public Settings getSettings() {
        return settings;
    }

    /** File Transfer Job Resource additional settings
     * <p>
     */
    @JsonProperty("settings")
    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("version", version).append("title", title).append("documentationName", documentationName).append(
                "profile", profile).append("settings", settings).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(settings).append(documentationName).append(title).append(version).append(profile).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FileTransferJobResource) == false) {
            return false;
        }
        FileTransferJobResource rhs = ((FileTransferJobResource) other);
        return new EqualsBuilder().append(settings, rhs.settings).append(documentationName, rhs.documentationName).append(title, rhs.title).append(
                version, rhs.version).append(profile, rhs.profile).isEquals();
    }

    @Override
    public boolean sufficientlyEquals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FileTransferJobResource) == false) {
            return false;
        }
        FileTransferJobResource rhs = ((FileTransferJobResource) other);
        return new EqualsBuilder().append(settings, rhs.settings).append(profile, rhs.profile).isEquals();
    }

}
