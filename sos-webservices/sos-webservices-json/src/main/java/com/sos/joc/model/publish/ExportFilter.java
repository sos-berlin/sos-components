
package com.sos.joc.model.publish;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * ExportFilter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "useShortPath",
    "startFolder",
    "forSigning",
    "shallowCopy",
    "exportFile",
    "auditLog"
})
public class ExportFilter {

    @JsonProperty("useShortPath")
    private Boolean useShortPath = false;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("startFolder")
    private String startFolder;
    /**
     * ExportForSigningFilter
     * <p>
     * 
     * 
     */
    @JsonProperty("forSigning")
    private ExportForSigning forSigning;
    /**
     * Shallow Copy Export Filter
     * <p>
     * 
     * 
     */
    @JsonProperty("shallowCopy")
    private ExportShallowCopy shallowCopy;
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

    @JsonProperty("useShortPath")
    public Boolean getUseShortPath() {
        return useShortPath;
    }

    @JsonProperty("useShortPath")
    public void setUseShortPath(Boolean useShortPath) {
        this.useShortPath = useShortPath;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("startFolder")
    public String getStartFolder() {
        return startFolder;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("startFolder")
    public void setStartFolder(String startFolder) {
        this.startFolder = startFolder;
    }

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
     * Shallow Copy Export Filter
     * <p>
     * 
     * 
     */
    @JsonProperty("shallowCopy")
    public ExportShallowCopy getShallowCopy() {
        return shallowCopy;
    }

    /**
     * Shallow Copy Export Filter
     * <p>
     * 
     * 
     */
    @JsonProperty("shallowCopy")
    public void setShallowCopy(ExportShallowCopy shallowCopy) {
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
        return new ToStringBuilder(this).append("useShortPath", useShortPath).append("startFolder", startFolder).append("forSigning", forSigning).append("shallowCopy", shallowCopy).append("exportFile", exportFile).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(exportFile).append(auditLog).append(forSigning).append(startFolder).append(shallowCopy).append(useShortPath).toHashCode();
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
        return new EqualsBuilder().append(exportFile, rhs.exportFile).append(auditLog, rhs.auditLog).append(forSigning, rhs.forSigning).append(startFolder, rhs.startFolder).append(shallowCopy, rhs.shallowCopy).append(useShortPath, rhs.useShortPath).isEquals();
    }

}
