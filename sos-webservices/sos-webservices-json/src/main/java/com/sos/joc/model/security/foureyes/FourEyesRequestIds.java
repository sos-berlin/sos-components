
package com.sos.joc.model.security.foureyes;

import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * FourEyesRequestIds
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "ids",
    "auditLog"
})
public class FourEyesRequestIds {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("ids")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<Long> ids = new LinkedHashSet<Long>();
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    /**
     * No args constructor for use in serialization
     * 
     */
    public FourEyesRequestIds() {
    }

    /**
     * 
     * @param auditLog
     * @param ids
     */
    public FourEyesRequestIds(Set<Long> ids, AuditParams auditLog) {
        super();
        this.ids = ids;
        this.auditLog = auditLog;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("ids")
    public Set<Long> getIds() {
        return ids;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("ids")
    public void setIds(Set<Long> ids) {
        this.ids = ids;
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
        return new ToStringBuilder(this).append("ids", ids).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(auditLog).append(ids).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FourEyesRequestIds) == false) {
            return false;
        }
        FourEyesRequestIds rhs = ((FourEyesRequestIds) other);
        return new EqualsBuilder().append(auditLog, rhs.auditLog).append(ids, rhs.ids).isEquals();
    }

}
