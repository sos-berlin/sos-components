
package com.sos.joc.model.inventory.release;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * filter to recall already released configurations
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "releasables",
    "auditLog"
})
public class ReleasableRecallFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("releasables")
    private List<Releasable> releasables = new ArrayList<Releasable>();
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
    @JsonProperty("releasables")
    public List<Releasable> getReleasables() {
        return releasables;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("releasables")
    public void setReleasables(List<Releasable> releasables) {
        this.releasables = releasables;
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
        return new ToStringBuilder(this).append("releasables", releasables).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(auditLog).append(releasables).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ReleasableRecallFilter) == false) {
            return false;
        }
        ReleasableRecallFilter rhs = ((ReleasableRecallFilter) other);
        return new EqualsBuilder().append(auditLog, rhs.auditLog).append(releasables, rhs.releasables).isEquals();
    }

}
