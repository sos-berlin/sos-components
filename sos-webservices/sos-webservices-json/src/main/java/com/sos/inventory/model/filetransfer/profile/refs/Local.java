
package com.sos.inventory.model.filetransfer.profile.refs;

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
 * Local source/target options. Only allowed when type is LOCAL.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "credentialStoreRef",
    "decryptionRef",
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
    @JsonProperty("credentialStoreRef")
    private String credentialStoreRef;
    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("decryptionRef")
    private String decryptionRef;
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
     * @param credentialStoreRef
     * @param processing
     * @param decryptionRef
     */
    public Local(String credentialStoreRef, String decryptionRef, Processing processing, Rename rename) {
        super();
        this.credentialStoreRef = credentialStoreRef;
        this.decryptionRef = decryptionRef;
        this.processing = processing;
        this.rename = rename;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("credentialStoreRef")
    public String getCredentialStoreRef() {
        return credentialStoreRef;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("credentialStoreRef")
    public void setCredentialStoreRef(String credentialStoreRef) {
        this.credentialStoreRef = credentialStoreRef;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("decryptionRef")
    public String getDecryptionRef() {
        return decryptionRef;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("decryptionRef")
    public void setDecryptionRef(String decryptionRef) {
        this.decryptionRef = decryptionRef;
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
        return new ToStringBuilder(this).append("credentialStoreRef", credentialStoreRef).append("decryptionRef", decryptionRef).append("processing", processing).append("rename", rename).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(credentialStoreRef).append(processing).append(decryptionRef).append(rename).toHashCode();
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
        return new EqualsBuilder().append(credentialStoreRef, rhs.credentialStoreRef).append(processing, rhs.processing).append(decryptionRef, rhs.decryptionRef).append(rename, rhs.rename).isEquals();
    }

}
