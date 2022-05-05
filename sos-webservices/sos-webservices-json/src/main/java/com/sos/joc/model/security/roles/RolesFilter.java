
package com.sos.joc.model.security.roles;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Role Filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "identityServiceName",
    "roleNames",
    "auditLog"
})
public class RolesFilter {

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
     * 
     * (Required)
     * 
     */
    @JsonProperty("roleNames")
    private List<String> roleNames = new ArrayList<String>();
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
    public RolesFilter() {
    }

    /**
     * 
     * @param identityServiceName
     * @param auditLog
     * @param roleNames
     */
    public RolesFilter(String identityServiceName, List<String> roleNames, AuditParams auditLog) {
        super();
        this.identityServiceName = identityServiceName;
        this.roleNames = roleNames;
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
     * 
     * (Required)
     * 
     */
    @JsonProperty("roleNames")
    public List<String> getRoleNames() {
        return roleNames;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("roleNames")
    public void setRoleNames(List<String> roleNames) {
        this.roleNames = roleNames;
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
        return new ToStringBuilder(this).append("identityServiceName", identityServiceName).append("roleNames", roleNames).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(identityServiceName).append(auditLog).append(roleNames).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RolesFilter) == false) {
            return false;
        }
        RolesFilter rhs = ((RolesFilter) other);
        return new EqualsBuilder().append(identityServiceName, rhs.identityServiceName).append(auditLog, rhs.auditLog).append(roleNames, rhs.roleNames).isEquals();
    }

}
