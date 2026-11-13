
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
    "disableCertificateHostnameVerification"
})
public class Untrusted {

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("disableCertificateHostnameVerification")
    private String disableCertificateHostnameVerification;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Untrusted() {
    }

    /**
     * 
     * @param disableCertificateHostnameVerification
     */
    public Untrusted(String disableCertificateHostnameVerification) {
        super();
        this.disableCertificateHostnameVerification = disableCertificateHostnameVerification;
    }

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("disableCertificateHostnameVerification")
    public String getDisableCertificateHostnameVerification() {
        return disableCertificateHostnameVerification;
    }

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("disableCertificateHostnameVerification")
    public void setDisableCertificateHostnameVerification(String disableCertificateHostnameVerification) {
        this.disableCertificateHostnameVerification = disableCertificateHostnameVerification;
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
        return new ToStringBuilder(this).append("disableCertificateHostnameVerification", disableCertificateHostnameVerification).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(disableCertificateHostnameVerification).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Untrusted) == false) {
            return false;
        }
        Untrusted rhs = ((Untrusted) other);
        return new EqualsBuilder().append(disableCertificateHostnameVerification, rhs.disableCertificateHostnameVerification).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
