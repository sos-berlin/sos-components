
package com.sos.joc.model.security.accounts;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Account Filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "identityServiceName",
    "accountNames",
    "disabled",
    "auditLog"
})
public class AccountsFilter {

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
    @JsonProperty("accountNames")
    private List<String> accountNames = new ArrayList<String>();
    /**
     * disabled parameter
     * <p>
     * controls if the object is disabled
     * 
     */
    @JsonProperty("disabled")
    @JsonPropertyDescription("controls if the object is disabled")
    private Boolean disabled = false;
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
    public AccountsFilter() {
    }

    /**
     * 
     * @param identityServiceName
     * @param auditLog
     * @param disabled
     * @param accountNames
     */
    public AccountsFilter(String identityServiceName, List<String> accountNames, Boolean disabled, AuditParams auditLog) {
        super();
        this.identityServiceName = identityServiceName;
        this.accountNames = accountNames;
        this.disabled = disabled;
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
    @JsonProperty("accountNames")
    public List<String> getAccountNames() {
        return accountNames;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("accountNames")
    public void setAccountNames(List<String> accountNames) {
        this.accountNames = accountNames;
    }

    /**
     * disabled parameter
     * <p>
     * controls if the object is disabled
     * 
     */
    @JsonProperty("disabled")
    public Boolean getDisabled() {
        return disabled;
    }

    /**
     * disabled parameter
     * <p>
     * controls if the object is disabled
     * 
     */
    @JsonProperty("disabled")
    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
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
        return new ToStringBuilder(this).append("identityServiceName", identityServiceName).append("accountNames", accountNames).append("disabled", disabled).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(identityServiceName).append(disabled).append(auditLog).append(accountNames).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AccountsFilter) == false) {
            return false;
        }
        AccountsFilter rhs = ((AccountsFilter) other);
        return new EqualsBuilder().append(identityServiceName, rhs.identityServiceName).append(disabled, rhs.disabled).append(auditLog, rhs.auditLog).append(accountNames, rhs.accountNames).isEquals();
    }

}
