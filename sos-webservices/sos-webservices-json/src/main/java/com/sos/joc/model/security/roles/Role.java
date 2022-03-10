
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
 * Role
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "identityServiceName",
    "roleName",
    "controllers",
    "auditLog",
    "additionalProperties"
})
public class Role {

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
    @JsonProperty("roleName")
    private String roleName;
    @JsonProperty("controllers")
    private List<String> controllers = new ArrayList<String>();
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;
    @JsonProperty("additionalProperties")
    private Object additionalProperties;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Role() {
    }

    /**
     * 
     * @param identityServiceName
     * @param auditLog
     * @param roleName
     * @param controllers
     * @param additionalProperties
     */
    public Role(String identityServiceName, String roleName, List<String> controllers, AuditParams auditLog, Object additionalProperties) {
        super();
        this.identityServiceName = identityServiceName;
        this.roleName = roleName;
        this.controllers = controllers;
        this.auditLog = auditLog;
        this.additionalProperties = additionalProperties;
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

    @JsonProperty("controllers")
    public List<String> getControllers() {
        return controllers;
    }

    @JsonProperty("controllers")
    public void setControllers(List<String> controllers) {
        this.controllers = controllers;
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

    @JsonProperty("additionalProperties")
    public Object getAdditionalProperties() {
        return additionalProperties;
    }

    @JsonProperty("additionalProperties")
    public void setAdditionalProperties(Object additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("identityServiceName", identityServiceName).append("roleName", roleName).append("controllers", controllers).append("auditLog", auditLog).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(roleName).append(controllers).append(identityServiceName).append(additionalProperties).append(auditLog).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Role) == false) {
            return false;
        }
        Role rhs = ((Role) other);
        return new EqualsBuilder().append(roleName, rhs.roleName).append(controllers, rhs.controllers).append(identityServiceName, rhs.identityServiceName).append(additionalProperties, rhs.additionalProperties).append(auditLog, rhs.auditLog).isEquals();
    }

}
