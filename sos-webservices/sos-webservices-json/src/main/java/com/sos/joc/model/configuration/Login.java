
package com.sos.joc.model.configuration;

import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * security_configuration
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "defaultProfileAccount",
    "customLogo"
})
public class Login {

    @JsonProperty("defaultProfileAccount")
    private String defaultProfileAccount;
    /**
     * security_configuration
     * <p>
     * 
     * 
     */
    @JsonProperty("customLogo")
    private LoginLogo customLogo;

    /**
     * 
     * @return
     *     The defaultProfileAccount
     */
    @JsonProperty("defaultProfileAccount")
    public String getDefaultProfileAccount() {
        return defaultProfileAccount;
    }

    /**
     * 
     * @param defaultProfileAccount
     *     The defaultProfileAccount
     */
    @JsonProperty("defaultProfileAccount")
    public void setDefaultProfileAccount(String defaultProfileAccount) {
        this.defaultProfileAccount = defaultProfileAccount;
    }

    /**
     * security_configuration
     * <p>
     * 
     * 
     * @return
     *     The customLogo
     */
    @JsonProperty("customLogo")
    public LoginLogo getCustomLogo() {
        return customLogo;
    }

    /**
     * security_configuration
     * <p>
     * 
     * 
     * @param customLogo
     *     The customLogo
     */
    @JsonProperty("customLogo")
    public void setCustomLogo(LoginLogo customLogo) {
        this.customLogo = customLogo;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(defaultProfileAccount).append(customLogo).toHashCode();
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
        return new EqualsBuilder().append(defaultProfileAccount, rhs.defaultProfileAccount).append(customLogo, rhs.customLogo).isEquals();
    }

}
