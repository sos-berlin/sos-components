
package com.sos.inventory.model.filetransfer.fragments.protocol.sftp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Public key authentication.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "file",
    "passphrase"
})
public class SftpAuthPublicKey {

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
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("passphrase")
    private String passphrase;

    /**
     * No args constructor for use in serialization
     * 
     */
    public SftpAuthPublicKey() {
    }

    /**
     * 
     * @param file
     * @param passphrase
     */
    public SftpAuthPublicKey(String file, String passphrase) {
        super();
        this.file = file;
        this.passphrase = passphrase;
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
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("passphrase")
    public String getPassphrase() {
        return passphrase;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("passphrase")
    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("file", file).append("passphrase", passphrase).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(passphrase).append(file).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SftpAuthPublicKey) == false) {
            return false;
        }
        SftpAuthPublicKey rhs = ((SftpAuthPublicKey) other);
        return new EqualsBuilder().append(passphrase, rhs.passphrase).append(file, rhs.file).isEquals();
    }

}
