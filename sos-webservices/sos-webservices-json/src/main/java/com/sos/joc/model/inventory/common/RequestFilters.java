
package com.sos.joc.model.inventory.common;

import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * common inventory request filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "objects",
    "auditLog"
})
public class RequestFilters {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("objects")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<RequestFilter> objects = new LinkedHashSet<RequestFilter>();
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
    @JsonProperty("objects")
    public Set<RequestFilter> getObjects() {
        return objects;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("objects")
    public void setObjects(Set<RequestFilter> objects) {
        this.objects = objects;
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
        return new ToStringBuilder(this).append("objects", objects).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(auditLog).append(objects).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RequestFilters) == false) {
            return false;
        }
        RequestFilters rhs = ((RequestFilters) other);
        return new EqualsBuilder().append(auditLog, rhs.auditLog).append(objects, rhs.objects).isEquals();
    }

}
