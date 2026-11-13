
package com.sos.inventory.model.filetransfer.fragments.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.fragments.protocol.common.BasicConnection;
import com.sos.inventory.model.filetransfer.fragments.protocol.refs.ProxyFragmentRefAny;
import com.sos.inventory.model.filetransfer.fragments.protocol.refs.SftpAlternativeFragmentRef;
import com.sos.inventory.model.filetransfer.fragments.protocol.sftp.SftpAuthentication;
import com.sos.inventory.model.filetransfer.fragments.protocol.sftp.SftpKeepAlive;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * SFTP Fragment
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "jumpRef",
    "proxyRef",
    "alternativeRef",
    "connection",
    "authentication",
    "socketTimeout",
    "keepAlive",
    "strictHostkeyChecking",
    "disableAutoDetectShell",
    "configurationFile"
})
public class SftpFragment
    extends AProtocolFragment
{

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jumpRef")
    private String jumpRef;
    /**
     * HTTP Proxy Reference
     * <p>
     * Reference to an HTTP or SOCKS proxy fragment.
     * 
     */
    @JsonProperty("proxyRef")
    @JsonPropertyDescription("Reference to an HTTP or SOCKS proxy fragment.")
    private ProxyFragmentRefAny proxyRef;
    /**
     * SFTP Alternative Fragment Reference
     * <p>
     * 
     * 
     */
    @JsonProperty("alternativeRef")
    private SftpAlternativeFragmentRef alternativeRef;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("connection")
    private BasicConnection connection;
    /**
     * SSH Authentication
     * <p>
     * SSH authentication. Provide 'account' plus exactly one of 'password', 'publicKey'.
     * (Required)
     * 
     */
    @JsonProperty("authentication")
    @JsonPropertyDescription("SSH authentication. Provide 'account' plus exactly one of 'password', 'publicKey'.")
    private SftpAuthentication authentication;
    /**
     * Duration in seconds, human-readable format (e.g. 30s, 1m, 1h 30m). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("socketTimeout")
    private String socketTimeout;
    /**
     * SSH keep-alive
     * <p>
     * 
     * 
     */
    @JsonProperty("keepAlive")
    private SftpKeepAlive keepAlive;
    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("strictHostkeyChecking")
    private String strictHostkeyChecking;
    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("disableAutoDetectShell")
    private String disableAutoDetectShell;
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
    public SftpFragment() {
    }

    /**
     * 
     * @param proxyRef
     * @param alternativeRef
     * @param keepAlive
     * @param disableAutoDetectShell
     * @param jumpRef
     * @param strictHostkeyChecking
     * @param title
     * @param decryptionRef
     * @param version
     * @param path
     * @param configurationFile
     * @param socketTimeout
     * @param credentialStoreRef
     * @param connection
     * @param documentationName
     * @param authentication
     */
    public SftpFragment(String jumpRef, ProxyFragmentRefAny proxyRef, SftpAlternativeFragmentRef alternativeRef, BasicConnection connection, SftpAuthentication authentication, String socketTimeout, SftpKeepAlive keepAlive, String strictHostkeyChecking, String disableAutoDetectShell, String configurationFile, String credentialStoreRef, String decryptionRef, String version, String path, String title, String documentationName) {
        super(credentialStoreRef, decryptionRef, version, path, title, documentationName);
        this.jumpRef = jumpRef;
        this.proxyRef = proxyRef;
        this.alternativeRef = alternativeRef;
        this.connection = connection;
        this.authentication = authentication;
        this.socketTimeout = socketTimeout;
        this.keepAlive = keepAlive;
        this.strictHostkeyChecking = strictHostkeyChecking;
        this.disableAutoDetectShell = disableAutoDetectShell;
        this.configurationFile = configurationFile;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jumpRef")
    public String getJumpRef() {
        return jumpRef;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jumpRef")
    public void setJumpRef(String jumpRef) {
        this.jumpRef = jumpRef;
    }

    /**
     * HTTP Proxy Reference
     * <p>
     * Reference to an HTTP or SOCKS proxy fragment.
     * 
     */
    @JsonProperty("proxyRef")
    public ProxyFragmentRefAny getProxyRef() {
        return proxyRef;
    }

    /**
     * HTTP Proxy Reference
     * <p>
     * Reference to an HTTP or SOCKS proxy fragment.
     * 
     */
    @JsonProperty("proxyRef")
    public void setProxyRef(ProxyFragmentRefAny proxyRef) {
        this.proxyRef = proxyRef;
    }

    /**
     * SFTP Alternative Fragment Reference
     * <p>
     * 
     * 
     */
    @JsonProperty("alternativeRef")
    public SftpAlternativeFragmentRef getAlternativeRef() {
        return alternativeRef;
    }

    /**
     * SFTP Alternative Fragment Reference
     * <p>
     * 
     * 
     */
    @JsonProperty("alternativeRef")
    public void setAlternativeRef(SftpAlternativeFragmentRef alternativeRef) {
        this.alternativeRef = alternativeRef;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("connection")
    public BasicConnection getConnection() {
        return connection;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("connection")
    public void setConnection(BasicConnection connection) {
        this.connection = connection;
    }

    /**
     * SSH Authentication
     * <p>
     * SSH authentication. Provide 'account' plus exactly one of 'password', 'publicKey'.
     * (Required)
     * 
     */
    @JsonProperty("authentication")
    public SftpAuthentication getAuthentication() {
        return authentication;
    }

    /**
     * SSH Authentication
     * <p>
     * SSH authentication. Provide 'account' plus exactly one of 'password', 'publicKey'.
     * (Required)
     * 
     */
    @JsonProperty("authentication")
    public void setAuthentication(SftpAuthentication authentication) {
        this.authentication = authentication;
    }

    /**
     * Duration in seconds, human-readable format (e.g. 30s, 1m, 1h 30m). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("socketTimeout")
    public String getSocketTimeout() {
        return socketTimeout;
    }

    /**
     * Duration in seconds, human-readable format (e.g. 30s, 1m, 1h 30m). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("socketTimeout")
    public void setSocketTimeout(String socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    /**
     * SSH keep-alive
     * <p>
     * 
     * 
     */
    @JsonProperty("keepAlive")
    public SftpKeepAlive getKeepAlive() {
        return keepAlive;
    }

    /**
     * SSH keep-alive
     * <p>
     * 
     * 
     */
    @JsonProperty("keepAlive")
    public void setKeepAlive(SftpKeepAlive keepAlive) {
        this.keepAlive = keepAlive;
    }

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("strictHostkeyChecking")
    public String getStrictHostkeyChecking() {
        return strictHostkeyChecking;
    }

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("strictHostkeyChecking")
    public void setStrictHostkeyChecking(String strictHostkeyChecking) {
        this.strictHostkeyChecking = strictHostkeyChecking;
    }

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("disableAutoDetectShell")
    public String getDisableAutoDetectShell() {
        return disableAutoDetectShell;
    }

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("disableAutoDetectShell")
    public void setDisableAutoDetectShell(String disableAutoDetectShell) {
        this.disableAutoDetectShell = disableAutoDetectShell;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("jumpRef", jumpRef).append("proxyRef", proxyRef).append("alternativeRef", alternativeRef).append("connection", connection).append("authentication", authentication).append("socketTimeout", socketTimeout).append("keepAlive", keepAlive).append("strictHostkeyChecking", strictHostkeyChecking).append("disableAutoDetectShell", disableAutoDetectShell).append("configurationFile", configurationFile).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(proxyRef).append(alternativeRef).append(keepAlive).append(disableAutoDetectShell).append(configurationFile).append(socketTimeout).append(jumpRef).append(connection).append(strictHostkeyChecking).append(authentication).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SftpFragment) == false) {
            return false;
        }
        SftpFragment rhs = ((SftpFragment) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(proxyRef, rhs.proxyRef).append(alternativeRef, rhs.alternativeRef).append(keepAlive, rhs.keepAlive).append(disableAutoDetectShell, rhs.disableAutoDetectShell).append(configurationFile, rhs.configurationFile).append(socketTimeout, rhs.socketTimeout).append(jumpRef, rhs.jumpRef).append(connection, rhs.connection).append(strictHostkeyChecking, rhs.strictHostkeyChecking).append(authentication, rhs.authentication).isEquals();
    }

}
