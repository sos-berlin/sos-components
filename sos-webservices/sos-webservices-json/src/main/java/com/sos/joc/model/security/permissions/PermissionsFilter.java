
package com.sos.joc.model.security.permissions;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Permissions Filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "identityServiceName",
    "controllerId",
    "roleName",
    "permissionPaths",
    "auditLog"
})
public class PermissionsFilter {

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
     * 
     * (Required)
     * 
     */
    @JsonProperty("permissionPaths")
    private List<String> permissionPaths = new ArrayList<String>();
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
    public PermissionsFilter() {
    }

    /**
     * 
     * @param identityServiceName
     * @param permissionPaths
     * @param controllerId
     * @param auditLog
     * @param roleName
     */
    public PermissionsFilter(String identityServiceName, String controllerId, String roleName, List<String> permissionPaths, AuditParams auditLog) {
        super();
        this.identityServiceName = identityServiceName;
        this.controllerId = controllerId;
        this.roleName = roleName;
        this.permissionPaths = permissionPaths;
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
     * 
     * (Required)
     * 
     */
    @JsonProperty("permissionPaths")
    public List<String> getPermissionPaths() {
        return permissionPaths;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("permissionPaths")
    public void setPermissionPaths(List<String> permissionPaths) {
        this.permissionPaths = permissionPaths;
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
        return new ToStringBuilder(this).append("identityServiceName", identityServiceName).append("controllerId", controllerId).append("roleName", roleName).append("permissionPaths", permissionPaths).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(roleName).append(identityServiceName).append(permissionPaths).append(controllerId).append(auditLog).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PermissionsFilter) == false) {
            return false;
        }
        PermissionsFilter rhs = ((PermissionsFilter) other);
        return new EqualsBuilder().append(roleName, rhs.roleName).append(identityServiceName, rhs.identityServiceName).append(permissionPaths, rhs.permissionPaths).append(controllerId, rhs.controllerId).append(auditLog, rhs.auditLog).isEquals();
    }

}
