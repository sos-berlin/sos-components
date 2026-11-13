
package com.sos.inventory.model.filetransfer.fragments.protocol.common.ssl;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "trustStore",
    "keyStore"
})
public class Trusted {

    @JsonProperty("trustStore")
    private KeyStore trustStore;
    @JsonProperty("keyStore")
    private KeyStore keyStore;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Trusted() {
    }

    /**
     * 
     * @param keyStore
     * @param trustStore
     */
    public Trusted(KeyStore trustStore, KeyStore keyStore) {
        super();
        this.trustStore = trustStore;
        this.keyStore = keyStore;
    }

    @JsonProperty("trustStore")
    public KeyStore getTrustStore() {
        return trustStore;
    }

    @JsonProperty("trustStore")
    public void setTrustStore(KeyStore trustStore) {
        this.trustStore = trustStore;
    }

    @JsonProperty("keyStore")
    public KeyStore getKeyStore() {
        return keyStore;
    }

    @JsonProperty("keyStore")
    public void setKeyStore(KeyStore keyStore) {
        this.keyStore = keyStore;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("trustStore", trustStore).append("keyStore", keyStore).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(trustStore).append(additionalProperties).append(keyStore).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Trusted) == false) {
            return false;
        }
        Trusted rhs = ((Trusted) other);
        return new EqualsBuilder().append(trustStore, rhs.trustStore).append(additionalProperties, rhs.additionalProperties).append(keyStore, rhs.keyStore).isEquals();
    }

}
