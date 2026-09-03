
package com.sos.joc.model.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * login
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "title",
    "enableRememberMe",
    "customLogo"
})
public class Login {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    private String title;
    @JsonProperty("enableRememberMe")
    private Boolean enableRememberMe = false;
    /**
     * login logo
     * <p>
     * 
     * 
     */
    @JsonProperty("customLogo")
    private LoginLogo customLogo;

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
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
        return new ToStringBuilder(this).append("title", title).append("enableRememberMe", enableRememberMe).append("customLogo", customLogo).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(customLogo).append(enableRememberMe).append(title).toHashCode();
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
        return new EqualsBuilder().append(customLogo, rhs.customLogo).append(enableRememberMe, rhs.enableRememberMe).append(title, rhs.title).isEquals();
    }

}
