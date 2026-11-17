
package com.sos.inventory.model.filetransfer.fragments.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.fragments.protocol.common.BasicConnection;
import com.sos.inventory.model.filetransfer.fragments.protocol.jump.JumpProcessing;
import com.sos.inventory.model.filetransfer.fragments.protocol.sftp.SftpAuthentication;
import com.sos.inventory.model.filetransfer.fragments.protocol.sftp.SftpKeepAlive;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Jump Fragment
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "httpProxyFragmentName",
    "socksProxyFragmentName",
    "alternativeJumpFragmentName",
    "connection",
    "authentication",
    "socketTimeout",
    "keepAlive",
    "strictHostkeyChecking",
    "configurationFile",
    "yadeClientCommand",
    "tempDirectoryParent",
    "jumpProcessing"
})
public class JumpFragment
    extends AProtocolFragment
{

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
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("alternativeJumpFragmentName")
    private String alternativeJumpFragmentName;
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
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("configurationFile")
    private String configurationFile;
    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("yadeClientCommand")
    private String yadeClientCommand;
    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("tempDirectoryParent")
    private String tempDirectoryParent;
    /**
     * SFTP processing options on the jump host.
     * 
     */
    @JsonProperty("jumpProcessing")
    @JsonPropertyDescription("SFTP processing options on the jump host.")
    private JumpProcessing jumpProcessing;

    /**
     * No args constructor for use in serialization
     * 
     */
    public JumpFragment() {
    }

    /**
     * 
     * @param yadeClientCommand
     * @param tempDirectoryParent
     * @param keepAlive
     * @param jumpProcessing
     * @param configurationFile
     * @param socketTimeout
     * @param alternativeJumpFragmentName
     * @param httpProxyFragmentName
     * @param connection
     * @param strictHostkeyChecking
     * @param socksProxyFragmentName
     * @param authentication
     */
    public JumpFragment(String httpProxyFragmentName, String socksProxyFragmentName, String alternativeJumpFragmentName, BasicConnection connection, SftpAuthentication authentication, String socketTimeout, SftpKeepAlive keepAlive, String strictHostkeyChecking, String configurationFile, String yadeClientCommand, String tempDirectoryParent, JumpProcessing jumpProcessing) {
        super();
        this.httpProxyFragmentName = httpProxyFragmentName;
        this.socksProxyFragmentName = socksProxyFragmentName;
        this.alternativeJumpFragmentName = alternativeJumpFragmentName;
        this.connection = connection;
        this.authentication = authentication;
        this.socketTimeout = socketTimeout;
        this.keepAlive = keepAlive;
        this.strictHostkeyChecking = strictHostkeyChecking;
        this.configurationFile = configurationFile;
        this.yadeClientCommand = yadeClientCommand;
        this.tempDirectoryParent = tempDirectoryParent;
        this.jumpProcessing = jumpProcessing;
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
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("alternativeJumpFragmentName")
    public String getAlternativeJumpFragmentName() {
        return alternativeJumpFragmentName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("alternativeJumpFragmentName")
    public void setAlternativeJumpFragmentName(String alternativeJumpFragmentName) {
        this.alternativeJumpFragmentName = alternativeJumpFragmentName;
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

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("yadeClientCommand")
    public String getYadeClientCommand() {
        return yadeClientCommand;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("yadeClientCommand")
    public void setYadeClientCommand(String yadeClientCommand) {
        this.yadeClientCommand = yadeClientCommand;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("tempDirectoryParent")
    public String getTempDirectoryParent() {
        return tempDirectoryParent;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("tempDirectoryParent")
    public void setTempDirectoryParent(String tempDirectoryParent) {
        this.tempDirectoryParent = tempDirectoryParent;
    }

    /**
     * SFTP processing options on the jump host.
     * 
     */
    @JsonProperty("jumpProcessing")
    public JumpProcessing getJumpProcessing() {
        return jumpProcessing;
    }

    /**
     * SFTP processing options on the jump host.
     * 
     */
    @JsonProperty("jumpProcessing")
    public void setJumpProcessing(JumpProcessing jumpProcessing) {
        this.jumpProcessing = jumpProcessing;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("httpProxyFragmentName", httpProxyFragmentName).append("socksProxyFragmentName", socksProxyFragmentName).append("alternativeJumpFragmentName", alternativeJumpFragmentName).append("connection", connection).append("authentication", authentication).append("socketTimeout", socketTimeout).append("keepAlive", keepAlive).append("strictHostkeyChecking", strictHostkeyChecking).append("configurationFile", configurationFile).append("yadeClientCommand", yadeClientCommand).append("tempDirectoryParent", tempDirectoryParent).append("jumpProcessing", jumpProcessing).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(yadeClientCommand).append(tempDirectoryParent).append(keepAlive).append(jumpProcessing).append(httpProxyFragmentName).append(strictHostkeyChecking).append(configurationFile).append(socketTimeout).append(alternativeJumpFragmentName).append(connection).append(socksProxyFragmentName).append(authentication).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JumpFragment) == false) {
            return false;
        }
        JumpFragment rhs = ((JumpFragment) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(yadeClientCommand, rhs.yadeClientCommand).append(tempDirectoryParent, rhs.tempDirectoryParent).append(keepAlive, rhs.keepAlive).append(jumpProcessing, rhs.jumpProcessing).append(httpProxyFragmentName, rhs.httpProxyFragmentName).append(strictHostkeyChecking, rhs.strictHostkeyChecking).append(configurationFile, rhs.configurationFile).append(socketTimeout, rhs.socketTimeout).append(alternativeJumpFragmentName, rhs.alternativeJumpFragmentName).append(connection, rhs.connection).append(socksProxyFragmentName, rhs.socksProxyFragmentName).append(authentication, rhs.authentication).isEquals();
    }

}
