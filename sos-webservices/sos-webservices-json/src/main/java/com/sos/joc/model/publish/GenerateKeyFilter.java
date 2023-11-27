
package com.sos.joc.model.publish;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * set generate Key filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "validUntil",
    "keyAlgorithm",
    "dn",
    "useSslCa",
    "auditLog"
})
public class GenerateKeyFilter {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("validUntil")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date validUntil;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("keyAlgorithm")
    private String keyAlgorithm;
    @JsonProperty("dn")
    private String dn;
    @JsonProperty("useSslCa")
    private Boolean useSslCa = false;
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("validUntil")
    public Date getValidUntil() {
        return validUntil;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("validUntil")
    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("keyAlgorithm")
    public String getKeyAlgorithm() {
        return keyAlgorithm;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("keyAlgorithm")
    public void setKeyAlgorithm(String keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
    }

    @JsonProperty("dn")
    public String getDn() {
        return dn;
    }

    @JsonProperty("dn")
    public void setDn(String dn) {
        this.dn = dn;
    }

    @JsonProperty("useSslCa")
    public Boolean getUseSslCa() {
        return useSslCa;
    }

    @JsonProperty("useSslCa")
    public void setUseSslCa(Boolean useSslCa) {
        this.useSslCa = useSslCa;
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
        return new ToStringBuilder(this).append("validUntil", validUntil).append("keyAlgorithm", keyAlgorithm).append("dn", dn).append("useSslCa", useSslCa).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(validUntil).append(dn).append(keyAlgorithm).append(useSslCa).append(auditLog).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof GenerateKeyFilter) == false) {
            return false;
        }
        GenerateKeyFilter rhs = ((GenerateKeyFilter) other);
        return new EqualsBuilder().append(validUntil, rhs.validUntil).append(dn, rhs.dn).append(keyAlgorithm, rhs.keyAlgorithm).append(useSslCa, rhs.useSslCa).append(auditLog, rhs.auditLog).isEquals();
    }

}
