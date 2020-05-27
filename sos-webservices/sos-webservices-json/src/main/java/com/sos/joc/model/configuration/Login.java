
package com.sos.joc.model.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * login
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "defaultProfileAccount",
    "title",
    "enableRememberMe",
    "customLogo"
})
public class Login {

    @JsonProperty("defaultProfileAccount")
    private String defaultProfileAccount;
    @JsonProperty("title")
    private String title;
    @JsonProperty("enableRememberMe")
    private Boolean enableRememberMe = true;
    /**
     * login logo
     * <p>
     * 
     * 
     */
    @JsonProperty("customLogo")
    private LoginLogo customLogo;

    @JsonProperty("defaultProfileAccount")
    public String getDefaultProfileAccount() {
        return defaultProfileAccount;
    }

    @JsonProperty("defaultProfileAccount")
    public void setDefaultProfileAccount(String defaultProfileAccount) {
        this.defaultProfileAccount = defaultProfileAccount;
    }

    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    @JsonProperty("enableRememberMe")
    public Boolean getEnableRememberMe() {
        return enableRememberMe;
    }

    @JsonProperty("enableRememberMe")
    public void setEnableRememberMe(Boolean enableRememberMe) {
        this.enableRememberMe = enableRememberMe;
    }

    /**
     * login logo
     * <p>
     * 
     * 
     */
    @JsonProperty("customLogo")
    public LoginLogo getCustomLogo() {
        return customLogo;
    }

    /**
     * login logo
     * <p>
     * 
     * 
     */
    @JsonProperty("customLogo")
    public void setCustomLogo(LoginLogo customLogo) {
        this.customLogo = customLogo;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("defaultProfileAccount", defaultProfileAccount).append("title", title).append("enableRememberMe", enableRememberMe).append("customLogo", customLogo).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(customLogo).append(enableRememberMe).append(title).append(defaultProfileAccount).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Login) == false) {
            return false;
        }
        Login rhs = ((Login) other);
        return new EqualsBuilder().append(customLogo, rhs.customLogo).append(enableRememberMe, rhs.enableRememberMe).append(title, rhs.title).append(defaultProfileAccount, rhs.defaultProfileAccount).isEquals();
    }

}
