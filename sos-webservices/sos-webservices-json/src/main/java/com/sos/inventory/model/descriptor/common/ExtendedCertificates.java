
package com.sos.inventory.model.descriptor.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Deployment Descriptor for extended Certificates Schema
 * <p>
 * JS7 Deployment Descriptor Certificates Schema
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "cert"
})
public class ExtendedCertificates
    extends Certificates
{

    @JsonProperty("cert")
    private String cert;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ExtendedCertificates() {
    }

    /**
     * 
     * @param trustStorePassword
     * @param keyStorePassword
     * @param keyAlias
     * @param keyPassword
     * @param keyStore
     * @param trustStore
     * @param cert
     */
    public ExtendedCertificates(String cert, String keyStore, String keyStorePassword, String keyPassword, String keyAlias, String trustStore, String trustStorePassword) {
        super(keyStore, keyStorePassword, keyPassword, keyAlias, trustStore, trustStorePassword);
        this.cert = cert;
    }

    @JsonProperty("cert")
    public String getCert() {
        return cert;
    }

    @JsonProperty("cert")
    public void setCert(String cert) {
        this.cert = cert;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("cert", cert).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(cert).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ExtendedCertificates) == false) {
            return false;
        }
        ExtendedCertificates rhs = ((ExtendedCertificates) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(cert, rhs.cert).isEquals();
    }
}
