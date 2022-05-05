
package com.sos.joc.model.security.roles;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * RoleRename
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "identityServiceName",
    "roleOldName",
    "roleNewName",
    "auditLog"
})
public class RoleRename {

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
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("roleOldName")
    private String roleOldName;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("roleNewName")
    private String roleNewName;
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
    public RoleRename() {
    }

    /**
     * 
     * @param identityServiceName
     * @param roleNewName
     * @param auditLog
     * @param roleOldName
     */
    public RoleRename(String identityServiceName, String roleOldName, String roleNewName, AuditParams auditLog) {
        super();
        this.identityServiceName = identityServiceName;
        this.roleOldName = roleOldName;
        this.roleNewName = roleNewName;
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
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("roleOldName")
    public String getRoleOldName() {
        return roleOldName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("roleOldName")
    public void setRoleOldName(String roleOldName) {
        this.roleOldName = roleOldName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("roleNewName")
    public String getRoleNewName() {
        return roleNewName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("roleNewName")
    public void setRoleNewName(String roleNewName) {
        this.roleNewName = roleNewName;
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
        return new ToStringBuilder(this).append("identityServiceName", identityServiceName).append("roleOldName", roleOldName).append("roleNewName", roleNewName).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(identityServiceName).append(roleNewName).append(auditLog).append(roleOldName).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RoleRename) == false) {
            return false;
        }
        RoleRename rhs = ((RoleRename) other);
        return new EqualsBuilder().append(identityServiceName, rhs.identityServiceName).append(roleNewName, rhs.roleNewName).append(auditLog, rhs.auditLog).append(roleOldName, rhs.roleOldName).isEquals();
    }

}
