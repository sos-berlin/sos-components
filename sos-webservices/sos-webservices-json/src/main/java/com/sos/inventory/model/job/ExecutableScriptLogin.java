
package com.sos.inventory.model.job;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * executable script login
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "credentialKey",
    "withUserProfile"
})
public class ExecutableScriptLogin {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("credentialKey")
    private String credentialKey;
    @JsonProperty("withUserProfile")
    private Boolean withUserProfile = false;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ExecutableScriptLogin() {
    }

    /**
     * 
     * @param withUserProfile
     * @param credentialKey
     */
    public ExecutableScriptLogin(String credentialKey, Boolean withUserProfile) {
        super();
        this.credentialKey = credentialKey;
        this.withUserProfile = withUserProfile;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("credentialKey")
    public String getCredentialKey() {
        return credentialKey;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("credentialKey")
    public void setCredentialKey(String credentialKey) {
        this.credentialKey = credentialKey;
    }

    @JsonProperty("withUserProfile")
    public Boolean getWithUserProfile() {
        return withUserProfile;
    }

    @JsonProperty("withUserProfile")
    public void setWithUserProfile(Boolean withUserProfile) {
        this.withUserProfile = withUserProfile;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("credentialKey", credentialKey).append("withUserProfile", withUserProfile).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(withUserProfile).append(credentialKey).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ExecutableScriptLogin) == false) {
            return false;
        }
        ExecutableScriptLogin rhs = ((ExecutableScriptLogin) other);
        return new EqualsBuilder().append(withUserProfile, rhs.withUserProfile).append(credentialKey, rhs.credentialKey).isEquals();
    }

}
