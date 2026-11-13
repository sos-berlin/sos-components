
package com.sos.inventory.model.filetransfer.fragments.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.fragments.protocol.common.BasicConnection;
import com.sos.inventory.model.filetransfer.fragments.protocol.jump.JumpProcessing;
import com.sos.inventory.model.filetransfer.fragments.protocol.refs.JumpAlternativeFragmentRef;
import com.sos.inventory.model.filetransfer.fragments.protocol.refs.ProxyFragmentRefAny;
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
    "proxyRef",
    "alternativeRef",
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
     * HTTP Proxy Reference
     * <p>
     * Reference to an HTTP or SOCKS proxy fragment.
     * 
     */
    @JsonProperty("proxyRef")
    @JsonPropertyDescription("Reference to an HTTP or SOCKS proxy fragment.")
    private ProxyFragmentRefAny proxyRef;
    /**
     * JUMP Alternative Fragment Reference
     * <p>
     * 
     * 
     */
    @JsonProperty("alternativeRef")
    private JumpAlternativeFragmentRef alternativeRef;
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
     * @param proxyRef
     * @param alternativeRef
     * @param keepAlive
     * @param jumpProcessing
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
    public JumpFragment(ProxyFragmentRefAny proxyRef, JumpAlternativeFragmentRef alternativeRef, BasicConnection connection, SftpAuthentication authentication, String socketTimeout, SftpKeepAlive keepAlive, String strictHostkeyChecking, String configurationFile, String yadeClientCommand, String tempDirectoryParent, JumpProcessing jumpProcessing, String credentialStoreRef, String decryptionRef, String version, String path, String title, String documentationName) {
        super(credentialStoreRef, decryptionRef, version, path, title, documentationName);
        this.proxyRef = proxyRef;
        this.alternativeRef = alternativeRef;
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
     * JUMP Alternative Fragment Reference
     * <p>
     * 
     * 
     */
    @JsonProperty("alternativeRef")
    public JumpAlternativeFragmentRef getAlternativeRef() {
        return alternativeRef;
    }

    /**
     * JUMP Alternative Fragment Reference
     * <p>
     * 
     * 
     */
    @JsonProperty("alternativeRef")
    public void setAlternativeRef(JumpAlternativeFragmentRef alternativeRef) {
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("proxyRef", proxyRef).append("alternativeRef", alternativeRef).append("connection", connection).append("authentication", authentication).append("socketTimeout", socketTimeout).append("keepAlive", keepAlive).append("strictHostkeyChecking", strictHostkeyChecking).append("configurationFile", configurationFile).append("yadeClientCommand", yadeClientCommand).append("tempDirectoryParent", tempDirectoryParent).append("jumpProcessing", jumpProcessing).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(yadeClientCommand).append(tempDirectoryParent).append(proxyRef).append(alternativeRef).append(keepAlive).append(jumpProcessing).append(configurationFile).append(socketTimeout).append(connection).append(strictHostkeyChecking).append(authentication).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(yadeClientCommand, rhs.yadeClientCommand).append(tempDirectoryParent, rhs.tempDirectoryParent).append(proxyRef, rhs.proxyRef).append(alternativeRef, rhs.alternativeRef).append(keepAlive, rhs.keepAlive).append(jumpProcessing, rhs.jumpProcessing).append(configurationFile, rhs.configurationFile).append(socketTimeout, rhs.socketTimeout).append(connection, rhs.connection).append(strictHostkeyChecking, rhs.strictHostkeyChecking).append(authentication, rhs.authentication).isEquals();
    }

}
