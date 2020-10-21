
package com.sos.joc.model.inventory.release;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.inventory.common.RequestFilter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * release
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerIds",
    "update",
    "delete",
    "auditLog"
})
public class ReleaseFilter {

    @JsonProperty("controllerIds")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> controllerIds = new LinkedHashSet<String>();
    @JsonProperty("update")
    private List<RequestFilter> update = new ArrayList<RequestFilter>();
    @JsonProperty("delete")
    private List<RequestFilter> delete = new ArrayList<RequestFilter>();
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    @JsonProperty("controllerIds")
    public Set<String> getControllerIds() {
        return controllerIds;
    }

    @JsonProperty("controllerIds")
    public void setControllerIds(Set<String> controllerIds) {
        this.controllerIds = controllerIds;
    }

    @JsonProperty("update")
    public List<RequestFilter> getUpdate() {
        return update;
    }

    @JsonProperty("update")
    public void setUpdate(List<RequestFilter> update) {
        this.update = update;
    }

    @JsonProperty("delete")
    public List<RequestFilter> getDelete() {
        return delete;
    }

    @JsonProperty("delete")
    public void setDelete(List<RequestFilter> delete) {
        this.delete = delete;
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
        return new ToStringBuilder(this).append("controllerIds", controllerIds).append("update", update).append("delete", delete).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(update).append(auditLog).append(delete).append(controllerIds).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ReleaseFilter) == false) {
            return false;
        }
        ReleaseFilter rhs = ((ReleaseFilter) other);
        return new EqualsBuilder().append(update, rhs.update).append(auditLog, rhs.auditLog).append(delete, rhs.delete).append(controllerIds, rhs.controllerIds).isEquals();
    }

}
