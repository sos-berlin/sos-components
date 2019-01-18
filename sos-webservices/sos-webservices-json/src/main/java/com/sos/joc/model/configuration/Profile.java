
package com.sos.joc.model.configuration;

import java.util.Date;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "account",
    "lastLogin"
})
public class Profile {

    @JsonProperty("account")
    private String account;
    @JsonProperty("lastLogin")
    private Date lastLogin;

    /**
     * 
     * @return
     *     The account
     */
    @JsonProperty("account")
    public String getAccount() {
        return account;
    }

    /**
     * 
     * @param account
     *     The account
     */
    @JsonProperty("account")
    public void setAccount(String account) {
        this.account = account;
    }

    /**
     * 
     * @return
     *     The lastLogin
     */
    @JsonProperty("lastLogin")
    public Date getLastLogin() {
        return lastLogin;
    }

    /**
     * 
     * @param lastLogin
     *     The lastLogin
     */
    @JsonProperty("lastLogin")
    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(account).append(lastLogin).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Profile) == false) {
            return false;
        }
        Profile rhs = ((Profile) other);
        return new EqualsBuilder().append(account, rhs.account).append(lastLogin, rhs.lastLogin).isEquals();
    }

}
