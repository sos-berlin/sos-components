
package com.sos.inventory.model.filetransfer.fragments.protocol.common.ssl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "type",
    "file",
    "password"
})
public class KeyStore {

    /**
     * Key store type: JKS, JCEKS, PKCS12, PKCS11 or DKS. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("type")
    private String type;
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
    @JsonProperty("password")
    private String password;

    /**
     * No args constructor for use in serialization
     * 
     */
    public KeyStore() {
    }

    /**
     * 
     * @param password
     * @param file
     * @param type
     */
    public KeyStore(String type, String file, String password) {
        super();
        this.type = type;
        this.file = file;
        this.password = password;
    }

    /**
     * Key store type: JKS, JCEKS, PKCS12, PKCS11 or DKS. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    /**
     * Key store type: JKS, JCEKS, PKCS12, PKCS11 or DKS. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
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
    @JsonProperty("password")
    public String getPassword() {
        return password;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("password")
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("type", type).append("file", file).append("password", password).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(password).append(file).append(type).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof KeyStore) == false) {
            return false;
        }
        KeyStore rhs = ((KeyStore) other);
        return new EqualsBuilder().append(password, rhs.password).append(file, rhs.file).append(type, rhs.type).isEquals();
    }

}
