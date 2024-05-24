
package com.sos.joc.model.encipherment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * certificate and additional credetials for encipherment
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "certAlias",
    "certificate",
    "privateKeyPath"
})
public class EncCertificate {

    @JsonProperty("certAlias")
    private String certAlias;
    @JsonProperty("certificate")
    private String certificate;
    @JsonProperty("privateKeyPath")
    private String privateKeyPath;

    @JsonProperty("certAlias")
    public String getCertAlias() {
        return certAlias;
    }

    @JsonProperty("certAlias")
    public void setCertAlias(String certAlias) {
        this.certAlias = certAlias;
    }

    @JsonProperty("certificate")
    public String getCertificate() {
        return certificate;
    }

    @JsonProperty("certificate")
    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    @JsonProperty("privateKeyPath")
    public String getPrivateKeyPath() {
        return privateKeyPath;
    }

    @JsonProperty("privateKeyPath")
    public void setPrivateKeyPath(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("certAlias", certAlias).append("certificate", certificate).append("privateKeyPath", privateKeyPath).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(certAlias).append(certificate).append(privateKeyPath).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof EncCertificate) == false) {
            return false;
        }
        EncCertificate rhs = ((EncCertificate) other);
        return new EqualsBuilder().append(certAlias, rhs.certAlias).append(certificate, rhs.certificate).append(privateKeyPath, rhs.privateKeyPath).isEquals();
    }

}
