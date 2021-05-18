
package com.sos.joc.model.audit;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * auditLogDetailFilter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "auditLogId"
})
public class AuditLogDetailFilter {

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("auditLogId")
    private Long auditLogId;

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("auditLogId")
    public Long getAuditLogId() {
        return auditLogId;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("auditLogId")
    public void setAuditLogId(Long auditLogId) {
        this.auditLogId = auditLogId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("auditLogId", auditLogId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(auditLogId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AuditLogDetailFilter) == false) {
            return false;
        }
        AuditLogDetailFilter rhs = ((AuditLogDetailFilter) other);
        return new EqualsBuilder().append(auditLogId, rhs.auditLogId).isEquals();
    }

}
