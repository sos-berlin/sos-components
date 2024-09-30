
package com.sos.joc.model.tag.group;

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
 * common group request filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "groups",
    "auditLog"
})
public class RequestFilters {

    /**
     * tags
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("groups")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> groups = new LinkedHashSet<String>();
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    /**
     * tags
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("groups")
    public Set<String> getGroups() {
        return groups;
    }

    /**
     * tags
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("groups")
    public void setGroups(Set<String> groups) {
        this.groups = groups;
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
        return new ToStringBuilder(this).append("groups", groups).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(auditLog).append(groups).toHashCode();
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
        return new EqualsBuilder().append(auditLog, rhs.auditLog).append(groups, rhs.groups).isEquals();
    }

}
