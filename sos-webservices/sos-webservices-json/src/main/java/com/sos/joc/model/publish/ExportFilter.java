
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
    "forBackup",
    "recursive",
    "exportFile",
    "auditLog"
})
public class ExportFilter {

    /**
     * ExportForSigningFilter
     * <p>
     * 
     * 
     */
    @JsonProperty("forSigning")
    private ExportForSigning forSigning;
    /**
     * ExportForBackupFilter
     * <p>
     * 
     * 
     */
    @JsonProperty("forBackup")
    private ExportForBackup forBackup;
    /**
     * Decides if folders contained in the request will be processed recursively. default false
     * 
     */
    @JsonProperty("recursive")
    @JsonPropertyDescription("Decides if folders contained in the request will be processed recursively. default false")
    private Boolean recursive;
    /**
     * ExportFile
     * <p>
     * 
     * 
     */
    @JsonProperty("exportFile")
    private ExportFile exportFile;
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    /**
     * ExportForSigningFilter
     * <p>
     * 
     * 
     */
    @JsonProperty("forSigning")
    public ExportForSigning getForSigning() {
        return forSigning;
    }

    /**
     * ExportForSigningFilter
     * <p>
     * 
     * 
     */
    @JsonProperty("forSigning")
    public void setForSigning(ExportForSigning forSigning) {
        this.forSigning = forSigning;
    }

    /**
     * ExportForBackupFilter
     * <p>
     * 
     * 
     */
    @JsonProperty("forBackup")
    public ExportForBackup getForBackup() {
        return forBackup;
    }

    /**
     * ExportForBackupFilter
     * <p>
     * 
     * 
     */
    @JsonProperty("forBackup")
    public void setForBackup(ExportForBackup forBackup) {
        this.forBackup = forBackup;
    }

    /**
     * Decides if folders contained in the request will be processed recursively. default false
     * 
     */
    @JsonProperty("recursive")
    public Boolean getRecursive() {
        return recursive;
    }

    /**
     * Decides if folders contained in the request will be processed recursively. default false
     * 
     */
    @JsonProperty("recursive")
    public void setRecursive(Boolean recursive) {
        this.recursive = recursive;
    }

    /**
     * ExportFile
     * <p>
     * 
     * 
     */
    @JsonProperty("exportFile")
    public ExportFile getExportFile() {
        return exportFile;
    }

    /**
     * ExportFile
     * <p>
     * 
     * 
     */
    @JsonProperty("exportFile")
    public void setExportFile(ExportFile exportFile) {
        this.exportFile = exportFile;
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
        return new ToStringBuilder(this).append("forSigning", forSigning).append("forBackup", forBackup).append("recursive", recursive).append("exportFile", exportFile).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(forBackup).append(exportFile).append(forSigning).append(auditLog).append(recursive).toHashCode();
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
        return new EqualsBuilder().append(forBackup, rhs.forBackup).append(exportFile, rhs.exportFile).append(forSigning, rhs.forSigning).append(auditLog, rhs.auditLog).append(recursive, rhs.recursive).isEquals();
    }

}
