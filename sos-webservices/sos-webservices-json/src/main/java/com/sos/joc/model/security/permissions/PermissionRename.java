
package com.sos.joc.model.security.permissions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * PermissionRename
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "identityServiceName",
    "controllerId",
    "roleName",
    "oldPermissionPath",
    "newPermission",
    "auditLog"
})
public class PermissionRename {

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
    @JsonProperty("oldPermissionPath")
    private String oldPermissionPath;
    /**
     * PermissionRec
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("newPermission")
    private Permission newPermission;
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
    public PermissionRename() {
    }

    /**
     * 
     * @param identityServiceName
     * @param controllerId
     * @param auditLog
     * @param newPermission
     * @param roleName
     * @param oldPermissionPath
     */
    public PermissionRename(String identityServiceName, String controllerId, String roleName, String oldPermissionPath, Permission newPermission, AuditParams auditLog) {
        super();
        this.identityServiceName = identityServiceName;
        this.controllerId = controllerId;
        this.roleName = roleName;
        this.oldPermissionPath = oldPermissionPath;
        this.newPermission = newPermission;
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
    @JsonProperty("oldPermissionPath")
    public String getOldPermissionPath() {
        return oldPermissionPath;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("oldPermissionPath")
    public void setOldPermissionPath(String oldPermissionPath) {
        this.oldPermissionPath = oldPermissionPath;
    }

    /**
     * PermissionRec
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("newPermission")
    public Permission getNewPermission() {
        return newPermission;
    }

    /**
     * PermissionRec
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("newPermission")
    public void setNewPermission(Permission newPermission) {
        this.newPermission = newPermission;
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
        return new ToStringBuilder(this).append("identityServiceName", identityServiceName).append("controllerId", controllerId).append("roleName", roleName).append("oldPermissionPath", oldPermissionPath).append("newPermission", newPermission).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(identityServiceName).append(controllerId).append(auditLog).append(newPermission).append(roleName).append(oldPermissionPath).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PermissionRename) == false) {
            return false;
        }
        PermissionRename rhs = ((PermissionRename) other);
        return new EqualsBuilder().append(identityServiceName, rhs.identityServiceName).append(controllerId, rhs.controllerId).append(auditLog, rhs.auditLog).append(newPermission, rhs.newPermission).append(roleName, rhs.roleName).append(oldPermissionPath, rhs.oldPermissionPath).isEquals();
    }

}
