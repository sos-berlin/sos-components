
package com.sos.inventory.model.filetransfer.profile.fragments;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.common.Processing;
import com.sos.inventory.model.filetransfer.profile.common.Rename;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Local Protocol Fragment Reference
 * <p>
 * Local source/target options
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "credentialStoreFragmentName",
    "decryptionFragmentName",
    "processing",
    "rename"
})
public class Local {

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("credentialStoreFragmentName")
    private String credentialStoreFragmentName;
    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("decryptionFragmentName")
    private String decryptionFragmentName;
    /**
     * SSH processing options.
     * <p>
     * 
     * 
     */
    @JsonProperty("processing")
    private Processing processing;
    /**
     * Rename
     * <p>
     * Rename rule. Usable by any profile.
     * 
     */
    @JsonProperty("rename")
    @JsonPropertyDescription("Rename rule. Usable by any profile.")
    private Rename rename;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Local() {
    }

    /**
     * 
     * @param rename
     * @param processing
     * @param decryptionFragmentName
     * @param credentialStoreFragmentName
     */
    public Local(String credentialStoreFragmentName, String decryptionFragmentName, Processing processing, Rename rename) {
        super();
        this.credentialStoreFragmentName = credentialStoreFragmentName;
        this.decryptionFragmentName = decryptionFragmentName;
        this.processing = processing;
        this.rename = rename;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("credentialStoreFragmentName")
    public String getCredentialStoreFragmentName() {
        return credentialStoreFragmentName;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("credentialStoreFragmentName")
    public void setCredentialStoreFragmentName(String credentialStoreFragmentName) {
        this.credentialStoreFragmentName = credentialStoreFragmentName;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("decryptionFragmentName")
    public String getDecryptionFragmentName() {
        return decryptionFragmentName;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("decryptionFragmentName")
    public void setDecryptionFragmentName(String decryptionFragmentName) {
        this.decryptionFragmentName = decryptionFragmentName;
    }

    /**
     * SSH processing options.
     * <p>
     * 
     * 
     */
    @JsonProperty("processing")
    public Processing getProcessing() {
        return processing;
    }

    /**
     * SSH processing options.
     * <p>
     * 
     * 
     */
    @JsonProperty("processing")
    public void setProcessing(Processing processing) {
        this.processing = processing;
    }

    /**
     * Rename
     * <p>
     * Rename rule. Usable by any profile.
     * 
     */
    @JsonProperty("rename")
    public Rename getRename() {
        return rename;
    }

    /**
     * Rename
     * <p>
     * Rename rule. Usable by any profile.
     * 
     */
    @JsonProperty("rename")
    public void setRename(Rename rename) {
        this.rename = rename;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("credentialStoreFragmentName", credentialStoreFragmentName).append("decryptionFragmentName", decryptionFragmentName).append("processing", processing).append("rename", rename).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(processing).append(rename).append(decryptionFragmentName).append(credentialStoreFragmentName).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Local) == false) {
            return false;
        }
        Local rhs = ((Local) other);
        return new EqualsBuilder().append(processing, rhs.processing).append(rename, rhs.rename).append(decryptionFragmentName, rhs.decryptionFragmentName).append(credentialStoreFragmentName, rhs.credentialStoreFragmentName).isEquals();
    }

}
