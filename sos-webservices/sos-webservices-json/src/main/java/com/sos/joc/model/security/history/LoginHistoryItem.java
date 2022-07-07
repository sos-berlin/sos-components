
package com.sos.joc.model.security.history;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * LoginHistory
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "accountName",
    "loginDate",
    "loginSuccess",
    "details"
})
public class LoginHistoryItem {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("accountName")
    private String accountName;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("loginDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date loginDate;
    @JsonProperty("loginSuccess")
    private Boolean loginSuccess;
    /**
     * LoginHistoryDetails
     * <p>
     * 
     * 
     */
    @JsonProperty("details")
    private LoginHistoryDetails details;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public LoginHistoryItem() {
    }

    /**
     * 
     * @param loginSuccess
     * @param accountName
     * @param loginDate
     * @param details
     */
    public LoginHistoryItem(String accountName, Date loginDate, Boolean loginSuccess, LoginHistoryDetails details) {
        super();
        this.accountName = accountName;
        this.loginDate = loginDate;
        this.loginSuccess = loginSuccess;
        this.details = details;
    }

    /**
     * string without < and >
     * <p>
     * 
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
     * 
     */
    @JsonProperty("accountName")
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("loginDate")
    public Date getLoginDate() {
        return loginDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("loginDate")
    public void setLoginDate(Date loginDate) {
        this.loginDate = loginDate;
    }

    @JsonProperty("loginSuccess")
    public Boolean getLoginSuccess() {
        return loginSuccess;
    }

    @JsonProperty("loginSuccess")
    public void setLoginSuccess(Boolean loginSuccess) {
        this.loginSuccess = loginSuccess;
    }

    /**
     * LoginHistoryDetails
     * <p>
     * 
     * 
     */
    @JsonProperty("details")
    public LoginHistoryDetails getDetails() {
        return details;
    }

    /**
     * LoginHistoryDetails
     * <p>
     * 
     * 
     */
    @JsonProperty("details")
    public void setDetails(LoginHistoryDetails details) {
        this.details = details;
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
        return new ToStringBuilder(this).append("accountName", accountName).append("loginDate", loginDate).append("loginSuccess", loginSuccess).append("details", details).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(loginDate).append(loginSuccess).append(details).append(additionalProperties).append(accountName).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof LoginHistoryItem) == false) {
            return false;
        }
        LoginHistoryItem rhs = ((LoginHistoryItem) other);
        return new EqualsBuilder().append(loginDate, rhs.loginDate).append(loginSuccess, rhs.loginSuccess).append(details, rhs.details).append(additionalProperties, rhs.additionalProperties).append(accountName, rhs.accountName).isEquals();
    }

}
