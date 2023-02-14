
package com.sos.inventory.model.descriptor.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Deployment Descriptor Connection Schema
 * <p>
 * JS7 JOC Descriptor Connection Schema
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "host",
    "port"
})
public class Connection {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("host")
    private String host;
    @JsonProperty("port")
    private Integer port;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Connection() {
    }

    /**
     * 
     * @param port
     * @param host
     */
    public Connection(String host, Integer port) {
        super();
        this.host = host;
        this.port = port;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("host")
    public String getHost() {
        return host;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("host")
    public void setHost(String host) {
        this.host = host;
    }

    @JsonProperty("port")
    public Integer getPort() {
        return port;
    }

    @JsonProperty("port")
    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("host", host).append("port", port).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(host).append(port).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Connection) == false) {
            return false;
        }
        Connection rhs = ((Connection) other);
        return new EqualsBuilder().append(host, rhs.host).append(port, rhs.port).isEquals();
    }

}
