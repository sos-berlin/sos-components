
package com.sos.joc.model.publish;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * ExportFilter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "forSigning",
    "controllerId",
    "deployables",
    "releasables",
    "auditLog"
})
public class ExportFilter {

    /**
     * decides if the export is meant for signing the exported objects [default=false]
     * 
     */
    @JsonProperty("forSigning")
    @JsonPropertyDescription("decides if the export is meant for signing the exported objects [default=false]")
    private Boolean forSigning;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * Export Deployables
     * <p>
     * 
     * 
     */
    @JsonProperty("deployables")
    private ExportDeployables deployables;
    /**
     * Export Releasables
     * <p>
     * 
     * 
     */
    @JsonProperty("releasables")
    private ExportReleasables releasables;
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    /**
     * decides if the export is meant for signing the exported objects [default=false]
     * 
     */
    @JsonProperty("forSigning")
    public Boolean getForSigning() {
        return forSigning;
    }

    /**
     * decides if the export is meant for signing the exported objects [default=false]
     * 
     */
    @JsonProperty("forSigning")
    public void setForSigning(Boolean forSigning) {
        this.forSigning = forSigning;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * Export Deployables
     * <p>
     * 
     * 
     */
    @JsonProperty("deployables")
    public ExportDeployables getDeployables() {
        return deployables;
    }

    /**
     * Export Deployables
     * <p>
     * 
     * 
     */
    @JsonProperty("deployables")
    public void setDeployables(ExportDeployables deployables) {
        this.deployables = deployables;
    }

    /**
     * Export Releasables
     * <p>
     * 
     * 
     */
    @JsonProperty("releasables")
    public ExportReleasables getReleasables() {
        return releasables;
    }

    /**
     * Export Releasables
     * <p>
     * 
     * 
     */
    @JsonProperty("releasables")
    public void setReleasables(ExportReleasables releasables) {
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
        return new ToStringBuilder(this).append("forSigning", forSigning).append("controllerId", controllerId).append("deployables", deployables).append("releasables", releasables).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deployables).append(forSigning).append(controllerId).append(auditLog).append(releasables).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ExportFilter) == false) {
            return false;
        }
        ExportFilter rhs = ((ExportFilter) other);
        return new EqualsBuilder().append(deployables, rhs.deployables).append(forSigning, rhs.forSigning).append(controllerId, rhs.controllerId).append(auditLog, rhs.auditLog).append(releasables, rhs.releasables).isEquals();
    }

}
