
package com.sos.joc.model.encipherment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * stores a certificate for encipherment
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "certAlias",
    "certificate",
    "privateKeyPath",
    "auditLog"
})
public class StoreCertificateRequestFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("certAlias")
    private String certAlias;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("certificate")
    private String certificate;
    @JsonProperty("privateKeyPath")
    private String privateKeyPath;
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("certAlias")
    public String getCertAlias() {
        return certAlias;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("certAlias")
    public void setCertAlias(String certAlias) {
        this.certAlias = certAlias;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("certificate")
    public String getCertificate() {
        return certificate;
    }

    /**
     * 
     * (Required)
     * 
     */
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

    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    public AuditParams getAuditLog() {
        return auditLog;
    }

    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    public void setAuditLog(AuditParams auditLog) {
        this.auditLog = auditLog;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("certAlias", certAlias).append("certificate", certificate).append("privateKeyPath", privateKeyPath).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(certAlias).append(certificate).append(privateKeyPath).append(auditLog).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof StoreCertificateRequestFilter) == false) {
            return false;
        }
        StoreCertificateRequestFilter rhs = ((StoreCertificateRequestFilter) other);
        return new EqualsBuilder().append(certAlias, rhs.certAlias).append(certificate, rhs.certificate).append(privateKeyPath, rhs.privateKeyPath).append(auditLog, rhs.auditLog).isEquals();
    }

}
