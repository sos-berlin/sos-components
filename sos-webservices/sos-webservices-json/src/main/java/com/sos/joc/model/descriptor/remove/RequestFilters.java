
package com.sos.joc.model.descriptor.remove;

import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.descriptor.common.RequestFilter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * common descriptor request filters
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "paths",
    "auditLog"
})
public class RequestFilters {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("paths")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<RequestFilter> paths = new LinkedHashSet<RequestFilter>();
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
    @JsonProperty("paths")
    public Set<RequestFilter> getPaths() {
        return paths;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("paths")
    public void setPaths(Set<RequestFilter> paths) {
        this.paths = paths;
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
        return new ToStringBuilder(this).append("paths", paths).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(auditLog).append(paths).toHashCode();
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
        return new EqualsBuilder().append(auditLog, rhs.auditLog).append(paths, rhs.paths).isEquals();
    }

}
