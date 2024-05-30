
package com.sos.joc.model.encipherment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * imports a certificate for encipherment
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "certAlias",
    "privateKeyPath",
    "jobResourceFolder",
    "auditLog"
})
public class ImportCertificateRequestFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("certAlias")
    private String certAlias;
    @JsonProperty("privateKeyPath")
    private String privateKeyPath;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobResourceFolder")
    private String jobResourceFolder;
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

    @JsonProperty("privateKeyPath")
    public String getPrivateKeyPath() {
        return privateKeyPath;
    }

    @JsonProperty("privateKeyPath")
    public void setPrivateKeyPath(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobResourceFolder")
    public String getJobResourceFolder() {
        return jobResourceFolder;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobResourceFolder")
    public void setJobResourceFolder(String jobResourceFolder) {
        this.jobResourceFolder = jobResourceFolder;
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
        return new ToStringBuilder(this).append("certAlias", certAlias).append("privateKeyPath", privateKeyPath).append("jobResourceFolder", jobResourceFolder).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(certAlias).append(privateKeyPath).append(auditLog).append(jobResourceFolder).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ImportCertificateRequestFilter) == false) {
            return false;
        }
        ImportCertificateRequestFilter rhs = ((ImportCertificateRequestFilter) other);
        return new EqualsBuilder().append(certAlias, rhs.certAlias).append(privateKeyPath, rhs.privateKeyPath).append(auditLog, rhs.auditLog).append(jobResourceFolder, rhs.jobResourceFolder).isEquals();
    }

}
