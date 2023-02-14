
package com.sos.inventory.model.descriptor.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Deployment Descriptor Certificates Schema
 * <p>
 * JS7 JOC Descriptor Certificates Schema
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "keyStore",
    "keyStorePassword",
    "keyPassword",
    "keyAlias",
    "trustStore",
    "trustStorePassword"
})
public class Certificates {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("keyStore")
    private String keyStore;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("keyStorePassword")
    private String keyStorePassword;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("keyPassword")
    private String keyPassword;
    @JsonProperty("keyAlias")
    private String keyAlias;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("trustStore")
    private String trustStore;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("trustStorePassword")
    private String trustStorePassword;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Certificates() {
    }

    /**
     * 
     * @param trustStorePassword
     * @param keyStorePassword
     * @param keyAlias
     * @param keyPassword
     * @param keyStore
     * @param trustStore
     */
    public Certificates(String keyStore, String keyStorePassword, String keyPassword, String keyAlias, String trustStore, String trustStorePassword) {
        super();
        this.keyStore = keyStore;
        this.keyStorePassword = keyStorePassword;
        this.keyPassword = keyPassword;
        this.keyAlias = keyAlias;
        this.trustStore = trustStore;
        this.trustStorePassword = trustStorePassword;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("keyStore")
    public String getKeyStore() {
        return keyStore;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("keyStore")
    public void setKeyStore(String keyStore) {
        this.keyStore = keyStore;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("keyStorePassword")
    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("keyStorePassword")
    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("keyPassword")
    public String getKeyPassword() {
        return keyPassword;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("keyPassword")
    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    @JsonProperty("keyAlias")
    public String getKeyAlias() {
        return keyAlias;
    }

    @JsonProperty("keyAlias")
    public void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("trustStore")
    public String getTrustStore() {
        return trustStore;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("trustStore")
    public void setTrustStore(String trustStore) {
        this.trustStore = trustStore;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("trustStorePassword")
    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("trustStorePassword")
    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("keyStore", keyStore).append("keyStorePassword", keyStorePassword).append("keyPassword", keyPassword).append("keyAlias", keyAlias).append("trustStore", trustStore).append("trustStorePassword", trustStorePassword).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(trustStorePassword).append(keyStorePassword).append(keyAlias).append(keyPassword).append(keyStore).append(trustStore).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Certificates) == false) {
            return false;
        }
        Certificates rhs = ((Certificates) other);
        return new EqualsBuilder().append(trustStorePassword, rhs.trustStorePassword).append(keyStorePassword, rhs.keyStorePassword).append(keyAlias, rhs.keyAlias).append(keyPassword, rhs.keyPassword).append(keyStore, rhs.keyStore).append(trustStore, rhs.trustStore).isEquals();
    }

}
