
package com.sos.joc.model.publish.git;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Filter to unlink the connection of a JOC folder to a local repository
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "folder",
    "deleteRepository",
    "auditLog"
})
public class UnlinkFolderFilter {

    /**
     * string without < and >
     * <p>
     * (Required)
     */
    @JsonProperty("folder")
    private String folder;
    
    @JsonProperty("deleteRepository")
    private Boolean deleteRepository = false;
    /**
     * auditParams
     * <p>
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    /**
     * string without < and >
     * <p>
     * (Required)
     */
    @JsonProperty("folder")
    public String getFolder() {
        return folder;
    }

    /**
     * string without < and >
     * <p>
     * (Required)
     */
    @JsonProperty("folder")
    public void setFolder(String folder) {
        this.folder = folder;
    }

    @JsonProperty("deleteRepository")
    public Boolean getDeleteRepository() {
        return deleteRepository;
    }

    @JsonProperty("deleteRepository")
    public void setDeleteRepository(Boolean deleteRepository) {
        this.deleteRepository = deleteRepository;
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
     */
    @JsonProperty("auditLog")
    public void setAuditLog(AuditParams auditLog) {
        this.auditLog = auditLog;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("folder", folder).append("deleteRepository", deleteRepository).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(folder).append(auditLog).append(deleteRepository).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof UnlinkFolderFilter) == false) {
            return false;
        }
        UnlinkFolderFilter rhs = ((UnlinkFolderFilter) other);
        return new EqualsBuilder().append(folder, rhs.folder).append(auditLog, rhs.auditLog).append(deleteRepository, rhs.deleteRepository).isEquals();
    }

}
