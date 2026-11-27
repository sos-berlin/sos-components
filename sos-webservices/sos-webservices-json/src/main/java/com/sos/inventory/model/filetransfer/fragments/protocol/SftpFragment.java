
package com.sos.inventory.model.filetransfer.fragments.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.fragments.protocol.common.BasicConnection;
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
    "jumpFragmentName",
    "httpProxyFragmentName",
    "socksProxyFragmentName",
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
    @JsonProperty("jumpFragmentName")
    private String jumpFragmentName;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("httpProxyFragmentName")
    private String httpProxyFragmentName;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("socksProxyFragmentName")
    private String socksProxyFragmentName;
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
     * @param jumpFragmentName
     * @param keepAlive
     * @param disableAutoDetectShell
     * @param configurationFile
     * @param socketTimeout
     * @param httpProxyFragmentName
     * @param connection
     * @param strictHostkeyChecking
     * @param socksProxyFragmentName
     * @param authentication
     */
    public SftpFragment(String jumpFragmentName, String httpProxyFragmentName, String socksProxyFragmentName, BasicConnection connection, SftpAuthentication authentication, String socketTimeout, SftpKeepAlive keepAlive, String strictHostkeyChecking, String disableAutoDetectShell, String configurationFile) {
        super();
        this.jumpFragmentName = jumpFragmentName;
        this.httpProxyFragmentName = httpProxyFragmentName;
        this.socksProxyFragmentName = socksProxyFragmentName;
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
    @JsonProperty("jumpFragmentName")
    public String getJumpFragmentName() {
        return jumpFragmentName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jumpFragmentName")
    public void setJumpFragmentName(String jumpFragmentName) {
        this.jumpFragmentName = jumpFragmentName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("httpProxyFragmentName")
    public String getHttpProxyFragmentName() {
        return httpProxyFragmentName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("httpProxyFragmentName")
    public void setHttpProxyFragmentName(String httpProxyFragmentName) {
        this.httpProxyFragmentName = httpProxyFragmentName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("socksProxyFragmentName")
    public String getSocksProxyFragmentName() {
        return socksProxyFragmentName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("socksProxyFragmentName")
    public void setSocksProxyFragmentName(String socksProxyFragmentName) {
        this.socksProxyFragmentName = socksProxyFragmentName;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("jumpFragmentName", jumpFragmentName).append("httpProxyFragmentName", httpProxyFragmentName).append("socksProxyFragmentName", socksProxyFragmentName).append("connection", connection).append("authentication", authentication).append("socketTimeout", socketTimeout).append("keepAlive", keepAlive).append("strictHostkeyChecking", strictHostkeyChecking).append("disableAutoDetectShell", disableAutoDetectShell).append("configurationFile", configurationFile).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(jumpFragmentName).append(keepAlive).append(disableAutoDetectShell).append(configurationFile).append(socketTimeout).append(httpProxyFragmentName).append(connection).append(strictHostkeyChecking).append(socksProxyFragmentName).append(authentication).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(jumpFragmentName, rhs.jumpFragmentName).append(keepAlive, rhs.keepAlive).append(disableAutoDetectShell, rhs.disableAutoDetectShell).append(configurationFile, rhs.configurationFile).append(socketTimeout, rhs.socketTimeout).append(httpProxyFragmentName, rhs.httpProxyFragmentName).append(connection, rhs.connection).append(strictHostkeyChecking, rhs.strictHostkeyChecking).append(socksProxyFragmentName, rhs.socksProxyFragmentName).append(authentication, rhs.authentication).isEquals();
    }

}
