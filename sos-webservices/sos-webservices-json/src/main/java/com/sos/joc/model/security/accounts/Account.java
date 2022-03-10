
package com.sos.joc.model.security.accounts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Account
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "identityServiceName",
    "accountName",
    "password",
    "repeatedPassword",
    "oldPassword",
    "forcePasswordChange",
    "disabled",
    "roles",
    "auditLog"
})
public class Account {

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
    @JsonProperty("accountName")
    private String accountName;
    @JsonProperty("password")
    private String password;
    @JsonProperty("repeatedPassword")
    private String repeatedPassword;
    @JsonProperty("oldPassword")
    private String oldPassword;
    /**
     * forcePasswordChange parameter
     * <p>
     * controls if the account is forced to change the password
     * 
     */
    @JsonProperty("forcePasswordChange")
    @JsonPropertyDescription("controls if the account is forced to change the password")
    private Boolean forcePasswordChange = false;
    /**
     * disabled parameter
     * <p>
     * controls if the object is disabled
     * 
     */
    @JsonProperty("disabled")
    @JsonPropertyDescription("controls if the object is disabled")
    private Boolean disabled = false;
    @JsonProperty("roles")
    private List<String> roles = new ArrayList<String>();
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Account() {
    }

    /**
     * 
     * @param identityServiceName
     * @param password
     * @param auditLog
     * @param accountName
     * @param oldPassword
     * @param forcePasswordChange
     * @param roles
     * @param disabled
     * @param repeatedPassword
     */
    public Account(String identityServiceName, String accountName, String password, String repeatedPassword, String oldPassword, Boolean forcePasswordChange, Boolean disabled, List<String> roles, AuditParams auditLog) {
        super();
        this.identityServiceName = identityServiceName;
        this.accountName = accountName;
        this.password = password;
        this.repeatedPassword = repeatedPassword;
        this.oldPassword = oldPassword;
        this.forcePasswordChange = forcePasswordChange;
        this.disabled = disabled;
        this.roles = roles;
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

    @JsonProperty("password")
    public String getPassword() {
        return password;
    }

    @JsonProperty("password")
    public void setPassword(String password) {
        this.password = password;
    }

    @JsonProperty("repeatedPassword")
    public String getRepeatedPassword() {
        return repeatedPassword;
    }

    @JsonProperty("repeatedPassword")
    public void setRepeatedPassword(String repeatedPassword) {
        this.repeatedPassword = repeatedPassword;
    }

    @JsonProperty("oldPassword")
    public String getOldPassword() {
        return oldPassword;
    }

    @JsonProperty("oldPassword")
    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    /**
     * forcePasswordChange parameter
     * <p>
     * controls if the account is forced to change the password
     * 
     */
    @JsonProperty("forcePasswordChange")
    public Boolean getForcePasswordChange() {
        return forcePasswordChange;
    }

    /**
     * forcePasswordChange parameter
     * <p>
     * controls if the account is forced to change the password
     * 
     */
    @JsonProperty("forcePasswordChange")
    public void setForcePasswordChange(Boolean forcePasswordChange) {
        this.forcePasswordChange = forcePasswordChange;
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

    @JsonProperty("roles")
    public List<String> getRoles() {
        return roles;
    }

    @JsonProperty("roles")
    public void setRoles(List<String> roles) {
        this.roles = roles;
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

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("identityServiceName", identityServiceName).append("accountName", accountName).append("password", password).append("repeatedPassword", repeatedPassword).append("oldPassword", oldPassword).append("forcePasswordChange", forcePasswordChange).append("disabled", disabled).append("roles", roles).append("auditLog", auditLog).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(identityServiceName).append(password).append(auditLog).append(accountName).append(oldPassword).append(forcePasswordChange).append(roles).append(disabled).append(additionalProperties).append(repeatedPassword).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Account) == false) {
            return false;
        }
        Account rhs = ((Account) other);
        return new EqualsBuilder().append(identityServiceName, rhs.identityServiceName).append(password, rhs.password).append(auditLog, rhs.auditLog).append(accountName, rhs.accountName).append(oldPassword, rhs.oldPassword).append(forcePasswordChange, rhs.forcePasswordChange).append(roles, rhs.roles).append(disabled, rhs.disabled).append(additionalProperties, rhs.additionalProperties).append(repeatedPassword, rhs.repeatedPassword).isEquals();
    }

}
