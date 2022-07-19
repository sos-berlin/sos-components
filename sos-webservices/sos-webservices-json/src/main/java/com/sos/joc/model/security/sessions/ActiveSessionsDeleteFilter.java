
package com.sos.joc.model.security.sessions;

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
 * ActiveSessionsDelete Filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "accountNames",
    "ids",
    "auditLog"
})
public class ActiveSessionsDeleteFilter {

    @JsonProperty("accountNames")
    private List<String> accountNames = new ArrayList<String>();
    @JsonProperty("ids")
    private List<String> ids = new ArrayList<String>();
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
    public ActiveSessionsDeleteFilter() {
    }

    /**
     * 
     * @param auditLog
     * @param ids
     * @param accountNames
     */
    public ActiveSessionsDeleteFilter(List<String> accountNames, List<String> ids, AuditParams auditLog) {
        super();
        this.accountNames = accountNames;
        this.ids = ids;
        this.auditLog = auditLog;
    }

    @JsonProperty("accountNames")
    public List<String> getAccountNames() {
        return accountNames;
    }

    @JsonProperty("accountNames")
    public void setAccountNames(List<String> accountNames) {
        this.accountNames = accountNames;
    }

    @JsonProperty("ids")
    public List<String> getIds() {
        return ids;
    }

    @JsonProperty("ids")
    public void setIds(List<String> ids) {
        this.ids = ids;
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
        return new ToStringBuilder(this).append("accountNames", accountNames).append("ids", ids).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(ids).append(auditLog).append(accountNames).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ActiveSessionsDeleteFilter) == false) {
            return false;
        }
        ActiveSessionsDeleteFilter rhs = ((ActiveSessionsDeleteFilter) other);
        return new EqualsBuilder().append(ids, rhs.ids).append(auditLog, rhs.auditLog).append(accountNames, rhs.accountNames).isEquals();
    }

}
