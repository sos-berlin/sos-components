
package com.sos.inventory.model.filetransfer.fragments.protocol.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "hostname",
    "port",
    "connectTimeout"
})
public class BasicConnection {

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
     * Duration in seconds, human-readable format (e.g. 30s, 1m, 1h 30m). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("connectTimeout")
    private String connectTimeout;

    /**
     * No args constructor for use in serialization
     * 
     */
    public BasicConnection() {
    }

    /**
     * 
     * @param hostname
     * @param port
     * @param connectTimeout
     */
    public BasicConnection(String hostname, String port, String connectTimeout) {
        super();
        this.hostname = hostname;
        this.port = port;
        this.connectTimeout = connectTimeout;
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
     * Duration in seconds, human-readable format (e.g. 30s, 1m, 1h 30m). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("connectTimeout")
    public String getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * Duration in seconds, human-readable format (e.g. 30s, 1m, 1h 30m). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("connectTimeout")
    public void setConnectTimeout(String connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("hostname", hostname).append("port", port).append("connectTimeout", connectTimeout).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(connectTimeout).append(hostname).append(port).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof BasicConnection) == false) {
            return false;
        }
        BasicConnection rhs = ((BasicConnection) other);
        return new EqualsBuilder().append(connectTimeout, rhs.connectTimeout).append(hostname, rhs.hostname).append(port, rhs.port).isEquals();
    }

}
