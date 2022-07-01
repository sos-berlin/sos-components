
package com.sos.joc.model.publish.folder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.publish.ExportFile;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * ExportFolderFilter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "forSigning",
    "shallowCopy",
    "exportFile",
    "auditLog"
})
public class ExportFolderFilter {

    /**
     * ExportFolderForSigningFilter
     * <p>
     * 
     * 
     */
    @JsonProperty("forSigning")
    private ExportFolderForSigning forSigning;
    /**
     * Shallow Copy ExportFolderFilter
     * <p>
     * 
     * 
     */
    @JsonProperty("shallowCopy")
    private ExportFolderShallowCopy shallowCopy;
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
     * ExportFolderForSigningFilter
     * <p>
     * 
     * 
     */
    @JsonProperty("forSigning")
    public ExportFolderForSigning getForSigning() {
        return forSigning;
    }

    /**
     * ExportFolderForSigningFilter
     * <p>
     * 
     * 
     */
    @JsonProperty("forSigning")
    public void setForSigning(ExportFolderForSigning forSigning) {
        this.forSigning = forSigning;
    }

    /**
     * Shallow Copy ExportFolderFilter
     * <p>
     * 
     * 
     */
    @JsonProperty("shallowCopy")
    public ExportFolderShallowCopy getShallowCopy() {
        return shallowCopy;
    }

    /**
     * Shallow Copy ExportFolderFilter
     * <p>
     * 
     * 
     */
    @JsonProperty("shallowCopy")
    public void setShallowCopy(ExportFolderShallowCopy shallowCopy) {
        this.shallowCopy = shallowCopy;
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
        return new ToStringBuilder(this).append("forSigning", forSigning).append("shallowCopy", shallowCopy).append("exportFile", exportFile).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(exportFile).append(forSigning).append(auditLog).append(shallowCopy).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ExportFolderFilter) == false) {
            return false;
        }
        ExportFolderFilter rhs = ((ExportFolderFilter) other);
        return new EqualsBuilder().append(exportFile, rhs.exportFile).append(forSigning, rhs.forSigning).append(auditLog, rhs.auditLog).append(shallowCopy, rhs.shallowCopy).isEquals();
    }

}
