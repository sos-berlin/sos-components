
package com.sos.joc.model.encipherment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * encrypt
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "toEncrypt",
    "certAlias",
    "certificate",
    "jobResourceName",
    "auditLog"
})
public class EncryptRequestFilter {

    @JsonProperty("toEncrypt")
    private String toEncrypt;
    @JsonProperty("certAlias")
    private String certAlias;
    @JsonProperty("certificate")
    private String certificate;
    @JsonProperty("jobResourceName")
    private String jobResourceName;
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    @JsonProperty("toEncrypt")
    public String getToEncrypt() {
        return toEncrypt;
    }

    @JsonProperty("toEncrypt")
    public void setToEncrypt(String toEncrypt) {
        this.toEncrypt = toEncrypt;
    }

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

    @JsonProperty("jobResourceName")
    public String getJobResourceName() {
        return jobResourceName;
    }

    @JsonProperty("jobResourceName")
    public void setJobResourceName(String jobResourceName) {
        this.jobResourceName = jobResourceName;
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
        return new ToStringBuilder(this).append("toEncrypt", toEncrypt).append("certAlias", certAlias).append("certificate", certificate).append("jobResourceName", jobResourceName).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(certAlias).append(certificate).append(toEncrypt).append(jobResourceName).append(auditLog).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof EncryptRequestFilter) == false) {
            return false;
        }
        EncryptRequestFilter rhs = ((EncryptRequestFilter) other);
        return new EqualsBuilder().append(certAlias, rhs.certAlias).append(certificate, rhs.certificate).append(toEncrypt, rhs.toEncrypt).append(jobResourceName, rhs.jobResourceName).append(auditLog, rhs.auditLog).isEquals();
    }

}
