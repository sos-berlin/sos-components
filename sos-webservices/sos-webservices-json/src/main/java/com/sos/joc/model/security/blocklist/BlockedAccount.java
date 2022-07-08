
package com.sos.joc.model.security.blocklist;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * BlockedAccount
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "accountName",
    "notice",
    "since",
    "auditLog"
})
public class BlockedAccount {

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
    @JsonProperty("notice")
    private String notice;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("since")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date since;
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
    public BlockedAccount() {
    }

    /**
     * 
     * @param auditLog
     * @param accountName
     * @param notice
     * @param since
     */
    public BlockedAccount(String accountName, String notice, Date since, AuditParams auditLog) {
        super();
        this.accountName = accountName;
        this.notice = notice;
        this.since = since;
        this.auditLog = auditLog;
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
    @JsonProperty("notice")
    public String getNotice() {
        return notice;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("notice")
    public void setNotice(String notice) {
        this.notice = notice;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("since")
    public Date getSince() {
        return since;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("since")
    public void setSince(Date since) {
        this.since = since;
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
        return new ToStringBuilder(this).append("accountName", accountName).append("notice", notice).append("since", since).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(auditLog).append(accountName).append(notice).append(since).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof BlockedAccount) == false) {
            return false;
        }
        BlockedAccount rhs = ((BlockedAccount) other);
        return new EqualsBuilder().append(auditLog, rhs.auditLog).append(accountName, rhs.accountName).append(notice, rhs.notice).append(since, rhs.since).isEquals();
    }

}
