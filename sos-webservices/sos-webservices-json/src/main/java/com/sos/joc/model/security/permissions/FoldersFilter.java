
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
 * Folders Filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "identityServiceName",
    "controllerId",
    "roleName",
    "folderNames",
    "auditLog"
})
public class FoldersFilter {

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
    @JsonProperty("folderNames")
    private List<String> folderNames = new ArrayList<String>();
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
    public FoldersFilter() {
    }

    /**
     * 
     * @param identityServiceName
     * @param controllerId
     * @param auditLog
     * @param roleName
     * @param folderNames
     */
    public FoldersFilter(String identityServiceName, String controllerId, String roleName, List<String> folderNames, AuditParams auditLog) {
        super();
        this.identityServiceName = identityServiceName;
        this.controllerId = controllerId;
        this.roleName = roleName;
        this.folderNames = folderNames;
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
    @JsonProperty("folderNames")
    public List<String> getFolderNames() {
        return folderNames;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("folderNames")
    public void setFolderNames(List<String> folderNames) {
        this.folderNames = folderNames;
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
        return new ToStringBuilder(this).append("identityServiceName", identityServiceName).append("controllerId", controllerId).append("roleName", roleName).append("folderNames", folderNames).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(roleName).append(folderNames).append(identityServiceName).append(controllerId).append(auditLog).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FoldersFilter) == false) {
            return false;
        }
        FoldersFilter rhs = ((FoldersFilter) other);
        return new EqualsBuilder().append(roleName, rhs.roleName).append(folderNames, rhs.folderNames).append(identityServiceName, rhs.identityServiceName).append(controllerId, rhs.controllerId).append(auditLog, rhs.auditLog).isEquals();
    }

}
