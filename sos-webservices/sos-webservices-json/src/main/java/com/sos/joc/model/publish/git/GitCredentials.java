
package com.sos.joc.model.publish.git;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * GitCredentials
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "gitAccount",
    "username",
    "email",
    "keyfilePath",
    "password",
    "personalAccessToken",
    "gitServer"
})
public class GitCredentials {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("gitAccount")
    private String gitAccount;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("username")
    private String username;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("email")
    private String email;
    /**
     * path or filename of a private Key. Empty filename possible.
     * 
     */
    @JsonProperty("keyfilePath")
    @JsonPropertyDescription("path or filename of a private Key. Empty filename possible.")
    private String keyfilePath;
    @JsonProperty("password")
    private String password;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("personalAccessToken")
    private String personalAccessToken;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("gitServer")
    private String gitServer;

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("gitAccount")
    public String getGitAccount() {
        return gitAccount;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("gitAccount")
    public void setGitAccount(String gitAccount) {
        this.gitAccount = gitAccount;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("username")
    public String getUsername() {
        return username;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("username")
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("email")
    public String getEmail() {
        return email;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("email")
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * path or filename of a private Key. Empty filename possible.
     * 
     */
    @JsonProperty("keyfilePath")
    public String getKeyfilePath() {
        return keyfilePath;
    }

    /**
     * path or filename of a private Key. Empty filename possible.
     * 
     */
    @JsonProperty("keyfilePath")
    public void setKeyfilePath(String keyfilePath) {
        this.keyfilePath = keyfilePath;
    }

    @JsonProperty("password")
    public String getPassword() {
        return password;
    }

    @JsonProperty("password")
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("personalAccessToken")
    public String getPersonalAccessToken() {
        return personalAccessToken;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("personalAccessToken")
    public void setPersonalAccessToken(String personalAccessToken) {
        this.personalAccessToken = personalAccessToken;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("gitServer")
    public String getGitServer() {
        return gitServer;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("gitServer")
    public void setGitServer(String gitServer) {
        this.gitServer = gitServer;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("gitAccount", gitAccount).append("username", username).append("email", email).append("keyfilePath", keyfilePath).append("password", password).append("personalAccessToken", personalAccessToken).append("gitServer", gitServer).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(password).append(gitAccount).append(personalAccessToken).append(gitServer).append(email).append(username).append(keyfilePath).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof GitCredentials) == false) {
            return false;
        }
        GitCredentials rhs = ((GitCredentials) other);
        return new EqualsBuilder().append(password, rhs.password).append(gitAccount, rhs.gitAccount).append(personalAccessToken, rhs.personalAccessToken).append(gitServer, rhs.gitServer).append(email, rhs.email).append(username, rhs.username).append(keyfilePath, rhs.keyfilePath).isEquals();
    }

}
