
package com.sos.joc.model.security.sessions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * ActiveSession
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "accountName",
    "identityService",
    "timeout",
    "auditLog"
})
public class ActiveSession {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    private String id;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("accountName")
    private String accountName;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("identityService")
    private String identityService;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("timeout")
    private Long timeout;
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
    public ActiveSession() {
    }

    /**
     * 
     * @param identityService
     * @param auditLog
     * @param accountName
     * @param id
     * @param timeout
     */
    public ActiveSession(String id, String accountName, String identityService, Long timeout, AuditParams auditLog) {
        super();
        this.id = id;
        this.accountName = accountName;
        this.identityService = identityService;
        this.timeout = timeout;
        this.auditLog = auditLog;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("accountName")
    public String getAccountName() {
        return accountName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("accountName")
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("identityService")
    public String getIdentityService() {
        return identityService;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("identityService")
    public void setIdentityService(String identityService) {
        this.identityService = identityService;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("timeout")
    public Long getTimeout() {
        return timeout;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("timeout")
    public void setTimeout(Long timeout) {
        this.timeout = timeout;
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
        return new ToStringBuilder(this).append("id", id).append("accountName", accountName).append("identityService", identityService).append("timeout", timeout).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(identityService).append(id).append(auditLog).append(accountName).append(timeout).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ActiveSession) == false) {
            return false;
        }
        ActiveSession rhs = ((ActiveSession) other);
        return new EqualsBuilder().append(identityService, rhs.identityService).append(id, rhs.id).append(auditLog, rhs.auditLog).append(accountName, rhs.accountName).append(timeout, rhs.timeout).isEquals();
    }

}
