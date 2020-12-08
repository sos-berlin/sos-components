
package com.sos.joc.model.publish;

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
 * Export Releasables
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "draftConfigurations",
    "releasedConfigurations",
    "auditLog"
})
public class ExportReleasables {

    @JsonProperty("draftConfigurations")
    private List<ReleasableConfig> draftConfigurations = new ArrayList<ReleasableConfig>();
    @JsonProperty("releasedConfigurations")
    private List<ReleasedConfig> releasedConfigurations = new ArrayList<ReleasedConfig>();
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    @JsonProperty("draftConfigurations")
    public List<ReleasableConfig> getDraftConfigurations() {
        return draftConfigurations;
    }

    @JsonProperty("draftConfigurations")
    public void setDraftConfigurations(List<ReleasableConfig> draftConfigurations) {
        this.draftConfigurations = draftConfigurations;
    }

    @JsonProperty("releasedConfigurations")
    public List<ReleasedConfig> getReleasedConfigurations() {
        return releasedConfigurations;
    }

    @JsonProperty("releasedConfigurations")
    public void setReleasedConfigurations(List<ReleasedConfig> releasedConfigurations) {
        this.releasedConfigurations = releasedConfigurations;
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
        return new ToStringBuilder(this).append("draftConfigurations", draftConfigurations).append("releasedConfigurations", releasedConfigurations).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(draftConfigurations).append(releasedConfigurations).append(auditLog).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ExportReleasables) == false) {
            return false;
        }
        ExportReleasables rhs = ((ExportReleasables) other);
        return new EqualsBuilder().append(draftConfigurations, rhs.draftConfigurations).append(releasedConfigurations, rhs.releasedConfigurations).append(auditLog, rhs.auditLog).isEquals();
    }

}
