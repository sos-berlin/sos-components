
package com.sos.joc.model.security.folders;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.common.Folder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * FolderRename
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "identityServiceName",
    "controllerId",
    "roleName",
    "oldFolderName",
    "newFolder",
    "auditLog"
})
public class FolderRename {

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("identityServiceName")
    private String identityServiceName;
    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("roleName")
    private String roleName;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("oldFolderName")
    private String oldFolderName;
    /**
     * folders
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("newFolder")
    private Folder newFolder;
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    /**
     * No args constructor for use in serialization
     * 
     */
    public FolderRename() {
    }

    /**
     * 
     * @param identityServiceName
     * @param controllerId
     * @param auditLog
     * @param roleName
     * @param oldFolderName
     * @param newFolder
     */
    public FolderRename(String identityServiceName, String controllerId, String roleName, String oldFolderName, Folder newFolder, AuditParams auditLog) {
        super();
        this.identityServiceName = identityServiceName;
        this.controllerId = controllerId;
        this.roleName = roleName;
        this.oldFolderName = oldFolderName;
        this.newFolder = newFolder;
        this.auditLog = auditLog;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("identityServiceName")
    public String getIdentityServiceName() {
        return identityServiceName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("identityServiceName")
    public void setIdentityServiceName(String identityServiceName) {
        this.identityServiceName = identityServiceName;
    }

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("roleName")
    public String getRoleName() {
        return roleName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("roleName")
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("oldFolderName")
    public String getOldFolderName() {
        return oldFolderName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("oldFolderName")
    public void setOldFolderName(String oldFolderName) {
        this.oldFolderName = oldFolderName;
    }

    /**
     * folders
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("newFolder")
    public Folder getNewFolder() {
        return newFolder;
    }

    /**
     * folders
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("newFolder")
    public void setNewFolder(Folder newFolder) {
        this.newFolder = newFolder;
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
        return new ToStringBuilder(this).append("identityServiceName", identityServiceName).append("controllerId", controllerId).append("roleName", roleName).append("oldFolderName", oldFolderName).append("newFolder", newFolder).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(identityServiceName).append(controllerId).append(auditLog).append(roleName).append(oldFolderName).append(newFolder).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FolderRename) == false) {
            return false;
        }
        FolderRename rhs = ((FolderRename) other);
        return new EqualsBuilder().append(identityServiceName, rhs.identityServiceName).append(controllerId, rhs.controllerId).append(auditLog, rhs.auditLog).append(roleName, rhs.roleName).append(oldFolderName, rhs.oldFolderName).append(newFolder, rhs.newFolder).isEquals();
    }

}
