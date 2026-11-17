
package com.sos.inventory.model.filetransfer.fragments.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.fragments.protocol.smb.SmbAuthentication;
import com.sos.inventory.model.filetransfer.fragments.protocol.smb.SmbConnection;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * SMB Fragment
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "alternativeSmbFragmentName",
    "connection",
    "authentication",
    "configurationFile"
})
public class SmbFragment
    extends AProtocolFragment
{

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("alternativeSmbFragmentName")
    private String alternativeSmbFragmentName;
    /**
     * SMB connection settings.
     * (Required)
     * 
     */
    @JsonProperty("connection")
    @JsonPropertyDescription("SMB connection settings.")
    private SmbConnection connection;
    /**
     * SMB authentication. Provide exactly one of 'anonymous', 'guest', 'ntlm', 'kerberos', 'spnego'.
     * (Required)
     * 
     */
    @JsonProperty("authentication")
    @JsonPropertyDescription("SMB authentication. Provide exactly one of 'anonymous', 'guest', 'ntlm', 'kerberos', 'spnego'.")
    private SmbAuthentication authentication;
    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("configurationFile")
    private String configurationFile;

    /**
     * No args constructor for use in serialization
     * 
     */
    public SmbFragment() {
    }

    /**
     * 
     * @param configurationFile
     * @param alternativeSmbFragmentName
     * @param connection
     * @param authentication
     */
    public SmbFragment(String alternativeSmbFragmentName, SmbConnection connection, SmbAuthentication authentication, String configurationFile) {
        super();
        this.alternativeSmbFragmentName = alternativeSmbFragmentName;
        this.connection = connection;
        this.authentication = authentication;
        this.configurationFile = configurationFile;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("alternativeSmbFragmentName")
    public String getAlternativeSmbFragmentName() {
        return alternativeSmbFragmentName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("alternativeSmbFragmentName")
    public void setAlternativeSmbFragmentName(String alternativeSmbFragmentName) {
        this.alternativeSmbFragmentName = alternativeSmbFragmentName;
    }

    /**
     * SMB connection settings.
     * (Required)
     * 
     */
    @JsonProperty("connection")
    public SmbConnection getConnection() {
        return connection;
    }

    /**
     * SMB connection settings.
     * (Required)
     * 
     */
    @JsonProperty("connection")
    public void setConnection(SmbConnection connection) {
        this.connection = connection;
    }

    /**
     * SMB authentication. Provide exactly one of 'anonymous', 'guest', 'ntlm', 'kerberos', 'spnego'.
     * (Required)
     * 
     */
    @JsonProperty("authentication")
    public SmbAuthentication getAuthentication() {
        return authentication;
    }

    /**
     * SMB authentication. Provide exactly one of 'anonymous', 'guest', 'ntlm', 'kerberos', 'spnego'.
     * (Required)
     * 
     */
    @JsonProperty("authentication")
    public void setAuthentication(SmbAuthentication authentication) {
        this.authentication = authentication;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("configurationFile")
    public String getConfigurationFile() {
        return configurationFile;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("configurationFile")
    public void setConfigurationFile(String configurationFile) {
        this.configurationFile = configurationFile;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("alternativeSmbFragmentName", alternativeSmbFragmentName).append("connection", connection).append("authentication", authentication).append("configurationFile", configurationFile).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(alternativeSmbFragmentName).append(connection).append(authentication).append(configurationFile).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SmbFragment) == false) {
            return false;
        }
        SmbFragment rhs = ((SmbFragment) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(alternativeSmbFragmentName, rhs.alternativeSmbFragmentName).append(connection, rhs.connection).append(authentication, rhs.authentication).append(configurationFile, rhs.configurationFile).isEquals();
    }

}
