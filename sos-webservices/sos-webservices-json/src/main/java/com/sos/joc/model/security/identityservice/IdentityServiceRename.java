
package com.sos.joc.model.security.identityservice;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * IdentityServiceRename
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "identityServiceOldName",
    "identityServiceNewName",
    "auditLog"
})
public class IdentityServiceRename {

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("identityServiceOldName")
    private String identityServiceOldName;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("identityServiceNewName")
    private String identityServiceNewName;
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
    public IdentityServiceRename() {
    }

    /**
     * 
     * @param identityServiceOldName
     * @param auditLog
     * @param identityServiceNewName
     */
    public IdentityServiceRename(String identityServiceOldName, String identityServiceNewName, AuditParams auditLog) {
        super();
        this.identityServiceOldName = identityServiceOldName;
        this.identityServiceNewName = identityServiceNewName;
        this.auditLog = auditLog;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("identityServiceOldName")
    public String getIdentityServiceOldName() {
        return identityServiceOldName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("identityServiceOldName")
    public void setIdentityServiceOldName(String identityServiceOldName) {
        this.identityServiceOldName = identityServiceOldName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("identityServiceNewName")
    public String getIdentityServiceNewName() {
        return identityServiceNewName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("identityServiceNewName")
    public void setIdentityServiceNewName(String identityServiceNewName) {
        this.identityServiceNewName = identityServiceNewName;
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
        return new ToStringBuilder(this).append("identityServiceOldName", identityServiceOldName).append("identityServiceNewName", identityServiceNewName).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(identityServiceOldName).append(auditLog).append(identityServiceNewName).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof IdentityServiceRename) == false) {
            return false;
        }
        IdentityServiceRename rhs = ((IdentityServiceRename) other);
        return new EqualsBuilder().append(identityServiceOldName, rhs.identityServiceOldName).append(auditLog, rhs.auditLog).append(identityServiceNewName, rhs.identityServiceNewName).isEquals();
    }

}
