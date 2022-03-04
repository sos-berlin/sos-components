
package com.sos.joc.model.security;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "account",
    "password",
    "repeatedPassword",
    "oldPassword",
    "forcePasswordChange",
    "disabled",
    "identityServiceId",
    "roles"
})
public class SecurityConfigurationAccount {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("account")
    private String account;
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
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("identityServiceId")
    private Long identityServiceId;
    @JsonProperty("roles")
    private List<String> roles = new ArrayList<String>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public SecurityConfigurationAccount() {
    }

    /**
     * 
     * @param password
     * @param identityServiceId
     * @param oldPassword
     * @param forcePasswordChange
     * @param roles
     * @param disabled
     * @param repeatedPassword
     * @param account
     */
    public SecurityConfigurationAccount(String account, String password, String repeatedPassword, String oldPassword, Boolean forcePasswordChange, Boolean disabled, Long identityServiceId, List<String> roles) {
        super();
        this.account = account;
        this.password = password;
        this.repeatedPassword = repeatedPassword;
        this.oldPassword = oldPassword;
        this.forcePasswordChange = forcePasswordChange;
        this.disabled = disabled;
        this.identityServiceId = identityServiceId;
        this.roles = roles;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("account")
    public String getAccount() {
        return account;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("account")
    public void setAccount(String account) {
        this.account = account;
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

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("identityServiceId")
    public Long getIdentityServiceId() {
        return identityServiceId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("identityServiceId")
    public void setIdentityServiceId(Long identityServiceId) {
        this.identityServiceId = identityServiceId;
    }

    @JsonProperty("roles")
    public List<String> getRoles() {
        return roles;
    }

    @JsonProperty("roles")
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("account", account).append("password", password).append("repeatedPassword", repeatedPassword).append("oldPassword", oldPassword).append("forcePasswordChange", forcePasswordChange).append("disabled", disabled).append("identityServiceId", identityServiceId).append("roles", roles).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(password).append(identityServiceId).append(oldPassword).append(forcePasswordChange).append(roles).append(disabled).append(repeatedPassword).append(account).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SecurityConfigurationAccount) == false) {
            return false;
        }
        SecurityConfigurationAccount rhs = ((SecurityConfigurationAccount) other);
        return new EqualsBuilder().append(password, rhs.password).append(identityServiceId, rhs.identityServiceId).append(oldPassword, rhs.oldPassword).append(forcePasswordChange, rhs.forcePasswordChange).append(roles, rhs.roles).append(disabled, rhs.disabled).append(repeatedPassword, rhs.repeatedPassword).append(account, rhs.account).isEquals();
    }

}
