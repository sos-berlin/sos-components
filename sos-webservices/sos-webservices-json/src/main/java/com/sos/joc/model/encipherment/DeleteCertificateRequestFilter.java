
package com.sos.joc.model.encipherment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * delete certificate filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "certAlias",
    "auditLog"
})
public class DeleteCertificateRequestFilter {

    @JsonProperty("certAlias")
    private String certAlias;
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    @JsonProperty("certAlias")
    public String getCertAlias() {
        return certAlias;
    }

    @JsonProperty("certAlias")
    public void setCertAlias(String certAlias) {
        this.certAlias = certAlias;
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
        return new ToStringBuilder(this).append("certAlias", certAlias).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(certAlias).append(auditLog).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DeleteCertificateRequestFilter) == false) {
            return false;
        }
        DeleteCertificateRequestFilter rhs = ((DeleteCertificateRequestFilter) other);
        return new EqualsBuilder().append(certAlias, rhs.certAlias).append(auditLog, rhs.auditLog).isEquals();
    }

}
