
package com.sos.joc.model.publish.git;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Credentials to remove
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "gitAccount",
    "gitServer"
})
public class RemoveCredentials {

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("gitAccount")
    private String gitAccount;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("gitServer")
    private String gitServer;

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
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
     * (Required)
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
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("gitServer")
    public void setGitServer(String gitServer) {
        this.gitServer = gitServer;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("gitAccount", gitAccount).append("gitServer", gitServer).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(gitAccount).append(gitServer).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RemoveCredentials) == false) {
            return false;
        }
        RemoveCredentials rhs = ((RemoveCredentials) other);
        return new EqualsBuilder().append(gitAccount, rhs.gitAccount).append(gitServer, rhs.gitServer).isEquals();
    }

}
