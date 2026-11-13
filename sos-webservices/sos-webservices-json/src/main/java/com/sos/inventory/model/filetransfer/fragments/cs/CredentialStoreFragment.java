
package com.sos.inventory.model.filetransfer.fragments.cs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.fragments.AFragment;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Credential Store Fragment
 * <p>
 * Credential Store Fragment.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "file",
    "authentication",
    "entryPath"
})
public class CredentialStoreFragment
    extends AFragment
{

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("file")
    private String file;
    /**
     * Credential store authentication. Provide either 'password' or 'keyFile'.
     * (Required)
     * 
     */
    @JsonProperty("authentication")
    @JsonPropertyDescription("Credential store authentication. Provide either 'password' or 'keyFile'.")
    private CredentialStoreAuthentication authentication;
    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("entryPath")
    private String entryPath;

    /**
     * No args constructor for use in serialization
     * 
     */
    public CredentialStoreFragment() {
    }

    /**
     * 
     * @param path
     * @param file
     * @param documentationName
     * @param entryPath
     * @param title
     * @param version
     * @param authentication
     */
    public CredentialStoreFragment(String file, CredentialStoreAuthentication authentication, String entryPath, String version, String path, String title, String documentationName) {
        super(version, path, title, documentationName);
        this.file = file;
        this.authentication = authentication;
        this.entryPath = entryPath;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("file")
    public String getFile() {
        return file;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("file")
    public void setFile(String file) {
        this.file = file;
    }

    /**
     * Credential store authentication. Provide either 'password' or 'keyFile'.
     * (Required)
     * 
     */
    @JsonProperty("authentication")
    public CredentialStoreAuthentication getAuthentication() {
        return authentication;
    }

    /**
     * Credential store authentication. Provide either 'password' or 'keyFile'.
     * (Required)
     * 
     */
    @JsonProperty("authentication")
    public void setAuthentication(CredentialStoreAuthentication authentication) {
        this.authentication = authentication;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("entryPath")
    public String getEntryPath() {
        return entryPath;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("entryPath")
    public void setEntryPath(String entryPath) {
        this.entryPath = entryPath;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("file", file).append("authentication", authentication).append("entryPath", entryPath).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(file).append(entryPath).append(authentication).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CredentialStoreFragment) == false) {
            return false;
        }
        CredentialStoreFragment rhs = ((CredentialStoreFragment) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(file, rhs.file).append(entryPath, rhs.entryPath).append(authentication, rhs.authentication).isEquals();
    }

}
