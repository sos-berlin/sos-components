
package com.sos.joc.model.publish.git;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * List of Git credentials
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "credentials",
    "remoteUris"
})
public class GitCredentialsList {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("credentials")
    private List<GitCredentials> credentials = new ArrayList<GitCredentials>();
    @JsonProperty("remoteUris")
    private List<String> remoteUris = new ArrayList<String>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("credentials")
    public List<GitCredentials> getCredentials() {
        return credentials;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("credentials")
    public void setCredentials(List<GitCredentials> credentials) {
        this.credentials = credentials;
    }

    @JsonProperty("remoteUris")
    public List<String> getRemoteUris() {
        return remoteUris;
    }

    @JsonProperty("remoteUris")
    public void setRemoteUris(List<String> remoteUris) {
        this.remoteUris = remoteUris;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("credentials", credentials).append("remoteUris", remoteUris).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(remoteUris).append(credentials).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof GitCredentialsList) == false) {
            return false;
        }
        GitCredentialsList rhs = ((GitCredentialsList) other);
        return new EqualsBuilder().append(remoteUris, rhs.remoteUris).append(credentials, rhs.credentials).isEquals();
    }

}
