
package com.sos.joc.model.publish;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.sign.JocKeyPair;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * set key filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "keys",
    "auditLog"
})
public class SetKeyFilter {

    /**
     * SOS PGP Key Pair
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("keys")
    private JocKeyPair keys;
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    /**
     * SOS PGP Key Pair
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("keys")
    public JocKeyPair getKeys() {
        return keys;
    }

    /**
     * SOS PGP Key Pair
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("keys")
    public void setKeys(JocKeyPair keys) {
        this.keys = keys;
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
        return new ToStringBuilder(this).append("keys", keys).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(auditLog).append(keys).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SetKeyFilter) == false) {
            return false;
        }
        SetKeyFilter rhs = ((SetKeyFilter) other);
        return new EqualsBuilder().append(auditLog, rhs.auditLog).append(keys, rhs.keys).isEquals();
    }

}
