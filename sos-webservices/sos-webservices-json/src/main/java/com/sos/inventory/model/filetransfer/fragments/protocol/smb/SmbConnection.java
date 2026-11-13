
package com.sos.inventory.model.filetransfer.fragments.protocol.smb;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * SMB connection settings.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "hostname",
    "port",
    "sharename"
})
public class SmbConnection {

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("hostname")
    private String hostname;
    /**
     * A non-negative integer (digits only). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("port")
    private String port;
    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("sharename")
    private String sharename;

    /**
     * No args constructor for use in serialization
     * 
     */
    public SmbConnection() {
    }

    /**
     * 
     * @param hostname
     * @param port
     * @param sharename
     */
    public SmbConnection(String hostname, String port, String sharename) {
        super();
        this.hostname = hostname;
        this.port = port;
        this.sharename = sharename;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("hostname")
    public String getHostname() {
        return hostname;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("hostname")
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * A non-negative integer (digits only). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("port")
    public String getPort() {
        return port;
    }

    /**
     * A non-negative integer (digits only). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("port")
    public void setPort(String port) {
        this.port = port;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("sharename")
    public String getSharename() {
        return sharename;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("sharename")
    public void setSharename(String sharename) {
        this.sharename = sharename;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("hostname", hostname).append("port", port).append("sharename", sharename).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(hostname).append(port).append(sharename).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SmbConnection) == false) {
            return false;
        }
        SmbConnection rhs = ((SmbConnection) other);
        return new EqualsBuilder().append(hostname, rhs.hostname).append(port, rhs.port).append(sharename, rhs.sharename).isEquals();
    }

}
